package com.company.carryon.data.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Shared event stream used by the platform-specific push notification handler (FcmService on Android)
 * to wake the in-app incoming jobs fetch path immediately.
 */
object IncomingJobSignal {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun signalIncomingJob() {
        _events.tryEmit(Unit)
    }
}
