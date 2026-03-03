package com.example.drive_app.presentation.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drive_app.data.model.*
import com.example.drive_app.di.ServiceLocator
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

    /** Load job details for active delivery */
    fun loadJob(jobId: String) {
        viewModelScope.launch {
            _currentJob.value = UiState.Loading
            jobRepository.getJobDetails(jobId)
                .onSuccess { _currentJob.value = UiState.Success(it) }
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

    /** Submit proof of delivery */
    fun submitProof(jobId: String, photoTaken: Boolean, otpCode: String, recipientName: String) {
        viewModelScope.launch {
            _proofState.value = UiState.Loading
            val proof = ProofOfDelivery(
                photoUrl = if (photoTaken) "photo_${jobId}_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}.jpg" else null,
                otpCode = otpCode.ifBlank { null },
                recipientName = recipientName.ifBlank { null },
                deliveredAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
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
            JobStatus.ARRIVED_AT_PICKUP -> JobStatus.PICKED_UP
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
