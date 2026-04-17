package com.company.carryon.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.data.network.AuthenticationException
import com.company.carryon.data.network.IncomingJobSignal
import com.company.carryon.data.network.LocationApi
import com.company.carryon.data.network.RealtimeJobService
import com.company.carryon.data.network.getLastKnownLocation
import com.company.carryon.data.network.getFcmToken
import com.company.carryon.data.network.initLocationProvider
import com.company.carryon.di.ServiceLocator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * HomeViewModel — Manages home dashboard state.
 * Handles online/offline toggle, earnings summary, active jobs, and notifications.
 */
class HomeViewModel : ViewModel() {
    companion object {
        private const val INCOMING_POLL_MS = 30_000L
        private const val INCOMING_QUEUE_LIMIT = 5
        private const val OFFER_EXPIRY_MS = 60_000L
        private const val OFFER_FADE_OUT_MS = 250L
    }

    private val authRepository = ServiceLocator.authRepository
    private val jobRepository = ServiceLocator.jobRepository
    private val earningsRepository = ServiceLocator.earningsRepository
    private val notificationsRepository = ServiceLocator.notificationsRepository

    // Driver state
    val currentDriver: StateFlow<Driver?> = authRepository.currentDriver
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Online status
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Earnings summary
    private val _earningsSummary = MutableStateFlow<UiState<EarningsSummary>>(UiState.Idle)
    val earningsSummary: StateFlow<UiState<EarningsSummary>> = _earningsSummary.asStateFlow()

    // Active jobs
    private val _activeJobs = MutableStateFlow<UiState<List<DeliveryJob>>>(UiState.Idle)
    val activeJobs: StateFlow<UiState<List<DeliveryJob>>> = _activeJobs.asStateFlow()

    // Today's completed jobs for the summary section
    private val _todayCompletedJobs = MutableStateFlow<UiState<List<DeliveryJob>>>(UiState.Idle)
    val todayCompletedJobs: StateFlow<UiState<List<DeliveryJob>>> = _todayCompletedJobs.asStateFlow()

    // Incoming job requests (sorted queue)
    private val _incomingJobs = MutableStateFlow<List<DeliveryJob>>(emptyList())
    val incomingJobs: StateFlow<List<DeliveryJob>> = _incomingJobs.asStateFlow()

    // Driver location for map display
    private val _driverLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val driverLocation: StateFlow<Pair<Double, Double>?> = _driverLocation.asStateFlow()

    // Reverse-geocoded label shown on the map overlay
    private val _currentLocationName = MutableStateFlow<String?>(null)
    val currentLocationName: StateFlow<String?> = _currentLocationName.asStateFlow()

    // Map style URL
    private val _mapStyleUrl = MutableStateFlow("")
    val mapStyleUrl: StateFlow<String> = _mapStyleUrl.asStateFlow()

    // Location tracking job
    private var locationTrackingJob: Job? = null

    // Incoming job polling job
    private var jobPollingJob: Job? = null
    private var isIncomingPollRequestInFlight = false
    private var offerExpiryJob: Job? = null

    // Client-side suppressed job IDs (rejected/accepted in-flight) to avoid popup reappearance races.
    private val suppressedIncomingJobIds = mutableSetOf<String>()
    private var acceptingJobId: String? = null
    private val incomingOfferExpiryAtMillis = mutableMapOf<String, Long>()
    private val _expiringIncomingJobIds = MutableStateFlow<Set<String>>(emptySet())
    val expiringIncomingJobIds: StateFlow<Set<String>> = _expiringIncomingJobIds.asStateFlow()
    private val _acceptInFlight = MutableStateFlow(false)
    val acceptInFlight: StateFlow<Boolean> = _acceptInFlight.asStateFlow()

    // Flag to prevent initOnlineStatusFromDriver from overriding _isOnline during a toggle request
    private var isTogglingOnline = false
    private var authRecoveryInProgress = false

    // Transient error messages for toast/snackbar
    private val _toastError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastError: SharedFlow<String> = _toastError.asSharedFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        initLocationProvider()
        loadDashboardData()
        collectRealtimeJobs()
        collectIncomingSignals()
        initOnlineStatusFromDriver()
        loadMapConfig()
        refreshDriverLocation()
    }

    /** Initialize and continuously sync online status from the driver's server-side state */
    private fun initOnlineStatusFromDriver() {
        viewModelScope.launch {
            authRepository.currentDriver
                .filterNotNull()
                .distinctUntilChanged { old, new -> old.isOnline == new.isOnline }
                .collect { driver ->
                    // Skip update while a toggle request is in-flight to avoid race condition
                    if (isTogglingOnline) return@collect
                    _isOnline.value = driver.isOnline
                    // Driver became available/refreshed — refresh dashboard data so stale errors clear.
                    loadDashboardData()
                    if (driver.isOnline) {
                        startRealtimeSubscription(driver.id)
                        registerFcmToken()
                        startLocationTracking()
                        startJobPolling()
                    } else {
                        stopRealtimeSubscription()
                        stopLocationTracking()
                        stopJobPolling()
                        clearIncomingOffers()
                    }
                }
        }
    }

    /** Load all dashboard data */
    fun loadDashboardData() {
        loadEarnings()
        loadActiveJobs()
        loadTodayCompletedJobs()
        loadNotifications()
    }

    /** Toggle driver online/offline status */
    fun toggleOnlineStatus() {
        viewModelScope.launch {
            val newStatus = !_isOnline.value
            isTogglingOnline = true
            authRepository.toggleOnline(newStatus)
                .onSuccess {
                    _isOnline.value = newStatus
                    if (newStatus) {
                        checkForIncomingJobs()
                        currentDriver.value?.id?.let { startRealtimeSubscription(it) }
                        registerFcmToken()
                        startLocationTracking()
                        startJobPolling()
                    } else {
                        stopRealtimeSubscription()
                        stopLocationTracking()
                        stopJobPolling()
                        clearIncomingOffers()
                    }
                }
                .onFailure { _toastError.tryEmit(it.message ?: "Failed to update status") }
            isTogglingOnline = false
        }
    }

    private fun registerFcmToken() {
        val token = getFcmToken() ?: return
        viewModelScope.launch {
            authRepository.updateFcmToken(token)
        }
    }

    private fun startRealtimeSubscription(driverId: String) {
        viewModelScope.launch {
            RealtimeJobService.startListening(driverId, viewModelScope)
        }
    }

    private fun stopRealtimeSubscription() {
        viewModelScope.launch {
            RealtimeJobService.stopListening()
        }
    }

    /** Load map style URL from backend */
    private fun loadMapConfig() {
        viewModelScope.launch {
            LocationApi.getMapConfig()
                .onSuccess { config -> _mapStyleUrl.value = config.styleUrl }
        }
    }

    /** Get driver's current GPS location and expose it */
    fun refreshDriverLocation() {
        getLastKnownLocation()?.let { loc ->
            _driverLocation.value = loc
            reverseGeocodeLocation(loc.first, loc.second)
        }
    }

    private fun reverseGeocodeLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            LocationApi.reverseGeocode(lat, lng)
                .onSuccess { name -> if (name.isNotBlank()) _currentLocationName.value = name }
        }
    }

    /** Start sending GPS location to the backend every 30 seconds.
     *  Retries every 2s until the first location is obtained, then switches to 30s cadence. */
    private fun startLocationTracking() {
        if (locationTrackingJob?.isActive == true) return
        locationTrackingJob = viewModelScope.launch {
            // Retry quickly until we get a real GPS fix, then slow down
            var firstFixSent = false
            while (isActive) {
                val loc = getLastKnownLocation()
                if (loc != null) {
                    val (lat, lng) = loc
                    _driverLocation.value = Pair(lat, lng)
                    authRepository.updateLocation(lat, lng)
                        .onFailure { _toastError.tryEmit("Location update failed") }
                    if (!firstFixSent) reverseGeocodeLocation(lat, lng)
                    firstFixSent = true
                }
                delay(if (firstFixSent) 30_000L else 2_000L)
            }
        }
    }

    /** Stop the location tracking loop */
    private fun stopLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
    }

    /** Poll /incoming every 30 seconds as a fallback in case Supabase Realtime or FCM misses an event. */
    private fun startJobPolling() {
        if (jobPollingJob?.isActive == true) return
        jobPollingJob = viewModelScope.launch {
            var firstPoll = true
            while (isActive) {
                if (!firstPoll) delay(INCOMING_POLL_MS)
                firstPoll = false
                refreshIncomingOffers()
            }
        }
    }

    private fun stopJobPolling() {
        jobPollingJob?.cancel()
        jobPollingJob = null
    }

    private fun startOfferExpiryLoop() {
        if (offerExpiryJob?.isActive == true) return
        offerExpiryJob = viewModelScope.launch {
            while (isActive) {
                pruneExpiredOffers()
                delay(1000L)
            }
        }
    }

    private fun stopOfferExpiryLoop() {
        offerExpiryJob?.cancel()
        offerExpiryJob = null
    }

    private fun sortAndCapOffers(jobs: List<DeliveryJob>): List<DeliveryJob> {
        return jobs
            .sortedWith { a, b ->
                val payoutDiff = b.estimatedEarnings.compareTo(a.estimatedEarnings)
                if (payoutDiff != 0) {
                    payoutDiff
                } else {
                    (b.createdAt ?: "").compareTo(a.createdAt ?: "")
                }
            }
            .take(INCOMING_QUEUE_LIMIT)
    }

    private fun pruneExpiredOffers() {
        val now = Clock.System.now().toEpochMilliseconds()
        val expiringIds = _expiringIncomingJobIds.value
        val expiredJobIds = _incomingJobs.value
            .map { it.id }
            .filter { jobId ->
                val expiryAtMillis = incomingOfferExpiryAtMillis[jobId] ?: 0L
                expiryAtMillis <= now && jobId !in expiringIds
            }

        if (expiredJobIds.isNotEmpty()) {
            beginOfferExpiry(expiredJobIds)
        } else if (_incomingJobs.value.isEmpty()) {
            stopOfferExpiryLoop()
        }
    }

    private fun mergeIncomingOffers(newJobs: List<DeliveryJob>) {
        val now = Clock.System.now().toEpochMilliseconds()
        val mergedById = LinkedHashMap<String, DeliveryJob>()

        (_incomingJobs.value + newJobs).forEach { job ->
            if (job.id in suppressedIncomingJobIds) return@forEach
            if (job.id == acceptingJobId) return@forEach
            val expiryAtMillis = incomingOfferExpiryAtMillis[job.id]
                ?: job.offerExpiryInstant?.toEpochMilliseconds()
                ?: (now + OFFER_EXPIRY_MS)

            incomingOfferExpiryAtMillis[job.id] = expiryAtMillis
            if (expiryAtMillis <= now) {
                suppressExpiredOffer(job.id)
                return@forEach
            }
            mergedById[job.id] = job
        }

        _incomingJobs.value = sortAndCapOffers(mergedById.values.toList())
        val aliveIds = _incomingJobs.value.map { it.id }.toSet()
        incomingOfferExpiryAtMillis.keys.retainAll(aliveIds + _expiringIncomingJobIds.value)
        pruneExpiredOffers()
        if (_incomingJobs.value.isNotEmpty()) {
            startOfferExpiryLoop()
        }
    }

    private fun removeOffer(jobId: String) {
        _incomingJobs.value = _incomingJobs.value.filterNot { it.id == jobId }
        incomingOfferExpiryAtMillis.remove(jobId)
        _expiringIncomingJobIds.value = _expiringIncomingJobIds.value - jobId
        if (_incomingJobs.value.isEmpty()) {
            stopOfferExpiryLoop()
        }
    }

    private fun collectRealtimeJobs() {
        viewModelScope.launch {
            RealtimeJobService.incomingJobs.collect { job ->
                mergeIncomingOffers(listOf(job))
            }
        }
    }

    private fun collectIncomingSignals() {
        viewModelScope.launch {
            IncomingJobSignal.events.collect {
                if (_isOnline.value) {
                    refreshIncomingOffers()
                }
            }
        }
    }

    private fun refreshIncomingOffers() {
        if (isIncomingPollRequestInFlight) return
        isIncomingPollRequestInFlight = true
        viewModelScope.launch {
            jobRepository.getIncomingRequests()
                .onSuccess { jobs -> mergeIncomingOffers(jobs) }
                .onFailure { _toastError.tryEmit(it.message ?: "Failed to fetch incoming jobs") }
            isIncomingPollRequestInFlight = false
        }
    }

    private fun beginOfferExpiry(jobIds: List<String>) {
        if (jobIds.isEmpty()) return
        val pendingIds = jobIds.filterNot { it in _expiringIncomingJobIds.value }
        if (pendingIds.isEmpty()) return

        _expiringIncomingJobIds.value = _expiringIncomingJobIds.value + pendingIds
        suppressedIncomingJobIds.addAll(pendingIds)

        pendingIds.forEach { jobId ->
            suppressExpiredOffer(jobId)
            viewModelScope.launch {
                delay(OFFER_FADE_OUT_MS)
                removeOffer(jobId)
            }
        }
    }

    private fun suppressExpiredOffer(jobId: String) {
        suppressedIncomingJobIds.add(jobId)
        viewModelScope.launch {
            jobRepository.rejectJob(jobId)
        }
    }

    private fun clearIncomingOffers() {
        _incomingJobs.value = emptyList()
        incomingOfferExpiryAtMillis.clear()
        _expiringIncomingJobIds.value = emptySet()
        stopOfferExpiryLoop()
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
        stopJobPolling()
        stopOfferExpiryLoop()
        viewModelScope.launch {
            RealtimeJobService.stopListening()
        }
    }

    /** Load today's earnings summary */
    fun loadEarnings() {
        viewModelScope.launch {
            _earningsSummary.value = UiState.Loading
            earningsRepository.getEarningsSummary()
                .onSuccess { _earningsSummary.value = UiState.Success(it) }
                .onFailure {
                    if (it is AuthenticationException) {
                        attemptSessionRecovery(
                            onRecovered = { loadEarnings() },
                            onRecoveryFailed = { message -> _earningsSummary.value = UiState.Error(message) }
                        )
                    } else {
                        _earningsSummary.value = UiState.Error(it.message ?: "Failed to load earnings")
                    }
                }
        }
    }

    /** Load active delivery jobs */
    fun loadActiveJobs() {
        viewModelScope.launch {
            _activeJobs.value = UiState.Loading
            jobRepository.getActiveJobs()
                .onSuccess { _activeJobs.value = UiState.Success(it) }
                .onFailure {
                    if (it is AuthenticationException) {
                        attemptSessionRecovery(
                            onRecovered = { loadActiveJobs() },
                            onRecoveryFailed = { message -> _activeJobs.value = UiState.Error(message) }
                        )
                    } else {
                        _activeJobs.value = UiState.Error(it.message ?: "Failed to load jobs")
                    }
                }
        }
    }

    /** Load today's completed jobs for the summary section */
    private fun loadTodayCompletedJobs() {
        viewModelScope.launch {
            _todayCompletedJobs.value = UiState.Loading
            jobRepository.getCompletedJobs()
                .onSuccess { jobs ->
                    val todayPrefix = Clock.System.now().toString().take(10) // "YYYY-MM-DD"
                    val todayJobs = jobs.filter { job ->
                        (job.deliveredAt ?: job.completedAt ?: job.createdAt)
                            ?.startsWith(todayPrefix) == true
                    }
                    _todayCompletedJobs.value = UiState.Success(todayJobs)
                }
                .onFailure {
                    _todayCompletedJobs.value = UiState.Error(it.message ?: "Failed to load")
                }
        }
    }

    /** Check for incoming job requests (simulated polling) */
    private fun checkForIncomingJobs() {
        refreshIncomingOffers()
    }

    /** Load notifications */
    private fun loadNotifications() {
        viewModelScope.launch {
            notificationsRepository.getNotifications()
                .onSuccess { _notifications.value = it }
                .onFailure { _toastError.tryEmit(it.message ?: "Failed to load notifications") }
        }
    }

    private fun attemptSessionRecovery(
        onRecovered: () -> Unit,
        onRecoveryFailed: (String) -> Unit
    ) {
        if (authRecoveryInProgress) return
        authRecoveryInProgress = true
        viewModelScope.launch {
            authRepository.syncDriver()
                .onSuccess { onRecovered() }
                .onFailure {
                    val message = it.message ?: "Session expired. Please log in again."
                    _toastError.tryEmit(message)
                    onRecoveryFailed(message)
                }
            authRecoveryInProgress = false
        }
    }

    /** Accept incoming job request */
    fun acceptIncomingJob(jobId: String, onAccepted: ((DeliveryJob) -> Unit)? = null) {
        val job = _incomingJobs.value.firstOrNull { it.id == jobId } ?: return
        if (_acceptInFlight.value) return
        acceptingJobId = job.id
        suppressedIncomingJobIds.add(job.id)
        removeOffer(job.id)
        _acceptInFlight.value = true
        viewModelScope.launch {
            val result = jobRepository.acceptJob(job.id)
            result
                .onSuccess { acceptedJob ->
                    loadActiveJobs()
                    onAccepted?.invoke(acceptedJob)
                }
                .onFailure {
                    suppressedIncomingJobIds.remove(job.id)
                    mergeIncomingOffers(listOf(job))
                    _toastError.tryEmit(it.message ?: "Failed to accept job")
                }
            _acceptInFlight.value = false
            acceptingJobId = null
        }
    }

    /** Reject incoming job request */
    fun rejectIncomingJob(jobId: String) {
        val job = _incomingJobs.value.firstOrNull { it.id == jobId } ?: return
        // Track client-side immediately so polling doesn't re-show it before server records it
        suppressedIncomingJobIds.add(job.id)
        removeOffer(job.id)
        viewModelScope.launch {
            jobRepository.rejectJob(job.id)
                .onFailure { _toastError.tryEmit(it.message ?: "Failed to reject job") }
        }
    }

    /** Dismiss incoming job popup */
    fun dismissIncomingJob(jobId: String) {
        removeOffer(jobId)
    }
}
