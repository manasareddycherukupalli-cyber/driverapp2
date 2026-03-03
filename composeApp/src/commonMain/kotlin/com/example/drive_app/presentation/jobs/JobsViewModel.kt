package com.example.drive_app.presentation.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drive_app.data.model.*
import com.example.drive_app.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * JobsViewModel — Manages job listings, details, and job status updates.
 */
class JobsViewModel : ViewModel() {

    private val repository = ServiceLocator.jobRepository

    // Active jobs
    private val _activeJobs = MutableStateFlow<UiState<List<DeliveryJob>>>(UiState.Idle)
    val activeJobs: StateFlow<UiState<List<DeliveryJob>>> = _activeJobs.asStateFlow()

    // Scheduled jobs
    private val _scheduledJobs = MutableStateFlow<UiState<List<DeliveryJob>>>(UiState.Idle)
    val scheduledJobs: StateFlow<UiState<List<DeliveryJob>>> = _scheduledJobs.asStateFlow()

    // Completed jobs
    private val _completedJobs = MutableStateFlow<UiState<List<DeliveryJob>>>(UiState.Idle)
    val completedJobs: StateFlow<UiState<List<DeliveryJob>>> = _completedJobs.asStateFlow()

    // Selected job details
    private val _jobDetails = MutableStateFlow<UiState<DeliveryJob>>(UiState.Idle)
    val jobDetails: StateFlow<UiState<DeliveryJob>> = _jobDetails.asStateFlow()

    // Job action state
    private val _actionState = MutableStateFlow<UiState<DeliveryJob>>(UiState.Idle)
    val actionState: StateFlow<UiState<DeliveryJob>> = _actionState.asStateFlow()

    init {
        loadAllJobs()
    }

    fun loadAllJobs() {
        loadActiveJobs()
        loadScheduledJobs()
        loadCompletedJobs()
    }

    fun loadActiveJobs() {
        viewModelScope.launch {
            _activeJobs.value = UiState.Loading
            repository.getActiveJobs()
                .onSuccess { _activeJobs.value = UiState.Success(it) }
                .onFailure { _activeJobs.value = UiState.Error(it.message ?: "Failed to load") }
        }
    }

    fun loadScheduledJobs() {
        viewModelScope.launch {
            _scheduledJobs.value = UiState.Loading
            repository.getScheduledJobs()
                .onSuccess { _scheduledJobs.value = UiState.Success(it) }
                .onFailure { _scheduledJobs.value = UiState.Error(it.message ?: "Failed to load") }
        }
    }

    fun loadCompletedJobs() {
        viewModelScope.launch {
            _completedJobs.value = UiState.Loading
            repository.getCompletedJobs()
                .onSuccess { _completedJobs.value = UiState.Success(it) }
                .onFailure { _completedJobs.value = UiState.Error(it.message ?: "Failed to load") }
        }
    }

    fun loadJobDetails(jobId: String) {
        viewModelScope.launch {
            _jobDetails.value = UiState.Loading
            repository.getJobDetails(jobId)
                .onSuccess { _jobDetails.value = UiState.Success(it) }
                .onFailure { _jobDetails.value = UiState.Error(it.message ?: "Job not found") }
        }
    }

    fun acceptJob(jobId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.acceptJob(jobId)
                .onSuccess {
                    _actionState.value = UiState.Success(it)
                    loadActiveJobs()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to accept") }
        }
    }

    fun updateJobStatus(jobId: String, status: JobStatus) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            repository.updateJobStatus(jobId, status)
                .onSuccess {
                    _actionState.value = UiState.Success(it)
                    _jobDetails.value = UiState.Success(it)
                    loadActiveJobs()
                }
                .onFailure { _actionState.value = UiState.Error(it.message ?: "Failed to update") }
        }
    }
}
