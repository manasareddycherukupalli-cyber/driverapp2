package com.company.carryon.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
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

/**
 * HomeViewModel — Manages home dashboard state.
 * Handles online/offline toggle, earnings summary, active jobs, and notifications.
 */
class HomeViewModel : ViewModel() {

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

    // Incoming job request
    private val _incomingJob = MutableStateFlow<DeliveryJob?>(null)
    val incomingJob: StateFlow<DeliveryJob?> = _incomingJob.asStateFlow()

    // Driver location for map display
    private val _driverLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val driverLocation: StateFlow<Pair<Double, Double>?> = _driverLocation.asStateFlow()

    // Map style URL
    private val _mapStyleUrl = MutableStateFlow("")
    val mapStyleUrl: StateFlow<String> = _mapStyleUrl.asStateFlow()

    // Location tracking job
    private var locationTrackingJob: Job? = null

    // Incoming job polling job
    private var jobPollingJob: Job? = null

    // Client-side rejected job IDs — prevents rejected requests from reappearing before server catches up
    private val rejectedJobIds = mutableSetOf<String>()

    // Flag to prevent initOnlineStatusFromDriver from overriding _isOnline during a toggle request
    private var isTogglingOnline = false

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
                    if (driver.isOnline) {
                        startRealtimeSubscription()
                        registerFcmToken()
                        startLocationTracking()
                        startJobPolling()
                    } else {
                        stopRealtimeSubscription()
                        stopLocationTracking()
                        stopJobPolling()
                    }
                }
        }
    }

    /** Load all dashboard data */
    fun loadDashboardData() {
        loadEarnings()
        loadActiveJobs()
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
                        startRealtimeSubscription()
                        registerFcmToken()
                        startLocationTracking()
                        startJobPolling()
                    } else {
                        stopRealtimeSubscription()
                        stopLocationTracking()
                        stopJobPolling()
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

    private fun startRealtimeSubscription() {
        viewModelScope.launch {
            RealtimeJobService.startListening(viewModelScope)
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

    /** Poll /incoming every 6 seconds as a fallback in case Supabase Realtime misses events.
     *  Also fires immediately when an FCM JOB_REQUEST push signals a pending check. */
    private fun startJobPolling() {
        if (jobPollingJob?.isActive == true) return
        jobPollingJob = viewModelScope.launch {
            while (isActive) {
                // Check immediately if FCM signalled a job, otherwise wait 6s
                val fcmTriggered = IncomingJobSignal.pendingCheck
                if (!fcmTriggered) delay(6_000L)
                IncomingJobSignal.pendingCheck = false

                if (_incomingJob.value == null) {
                    jobRepository.getIncomingRequest()
                        .onSuccess { job ->
                            if (job != null && job.id !in rejectedJobIds) {
                                _incomingJob.value = job
                            }
                        }
                }
            }
        }
    }

    private fun stopJobPolling() {
        jobPollingJob?.cancel()
        jobPollingJob = null
    }

    private fun collectRealtimeJobs() {
        viewModelScope.launch {
            RealtimeJobService.incomingJobs.collect { job ->
                // Only show if no job popup is currently displayed
                if (_incomingJob.value == null) {
                    _incomingJob.value = job
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
        stopJobPolling()
        viewModelScope.launch {
            RealtimeJobService.stopListening()
        }
    }

    /** Load today's earnings summary */
    private fun loadEarnings() {
        viewModelScope.launch {
            _earningsSummary.value = UiState.Loading
            earningsRepository.getEarningsSummary()
                .onSuccess { _earningsSummary.value = UiState.Success(it) }
                .onFailure { _earningsSummary.value = UiState.Error(it.message ?: "Failed to load earnings") }
        }
    }

    /** Load active delivery jobs */
    private fun loadActiveJobs() {
        viewModelScope.launch {
            _activeJobs.value = UiState.Loading
            jobRepository.getActiveJobs()
                .onSuccess { _activeJobs.value = UiState.Success(it) }
                .onFailure { _activeJobs.value = UiState.Error(it.message ?: "Failed to load jobs") }
        }
    }

    /** Check for incoming job requests (simulated polling) */
    private fun checkForIncomingJobs() {
        viewModelScope.launch {
            jobRepository.getIncomingRequest()
                .onSuccess { _incomingJob.value = it }
        }
    }

    /** Load notifications */
    private fun loadNotifications() {
        viewModelScope.launch {
            notificationsRepository.getNotifications()
                .onSuccess { _notifications.value = it }
                .onFailure { _toastError.tryEmit(it.message ?: "Failed to load notifications") }
        }
    }

    /** Accept incoming job request */
    fun acceptIncomingJob() {
        val job = _incomingJob.value ?: return
        viewModelScope.launch {
            jobRepository.acceptJob(job.id)
                .onSuccess {
                    _incomingJob.value = null
                    loadActiveJobs()
                }
                .onFailure { _toastError.tryEmit(it.message ?: "Failed to accept job") }
        }
    }

    /** Reject incoming job request */
    fun rejectIncomingJob() {
        val job = _incomingJob.value ?: return
        // Track client-side immediately so polling doesn't re-show it before server records it
        rejectedJobIds.add(job.id)
        _incomingJob.value = null
        viewModelScope.launch {
            jobRepository.rejectJob(job.id)
                .onFailure { _toastError.tryEmit(it.message ?: "Failed to reject job") }
        }
    }

    /** Dismiss incoming job popup */
    fun dismissIncomingJob() {
        _incomingJob.value = null
    }
}
