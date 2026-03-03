package com.example.drive_app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drive_app.data.model.*
import com.example.drive_app.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ProfileViewModel — Manages driver profile data and updates.
 */
class ProfileViewModel : ViewModel() {

    private val authRepository = ServiceLocator.authRepository

    val currentDriver: StateFlow<Driver?> = authRepository.currentDriver
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _updateState = MutableStateFlow<UiState<Driver>>(UiState.Idle)
    val updateState: StateFlow<UiState<Driver>> = _updateState.asStateFlow()

    fun updateProfile(name: String, email: String, emergencyContact: String) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val current = currentDriver.value ?: return@launch
            val updated = current.copy(
                name = name,
                email = email,
                emergencyContact = emergencyContact
            )
            authRepository.updateProfile(updated)
                .onSuccess { _updateState.value = UiState.Success(it) }
                .onFailure { _updateState.value = UiState.Error(it.message ?: "Update failed") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
