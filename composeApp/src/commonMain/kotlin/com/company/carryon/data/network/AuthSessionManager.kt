package com.company.carryon.data.network

import io.github.jan.supabase.auth.auth

object AuthSessionManager {
    private var cachedAccessToken: String? = null

    fun currentAccessToken(): String? {
        val sessionToken = runCatching {
            SupabaseConfig.client.auth.currentSessionOrNull()?.accessToken
        }.getOrNull()

        if (!sessionToken.isNullOrBlank()) {
            return storeAccessToken(sessionToken)
        }

        val storedToken = getToken()
        if (!storedToken.isNullOrBlank()) {
            cachedAccessToken = storedToken
            return storedToken
        }

        return cachedAccessToken
    }

    fun storeAccessToken(token: String): String {
        cachedAccessToken = token
        saveToken(token)
        return token
    }

    fun clearAccessToken() {
        cachedAccessToken = null
        clearToken()
    }
}
