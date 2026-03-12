package com.example.drive_app.data.network

/**
 * Platform-specific FCM token retrieval.
 * Returns the current FCM token if available, or null on platforms
 * that don't support FCM (e.g. iOS, which uses APNs).
 */
expect fun getFcmToken(): String?
