package com.company.carryon.data.network

actual fun getFcmToken(): String? = FcmTokenHolder.token
