package com.company.carryon.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.data.network.RealtimeJobService
import com.company.carryon.data.network.getLastKnownLocation
import com.company.carryon.data.network.getFcmToken
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

    // Location tracking job
    private var locationTrackingJob: Job? = null

    // Notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadDashboardData()
        collectRealtimeJobs()
        initOnlineStatusFromDriver()
    }

    /** Initialize and continuously sync online status from the driver's server-side state */
    private fun initOnlineStatusFromDriver() {
        viewModelScope.launch {
            authRepository.currentDriver
                .filterNotNull()
                .distinctUntilChanged { old, new -> old.isOnline == new.isOnline }
                .collect { driver ->
                    _isOnline.value = driver.isOnline
                    if (driver.isOnline) {
                        startRealtimeSubscription()
                        registerFcmToken()
                        startLocationTracking()
                    } else {
                        stopRealtimeSubscription()
                        stopLocationTracking()
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
            authRepository.toggleOnline(newStatus)
                .onSuccess {
                    _isOnline.value = newStatus
                    if (newStatus) {
                        checkForIncomingJobs()
                        startRealtimeSubscription()
                        registerFcmToken()
                        startLocationTracking()
                    } else {
                        stopRealtimeSubscription()
                        stopLocationTracking()
                    }
                }
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

    /** Start sending GPS location to the backend every 30 seconds */
    private fun startLocationTracking() {
        if (locationTrackingJob?.isActive == true) return
        locationTrackingJob = viewModelScope.launch {
            while (isActive) {
                getLastKnownLocation()?.let { (lat, lng) ->
                    authRepository.updateLocation(lat, lng)
                }
                delay(30_000L)
            }
        }
    }

    /** Stop the location tracking loop */
    private fun stopLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
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
        }
    }

    /** Reject incoming job request */
    fun rejectIncomingJob() {
        val job = _incomingJob.value ?: return
        viewModelScope.launch {
            jobRepository.rejectJob(job.id)
                .onSuccess { _incomingJob.value = null }
        }
    }

    /** Dismiss incoming job popup */
    fun dismissIncomingJob() {
        _incomingJob.value = null
    }
}
