package com.company.carryon.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.data.network.DriverUploadApi
import com.company.carryon.data.network.LocationApi
import com.company.carryon.data.network.getLastKnownLocation
import com.company.carryon.di.ServiceLocator
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.navigation.deliveryFlowScreens
import com.company.carryon.presentation.navigation.mapJobStatusToResumeScreen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * DeliveryViewModel — Manages active delivery flow state.
 * Handles step-by-step delivery status updates and proof of delivery.
 */
class DeliveryViewModel : ViewModel() {

    private val jobRepository = ServiceLocator.jobRepository

    // Current delivery job
    private val _currentJob = MutableStateFlow<UiState<DeliveryJob>>(UiState.Idle)
    val currentJob: StateFlow<UiState<DeliveryJob>> = _currentJob.asStateFlow()

    private val _allowedCommands = MutableStateFlow<List<DeliveryLifecycleCommand>>(emptyList())
    val allowedCommands: StateFlow<List<DeliveryLifecycleCommand>> = _allowedCommands.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<DeliveryNavigationEvent>(extraBufferCapacity = 8)
    val navigationEvents: SharedFlow<DeliveryNavigationEvent> = _navigationEvents.asSharedFlow()

    // Proof of delivery state
    private val _proofState = MutableStateFlow<UiState<DeliveryJob>>(UiState.Idle)
    val proofState: StateFlow<UiState<DeliveryJob>> = _proofState.asStateFlow()

    // Photo upload state scoped to the job it was captured for.
    private val _photoUploadState = MutableStateFlow<UiState<ProofPhotoUpload>>(UiState.Idle)
    val photoUploadState: StateFlow<UiState<ProofPhotoUpload>> = _photoUploadState.asStateFlow()

    // Delivery OTP request state
    private val _deliveryOtpState = MutableStateFlow<UiState<DeliveryOtpInfo>>(UiState.Idle)
    val deliveryOtpState: StateFlow<UiState<DeliveryOtpInfo>> = _deliveryOtpState.asStateFlow()

    private val _startDeliveryState = MutableStateFlow<UiState<DeliveryJob>>(UiState.Idle)
    val startDeliveryState: StateFlow<UiState<DeliveryJob>> = _startDeliveryState.asStateFlow()

    private val _statusUpdateState = MutableStateFlow<UiState<DeliveryJob>>(UiState.Idle)
    val statusUpdateState: StateFlow<UiState<DeliveryJob>> = _statusUpdateState.asStateFlow()

    // Cancel job state
    private val _cancelState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val cancelState: StateFlow<UiState<Boolean>> = _cancelState.asStateFlow()

    private val _cancelCompletedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val cancelCompletedEvents: SharedFlow<Unit> = _cancelCompletedEvents.asSharedFlow()

    // Route geometry for map polyline
    private val _routeGeometry = MutableStateFlow<List<LatLng>?>(null)
    val routeGeometry: StateFlow<List<LatLng>?> = _routeGeometry.asStateFlow()

    // Map markers for pickup/dropoff
    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    private fun lifecyclePayload(
        otp: String? = null,
        forceResend: Boolean = false,
        proof: DeliveryLifecycleProof? = null
    ): DeliveryLifecycleCommandPayload {
        val location = getLastKnownLocation()?.let { (lat, lng) ->
            DeliveryLifecycleLocation(latitude = lat, longitude = lng)
        }
        return DeliveryLifecycleCommandPayload(
            otp = otp,
            forceResend = forceResend,
            proof = proof,
            location = location
        )
    }

    private suspend fun executeLifecycle(
        jobId: String,
        command: DeliveryLifecycleCommand,
        payload: DeliveryLifecycleCommandPayload = lifecyclePayload()
    ): Result<DeliveryLifecycleResult> {
        return jobRepository.executeLifecycleCommand(jobId, command, payload)
            .onSuccess { result ->
                applyLifecycleResult(result)
            }
    }

    private fun applyLifecycleResult(result: DeliveryLifecycleResult) {
        _currentJob.value = UiState.Success(result.job)
        _allowedCommands.value = result.allowedCommands
    }

    private fun emitNavigation(screen: Screen, clearStack: Boolean = false) {
        _navigationEvents.tryEmit(DeliveryNavigationEvent.Navigate(screen, clearStack))
    }

    fun redirectIfCurrentScreenInvalid(currentScreen: Screen, job: DeliveryJob) {
        if (currentScreen !in deliveryFlowScreens) return
        if (currentScreen in validDeliveryScreensForStatus(job.status)) return
        val expectedScreen = mapJobStatusToResumeScreen(job.status) ?: return
        emitNavigation(expectedScreen, clearStack = true)
    }

    fun canRun(command: DeliveryLifecycleCommand, job: DeliveryJob): Boolean {
        return canRunDeliveryCommand(command, job.status, _allowedCommands.value)
    }

    fun canCancelBeforePickup(job: DeliveryJob): Boolean {
        return canRun(DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP, job)
    }

    /** Load job details for active delivery */
    fun loadJob(jobId: String) {
        viewModelScope.launch {
            _currentJob.value = UiState.Loading
            _allowedCommands.value = emptyList()
            jobRepository.getJobDetails(jobId)
                .onSuccess { job ->
                    _currentJob.value = UiState.Success(job)
                    // Load route and markers from job coordinates
                    val pickup = job.pickup
                    val dropoff = job.dropoff
                    if (pickup.latitude != 0.0 && dropoff.latitude != 0.0) {
                        _markers.value = listOf(
                            MapMarker("pickup", pickup.latitude, pickup.longitude, "Pickup", MarkerColor.GREEN),
                            MapMarker("dropoff", dropoff.latitude, dropoff.longitude, "Drop-off", MarkerColor.RED)
                        )
                        LocationApi.calculateRoute(
                            pickup.latitude, pickup.longitude,
                            dropoff.latitude, dropoff.longitude
                        ).onSuccess { result ->
                            _routeGeometry.value = result.geometry
                        }
                    }
                }
                .onFailure { _currentJob.value = UiState.Error(it.message ?: "Failed to load job") }
        }
    }

    fun prepareProofOfDelivery(jobId: String) {
        val currentUpload = (_photoUploadState.value as? UiState.Success)?.data
        if (currentUpload?.jobId != jobId) {
            _photoUploadState.value = UiState.Idle
        }
        _proofState.value = UiState.Idle
    }

    /** Update delivery status step by step */
    fun updateStatus(jobId: String, newStatus: JobStatus) {
        viewModelScope.launch {
            val command = when (newStatus) {
                JobStatus.ARRIVED_AT_PICKUP -> DeliveryLifecycleCommand.ARRIVE_PICKUP
                JobStatus.IN_TRANSIT -> DeliveryLifecycleCommand.START_DELIVERY
                JobStatus.ARRIVED_AT_DROP -> DeliveryLifecycleCommand.ARRIVE_DROP
                else -> null
            }
            if (command == null) {
                jobRepository.getJobDetails(jobId)
                    .onSuccess { job -> _currentJob.value = UiState.Success(job) }
                    .onFailure { _currentJob.value = UiState.Error(it.message ?: "Failed to refresh job") }
                return@launch
            }
            _statusUpdateState.value = UiState.Loading
            executeLifecycle(jobId, command)
                .onSuccess { result ->
                    _statusUpdateState.value = UiState.Success(result.job)
                    mapJobStatusToResumeScreen(result.job.status)?.let { screen ->
                        emitNavigation(
                            screen,
                            clearStack = command == DeliveryLifecycleCommand.ARRIVE_PICKUP
                        )
                    }
                }
                .onFailure {
                    val message = it.message ?: "Failed to update status"
                    _statusUpdateState.value = UiState.Error(message)
                    _currentJob.value = UiState.Error(message)
                }
        }
    }

    fun startDelivery(jobId: String) {
        viewModelScope.launch {
            _startDeliveryState.value = UiState.Loading
            executeLifecycle(jobId, DeliveryLifecycleCommand.START_DELIVERY)
                .onSuccess { result ->
                    _startDeliveryState.value = UiState.Success(result.job)
                    emitNavigation(Screen.InTransitNavigation)
                }
                .onFailure { error ->
                    _startDeliveryState.value = UiState.Error(error.message ?: "Failed to start delivery")
                }
        }
    }

    // OTP verification error message
    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError.asStateFlow()

    private val _otpVerifying = MutableStateFlow(false)
    val otpVerifying: StateFlow<Boolean> = _otpVerifying.asStateFlow()

    /** Verify pickup OTP entered by driver */
    fun verifyPickupOtp(jobId: String, otp: String) {
        viewModelScope.launch {
            _otpError.value = null
            _otpVerifying.value = true
            executeLifecycle(
                jobId,
                DeliveryLifecycleCommand.VERIFY_PICKUP_OTP,
                lifecyclePayload(otp = otp)
            )
                .onSuccess {
                    _otpVerifying.value = false
                    emitNavigation(Screen.StartDelivery)
                }
                .onFailure { e ->
                    _otpError.value = e.message ?: "Invalid OTP"
                    _otpVerifying.value = false
                }
        }
    }

    fun clearOtpError() {
        _otpError.value = null
    }

    /** Generate/send drop-off OTP to recipient (backend-controlled) */
    fun requestDeliveryOtp(jobId: String, forceResend: Boolean = false) {
        viewModelScope.launch {
            _deliveryOtpState.value = UiState.Loading
            executeLifecycle(
                jobId,
                DeliveryLifecycleCommand.REQUEST_DROP_OTP,
                lifecyclePayload(forceResend = forceResend)
            )
                .onSuccess { _deliveryOtpState.value = UiState.Success(it.otpInfo ?: DeliveryOtpInfo()) }
                .onFailure { _deliveryOtpState.value = UiState.Error(it.message ?: "Failed to request delivery OTP") }
        }
    }

    /** Cancel the job and re-queue it for another driver */
    fun cancelJob(jobId: String) {
        viewModelScope.launch {
            val job = (_currentJob.value as? UiState.Success)?.data
            if (job != null && !canCancelBeforePickup(job)) {
                _cancelState.value = UiState.Error("Cannot cancel after picking up the package")
                return@launch
            }
            _cancelState.value = UiState.Loading
            jobRepository.executeLifecycleCommand(
                jobId,
                DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP,
                lifecyclePayload()
            )
                .onSuccess {
                    _cancelState.value = UiState.Success(true)
                    _cancelCompletedEvents.tryEmit(Unit)
                }
                .onFailure { _cancelState.value = UiState.Error(it.message ?: "Failed to cancel job") }
        }
    }

    /** Upload proof photo immediately after capture */
    fun uploadPhoto(jobId: String, photoBytes: ByteArray) {
        // Set Loading synchronously on the calling (main) thread so the overlay is visible
        // in the same recomposition frame as the captured image — no flickering gap.
        _photoUploadState.value = UiState.Loading
        viewModelScope.launch {
            DriverUploadApi.uploadProofImage(photoBytes)
                .onSuccess { url ->
                    _photoUploadState.value = UiState.Success(ProofPhotoUpload(jobId, url))
                }
                .onFailure { error ->
                    _photoUploadState.value = UiState.Error(error.message ?: "Failed to upload photo")
                }
        }
    }

    /** Submit proof of delivery */
    fun submitProof(jobId: String, photoBytes: ByteArray?, otpCode: String, recipientName: String) {
        viewModelScope.launch {
            _proofState.value = UiState.Loading
            val uploadedPhotoUrl = when (val state = _photoUploadState.value) {
                is UiState.Success -> state.data.takeIf { it.jobId == jobId }?.url
                    ?: run {
                        _proofState.value = UiState.Error("Photo is required")
                        return@launch
                    }
                else -> {
                    if (photoBytes == null) {
                        _proofState.value = UiState.Error("Photo is required")
                        return@launch
                    }
                    DriverUploadApi.uploadProofImage(photoBytes)
                        .getOrElse { error ->
                            _proofState.value = UiState.Error(error.message ?: "Failed to upload proof photo")
                            return@launch
                        }
                }
            }
            val proof = ProofOfDelivery(
                photoUrl = uploadedPhotoUrl,
                otpCode = otpCode.ifBlank { null },
                recipientName = recipientName.ifBlank { null },
                deliveredAt = null // Server sets the timestamp
            )
            executeLifecycle(
                jobId,
                DeliveryLifecycleCommand.COMPLETE_DELIVERY,
                lifecyclePayload(
                    otp = proof.otpCode,
                    proof = DeliveryLifecycleProof(
                        photoUrl = proof.photoUrl,
                        recipientName = proof.recipientName
                    )
                )
            )
                .onSuccess {
                    _proofState.value = UiState.Success(it.job)
                    emitNavigation(Screen.DeliveryComplete)
                }
                .onFailure { _proofState.value = UiState.Error(it.message ?: "Failed to submit proof") }
        }
    }

    /** Get next status in delivery flow */
    fun getNextStatus(currentStatus: JobStatus): JobStatus? {
        return when (currentStatus) {
            JobStatus.ACCEPTED -> JobStatus.HEADING_TO_PICKUP
            JobStatus.HEADING_TO_PICKUP -> JobStatus.ARRIVED_AT_PICKUP
            JobStatus.ARRIVED_AT_PICKUP -> null // OTP verification required
            JobStatus.PICKED_UP -> JobStatus.IN_TRANSIT
            JobStatus.IN_TRANSIT -> JobStatus.ARRIVED_AT_DROP
            JobStatus.ARRIVED_AT_DROP -> JobStatus.DELIVERED
            else -> null
        }
    }

    /** Get action text for current delivery step */
    fun getActionText(status: JobStatus): String {
        return when (status) {
            JobStatus.ACCEPTED -> "Head to Pickup →"
            JobStatus.HEADING_TO_PICKUP -> "Arrived at Pickup 📍"
            JobStatus.ARRIVED_AT_PICKUP -> "Picked Up Package 📦"
            JobStatus.PICKED_UP -> "Start Delivery 🚚"
            JobStatus.IN_TRANSIT -> "Arrived at Drop-off 🏁"
            JobStatus.ARRIVED_AT_DROP -> "Complete Delivery ✓"
            else -> "Done"
        }
    }
}

data class ProofPhotoUpload(
    val jobId: String,
    val url: String
)

sealed interface DeliveryNavigationEvent {
    data class Navigate(
        val screen: Screen,
        val clearStack: Boolean = false
    ) : DeliveryNavigationEvent
}

fun validDeliveryScreensForStatus(status: JobStatus): Set<Screen> = when (status) {
    JobStatus.ACCEPTED, JobStatus.HEADING_TO_PICKUP -> setOf(Screen.MapNavigation)
    JobStatus.ARRIVED_AT_PICKUP -> setOf(Screen.ActiveDelivery, Screen.PickupInstructions)
    JobStatus.PICKED_UP -> setOf(Screen.StartDelivery)
    JobStatus.IN_TRANSIT -> setOf(Screen.InTransitNavigation)
    JobStatus.ARRIVED_AT_DROP -> setOf(Screen.ArrivedAtDrop, Screen.ProofOfDelivery)
    JobStatus.DELIVERED -> setOf(Screen.DeliveryComplete, Screen.JobReceipt)
    else -> emptySet()
}

fun fallbackDeliveryCommandsForStatus(status: JobStatus): Set<DeliveryLifecycleCommand> = when (status) {
    JobStatus.ACCEPTED, JobStatus.HEADING_TO_PICKUP -> setOf(
        DeliveryLifecycleCommand.ARRIVE_PICKUP,
        DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP
    )
    JobStatus.ARRIVED_AT_PICKUP -> setOf(
        DeliveryLifecycleCommand.VERIFY_PICKUP_OTP,
        DeliveryLifecycleCommand.CANCEL_BEFORE_PICKUP
    )
    JobStatus.PICKED_UP -> setOf(DeliveryLifecycleCommand.START_DELIVERY)
    JobStatus.IN_TRANSIT -> setOf(DeliveryLifecycleCommand.ARRIVE_DROP)
    JobStatus.ARRIVED_AT_DROP -> setOf(
        DeliveryLifecycleCommand.REQUEST_DROP_OTP,
        DeliveryLifecycleCommand.COMPLETE_DELIVERY
    )
    else -> emptySet()
}

fun canRunDeliveryCommand(
    command: DeliveryLifecycleCommand,
    status: JobStatus,
    serverAllowedCommands: List<DeliveryLifecycleCommand> = emptyList()
): Boolean {
    return if (serverAllowedCommands.isNotEmpty()) {
        command in serverAllowedCommands
    } else {
        command in fallbackDeliveryCommandsForStatus(status)
    }
}
