package com.company.carryon.data.network

/**
 * Triggers the platform's native notification-permission prompt.
 * Call this only after the user has already opted in via our own themed
 * pre-permission screen — the native prompt itself can't be restyled.
 */
expect fun requestNotificationPermission(onResult: (Boolean) -> Unit)
