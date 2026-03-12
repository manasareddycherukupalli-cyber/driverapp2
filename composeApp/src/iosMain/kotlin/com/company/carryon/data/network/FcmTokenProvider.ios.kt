package com.company.carryon.data.network

// iOS uses APNs, not FCM directly. FCM token registration
// would require Firebase iOS SDK integration in the future.
actual fun getFcmToken(): String? = null
