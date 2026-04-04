package com.company.carryon

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.company.carryon.data.network.FcmTokenHolder
import com.company.carryon.data.network.initLocationProvider
import com.company.carryon.data.network.initTokenStorage
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "POST_NOTIFICATIONS permission granted: $granted")
        }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            Log.d("MainActivity", "Location permissions: fine=$fineGranted, coarse=$coarseGranted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initTokenStorage(applicationContext)
        initLocationProvider(applicationContext)
        createNotificationChannel()
        requestNotificationPermissionIfNeeded()
        requestLocationPermissionIfNeeded()
        retrieveFcmToken()

        setContent {
            App()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!alreadyGranted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted) {
            requestLocationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val jobChannel = NotificationChannel(
                "job_requests",
                "Job Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming delivery job requests"
            }
            manager.createNotificationChannel(jobChannel)

            val generalChannel = NotificationChannel(
                "carryon_notifications",
                "CarryOn Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General notifications from CarryOn"
            }
            manager.createNotificationChannel(generalChannel)
        }
    }

    private fun retrieveFcmToken() {
        val firebaseAvailable = try {
            FirebaseApp.getApps(this).isNotEmpty()
        } catch (_: Exception) {
            false
        }
        if (!firebaseAvailable) {
            Log.w("MainActivity", "Firebase is not configured (google-services.json missing). Skipping FCM token retrieval.")
            return
        }

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
