package com.company.carryon

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.company.carryon.data.network.initLocationProvider
import com.company.carryon.data.network.DeepLinkHandler
import com.company.carryon.data.network.PayoutRefreshGuard
import com.company.carryon.data.network.handleLocationPermissionResult
import com.company.carryon.data.network.initTokenStorage
import com.company.carryon.data.network.savePushToken
import com.company.carryon.data.network.deliverNotificationPermissionResult
import com.company.carryon.data.network.initNotificationPermissionHost
import com.company.carryon.data.network.notificationPermissionLauncherTrigger
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.company.carryon.update.initAppUpdatePlatform

class MainActivity : ComponentActivity() {
    private var lastHandledDeepLink: String? = null
    private var lastHandledDeepLinkAt: Long = 0L

    private val permissionPrefs by lazy {
        getSharedPreferences("carryon_permissions", MODE_PRIVATE)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "POST_NOTIFICATIONS permission granted: $granted")
            deliverNotificationPermissionResult(granted)
        }

    private val requestStartupPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            Log.d("MainActivity", "Startup permissions result: $granted")
            val locationGranted = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (locationGranted) initLocationProvider(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PayoutRefreshGuard.reset()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
            navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE)
        )
        initTokenStorage(applicationContext)
        initAppUpdatePlatform(applicationContext)
        initLocationProvider(this)
        initNotificationPermissionHost(applicationContext)
        notificationPermissionLauncherTrigger = {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        createNotificationChannel()
        window.decorView.post {
            requestInitialPermissionsIfNeeded()
        }
        retrieveFcmToken()
        handleDeepLinkIntent(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleLocationPermissionResult(requestCode, grantResults)
    }

    private fun requestInitialPermissionsIfNeeded() {
        if (permissionPrefs.getBoolean(KEY_INITIAL_PERMISSIONS_REQUESTED, false)) return

        // Notification permission is requested from Compose (see NotificationPermissionDialog)
        // after the user opts in via our own themed screen, not automatically here.

        val permissionsToRequest = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.READ_CONTACTS)

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
                task.result?.takeIf { it.isNotBlank() }?.let { token ->
                    savePushToken(token)
                    Log.d("MainActivity", "FCM token retrieved")
                }
            } else {
                Log.w("MainActivity", "Failed to get FCM token", task.exception)
            }
        }
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri = intent?.dataString ?: return
        val now = System.currentTimeMillis()
        if (uri == lastHandledDeepLink && now - lastHandledDeepLinkAt < 1_000L) return
        lastHandledDeepLink = uri
        lastHandledDeepLinkAt = now
        DeepLinkHandler.handle(uri)
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
