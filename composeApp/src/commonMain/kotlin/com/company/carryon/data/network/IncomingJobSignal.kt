package com.company.carryon.data.network

/**
 * Shared flag set by the platform-specific push notification handler (FcmService on Android)
 * when a JOB_REQUEST push arrives. The HomeViewModel polling loop checks and clears this flag
 * to trigger an immediate incoming-job check without waiting for the next poll interval.
 */
object IncomingJobSignal {
    @kotlin.concurrent.Volatile var pendingCheck: Boolean = false
}
