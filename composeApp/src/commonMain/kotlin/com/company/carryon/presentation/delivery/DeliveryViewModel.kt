package com.company.carryon.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.data.network.LocationApi
import com.company.carryon.di.ServiceLocator
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MarkerColor
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

    // Proof of delivery state
    private val _proofState = MutableStateFlow<UiState<DeliveryJob>>(UiState.Idle)
    val proofState: StateFlow<UiState<DeliveryJob>> = _proofState.asStateFlow()

    // Map style URL from AWS Location
    private val _mapStyleUrl = MutableStateFlow("")
    val mapStyleUrl: StateFlow<String> = _mapStyleUrl.asStateFlow()

    // Route geometry for map polyline
    private val _routeGeometry = MutableStateFlow<List<LatLng>?>(null)
    val routeGeometry: StateFlow<List<LatLng>?> = _routeGeometry.asStateFlow()

    // Map markers for pickup/dropoff
    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    init {
        viewModelScope.launch {
            LocationApi.getMapConfig().onSuccess { config ->
                _mapStyleUrl.value = config.styleUrl
            }
        }
    }

    /** Load job details for active delivery */
    fun loadJob(jobId: String) {
        viewModelScope.launch {
            _currentJob.value = UiState.Loading
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

    /** Update delivery status step by step */
    fun updateStatus(jobId: String, newStatus: JobStatus) {
        viewModelScope.launch {
            jobRepository.updateJobStatus(jobId, newStatus)
                .onSuccess { job ->
                    _currentJob.value = UiState.Success(job)
                }
                .onFailure { /* Handle error */ }
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
            jobRepository.verifyPickupOtp(jobId, otp)
                .onSuccess { job ->
                    _currentJob.value = UiState.Success(job)
                    _otpVerifying.value = false
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

    /** Submit proof of delivery */
    fun submitProof(jobId: String, photoTaken: Boolean, otpCode: String, recipientName: String) {
        viewModelScope.launch {
            _proofState.value = UiState.Loading
            val proof = ProofOfDelivery(
                photoUrl = if (photoTaken) "photo_${jobId}.jpg" else null,
                otpCode = otpCode.ifBlank { null },
                recipientName = recipientName.ifBlank { null },
                deliveredAt = null // Server sets the timestamp
            )
            jobRepository.submitProof(jobId, proof)
                .onSuccess { _proofState.value = UiState.Success(it) }
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
            JobStatus.ACCEPTED -> "Start Navigation →"
            JobStatus.HEADING_TO_PICKUP -> "Arrived at Pickup 📍"
            JobStatus.ARRIVED_AT_PICKUP -> "Picked Up Package 📦"
            JobStatus.PICKED_UP -> "Start Delivery 🚚"
            JobStatus.IN_TRANSIT -> "Arrived at Drop-off 🏁"
            JobStatus.ARRIVED_AT_DROP -> "Complete Delivery ✓"
            else -> "Done"
        }
    }
}
