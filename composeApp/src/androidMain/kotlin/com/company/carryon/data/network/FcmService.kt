package com.company.carryon.data.network

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.company.carryon.MainActivity
import com.company.carryon.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles incoming FCM push notifications and token refresh events.
 *
 * When a new job request push arrives (data payload type = "JOB_REQUEST"),
 * the Supabase Realtime subscription (if active in foreground) will already
 * have updated the UI. This service ensures the notification shows in the
 * system tray regardless of whether the app is in the foreground or background.
 */
class FcmService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FcmService", "New FCM token: $token")
        initTokenStorage(applicationContext)
        savePushToken(token)
        syncTokenToBackend(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FcmService", "FCM message received: ${message.data}")

        // Wake the in-app fetch path immediately when a ride request push arrives.
        if (message.data["type"] == "JOB_REQUEST") {
            initTokenStorage(applicationContext)
            markPendingIncomingJob()
            IncomingJobSignal.signalIncomingJob()
        }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("FcmService", "Notification permission not granted; skipping system notification")
            return
        }

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun syncTokenToBackend(token: String) {
        // onNewToken can fire while app process is cold-started.
        // Ensure token storage is initialized before withAuth() reads JWT.
        initTokenStorage(applicationContext)
        serviceScope.launch {
            runCatching {
                HttpClientFactory.client.put("/api/driver/profile/push-token") {
                    withAuth()
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(
                        mapOf(
                            "token" to token,
                            "platform" to currentPushPlatform(),
                            "deviceId" to getOrCreateDeviceId()
                        )
                    )
                }
            }.onSuccess {
                Log.d("FcmService", "FCM token synced to backend")
            }.onFailure { err ->
                Log.w("FcmService", "Failed to sync FCM token to backend", err)
            }
        }
    }
}
