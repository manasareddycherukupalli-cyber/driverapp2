package com.example.drive_app.data.model

/**
 * Generic UI state wrapper for handling loading, success, and error states.
 * Used across all ViewModels for consistent state management.
 */
sealed class UiState<out T> {
    /** Initial idle state before any action */
    data object Idle : UiState<Nothing>()

    /** Loading state while waiting for data */
    data object Loading : UiState<Nothing>()

    /** Success state containing the result data */
    data class Success<T>(val data: T) : UiState<T>()

    /** Error state with a user-friendly message */
    data class Error(val message: String) : UiState<Nothing>()
}

/**
 * Extension to check if the state is currently loading.
 */
val <T> UiState<T>.isLoading: Boolean
    get() = this is UiState.Loading

/**
 * Extension to get the data if available.
 */
val <T> UiState<T>.dataOrNull: T?
    get() = (this as? UiState.Success)?.data

/**
 * Extension to get error message if in error state.
 */
val <T> UiState<T>.errorMessage: String?
    get() = (this as? UiState.Error)?.message
