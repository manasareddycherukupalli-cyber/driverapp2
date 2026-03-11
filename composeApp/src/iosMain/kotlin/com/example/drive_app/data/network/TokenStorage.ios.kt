package com.example.drive_app.data.network

import platform.Foundation.NSUserDefaults

private const val KEY_TOKEN = "jwt_token"

actual fun saveToken(token: String) {
    NSUserDefaults.standardUserDefaults.setObject(token, forKey = KEY_TOKEN)
}

actual fun getToken(): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(KEY_TOKEN)
}

actual fun clearToken() {
    NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_TOKEN)
}
