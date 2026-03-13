package com.company.carryon.data.network

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.company.carryon.MainActivity
import com.company.carryon.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming FCM push notifications and token refresh events.
 *
 * When a new job request push arrives (data payload type = "JOB_REQUEST"),
 * the Supabase Realtime subscription (if active in foreground) will already
 * have updated the UI. This service ensures the notification shows in the
 * system tray regardless of whether the app is in the foreground or background.
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

        // Extract title/body from notification payload, falling back to data payload
        val title = message.notification?.title
            ?: message.data["title"]
            ?: return
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "carryon_notifications")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}

/**
 * Holds the current FCM token in memory. The token is sent to the backend
 * when the driver goes online (via the toggle-online flow).
 */
object FcmTokenHolder {
    var token: String? = null
}
