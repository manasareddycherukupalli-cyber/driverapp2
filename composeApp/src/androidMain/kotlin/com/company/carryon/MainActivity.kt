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

    private val permissionPrefs by lazy {
        getSharedPreferences("carryon_permissions", MODE_PRIVATE)
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "POST_NOTIFICATIONS permission granted: $granted")
        }

    private val requestStartupPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            Log.d("MainActivity", "Startup permissions result: $granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initTokenStorage(applicationContext)
        initLocationProvider(applicationContext)
        createNotificationChannel()
        window.decorView.post {
            requestInitialPermissionsIfNeeded()
        }
        retrieveFcmToken()

        setContent {
            App()
        }
    }

    private fun requestInitialPermissionsIfNeeded() {
        if (permissionPrefs.getBoolean(KEY_INITIAL_PERMISSIONS_REQUESTED, false)) return

        requestNotificationPermissionIfNeeded()

        val permissionsToRequest = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.READ_CONTACTS)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        val missingPermissions = permissionsToRequest.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestStartupPermissions.launch(missingPermissions.toTypedArray())
        }

        permissionPrefs.edit()
            .putBoolean(KEY_INITIAL_PERMISSIONS_REQUESTED, true)
            .apply()
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

    private companion object {
        const val KEY_INITIAL_PERMISSIONS_REQUESTED = "initial_permissions_requested"
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
