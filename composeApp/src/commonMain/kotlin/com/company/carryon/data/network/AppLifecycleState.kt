package com.company.carryon.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared foreground/background signal driven by platform lifecycle callbacks
 * (Android Activity onStart/onStop; iOS scene active/resign). Consumed by
 * HomeViewModel to stop the foreground ringtone when the app is backgrounded.
 */
object AppLifecycleState {
    private val _foregrounded = MutableStateFlow(true)
    val foregrounded: StateFlow<Boolean> = _foregrounded.asStateFlow()

    fun setForegrounded(value: Boolean) {
        _foregrounded.value = value
    }
}
