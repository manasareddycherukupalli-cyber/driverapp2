package com.company.carryon

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.company.carryon.data.network.FcmTokenHolder
import com.company.carryon.data.network.initTokenStorage
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initTokenStorage(applicationContext)
        createNotificationChannel()
        retrieveFcmToken()

        setContent {
            App()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "job_requests",
                "Job Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming delivery job requests"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun retrieveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FcmTokenHolder.token = task.result
                Log.d("MainActivity", "FCM token retrieved: ${task.result}")
            } else {
                Log.w("MainActivity", "Failed to get FCM token", task.exception)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}