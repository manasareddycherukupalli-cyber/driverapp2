package com.company.carryon.data.network

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

private var pendingNotificationPermissionCallback: ((Boolean) -> Unit)? = null

/** Set by MainActivity, since the permission launcher must be registered on the Activity itself. */
var notificationPermissionLauncherTrigger: (() -> Unit)? = null

actual fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        onResult(true)
        return
    }
    val context = appContextForNotifications
    if (context != null && ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        onResult(true)
        return
    }
    val trigger = notificationPermissionLauncherTrigger
    if (trigger == null) {
        onResult(false)
        return
    }
    pendingNotificationPermissionCallback = onResult
    trigger()
}

fun deliverNotificationPermissionResult(granted: Boolean) {
    pendingNotificationPermissionCallback?.invoke(granted)
    pendingNotificationPermissionCallback = null
}

private var appContextForNotifications: android.content.Context? = null

fun initNotificationPermissionHost(context: android.content.Context) {
    appContextForNotifications = context.applicationContext
}
