package com.company.carryon.data.network

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "driver_app_prefs"
private const val KEY_TOKEN = "jwt_token"

private lateinit var prefs: SharedPreferences

fun initTokenStorage(context: Context) {
    prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

actual fun saveToken(token: String) {
    prefs.edit().putString(KEY_TOKEN, token).apply()
}

actual fun getToken(): String? {
    return prefs.getString(KEY_TOKEN, null)
}

actual fun clearToken() {
    prefs.edit().remove(KEY_TOKEN).apply()
}
