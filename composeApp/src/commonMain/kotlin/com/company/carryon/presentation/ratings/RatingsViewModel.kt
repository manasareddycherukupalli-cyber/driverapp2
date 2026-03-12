package com.company.carryon.presentation.ratings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * RatingsViewModel — Manages driver ratings and customer feedback.
 */
class RatingsViewModel : ViewModel() {

    private val repository = ServiceLocator.ratingsRepository

    private val _ratingInfo = MutableStateFlow<UiState<RatingInfo>>(UiState.Idle)
    val ratingInfo: StateFlow<UiState<RatingInfo>> = _ratingInfo.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val submitState: StateFlow<UiState<Boolean>> = _submitState.asStateFlow()

    init {
        loadRatings()
    }

    fun loadRatings() {
        viewModelScope.launch {
            _ratingInfo.value = UiState.Loading
            repository.getRatingInfo()
                .onSuccess { _ratingInfo.value = UiState.Success(it) }
                .onFailure { _ratingInfo.value = UiState.Error(it.message ?: "Failed to load ratings") }
        }
    }

    fun rateCustomer(jobId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            _submitState.value = UiState.Loading
            repository.rateCustomer(CustomerRating(jobId, rating, comment))
                .onSuccess { _submitState.value = UiState.Success(true) }
                .onFailure { _submitState.value = UiState.Error(it.message ?: "Failed to submit") }
        }
    }
}
