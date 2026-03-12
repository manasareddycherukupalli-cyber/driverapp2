package com.example.drive_app.data.network

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming FCM push notifications and token refresh events.
 *
 * When a new job request push arrives (data payload type = "JOB_REQUEST"),
 * the Supabase Realtime subscription (if active in foreground) will already
 * have updated the UI. This service ensures the notification shows in the
 * system tray when the app is backgrounded.
 */
class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FcmService", "New FCM token: $token")
        // Store token locally so it can be sent to backend on next API call
        FcmTokenHolder.token = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FcmService", "FCM message received: ${message.data}")
        // When the app is in the foreground, Supabase Realtime handles the
        // job popup. The system notification (from the `notification` payload)
        // is automatically shown by FCM when the app is backgrounded.
    }
}

/**
 * Holds the current FCM token in memory. The token is sent to the backend
 * when the driver goes online (via the toggle-online flow).
 */
object FcmTokenHolder {
    var token: String? = null
}
