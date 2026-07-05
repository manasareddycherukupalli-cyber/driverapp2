package com.company.carryon.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Observable, persisted on/off preference for the foreground job-request
 * ringtone. Backed by [saveJobRingEnabled]/[getJobRingEnabled]. Shared so the
 * Settings toggle and a live HomeViewModel stay in sync.
 */
object JobRingPreference {
    private val _enabled = MutableStateFlow(getJobRingEnabled())
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun set(value: Boolean) {
        saveJobRingEnabled(value)
        _enabled.value = value
    }
}
