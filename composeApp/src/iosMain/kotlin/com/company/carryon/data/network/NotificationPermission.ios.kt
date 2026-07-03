package com.company.carryon.data.network

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

@OptIn(ExperimentalForeignApi::class)
actual fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
    val options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(options) { granted, _ ->
        onResult(granted)
    }
}
