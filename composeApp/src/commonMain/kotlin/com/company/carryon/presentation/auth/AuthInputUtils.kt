package com.company.carryon.presentation.auth

import com.company.carryon.data.network.AuthSessionManager
import com.company.carryon.data.network.SupabaseConfig
import io.github.jan.supabase.auth.auth

private val EmailRegex = Regex("^[A-Za-z0-9._%+\\-']+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

fun normalizeEmailInput(value: String): String {
    return value
        .trim()
        .replace(Regex("\\s+"), "")
        .replace("\u200B", "")
        .replace("\u200C", "")
        .replace("\u200D", "")
        .lowercase()
}

fun isValidEmailInput(value: String): Boolean {
    return EmailRegex.matches(normalizeEmailInput(value))
}

suspend fun clearStaleAuthSessionForOtp() {
    AuthSessionManager.clearAccessToken()
    runCatching { SupabaseConfig.client.auth.clearSession() }
}

fun mapAuthErrorMessage(error: Throwable): String {
    val message = error.message.orEmpty()
    return when {
        message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("No address associated with hostname", ignoreCase = true) ->
            "Cannot reach auth server. Please check your internet connection and try again."
        message.contains("Request timeout", ignoreCase = true) ||
            message.contains("request_timeout", ignoreCase = true) ->
            "Request timed out. Please check your internet connection and try again."
        message.contains("invalid format", ignoreCase = true) ||
            message.contains("validation_failed", ignoreCase = true) ||
            message.contains("/auth/v1/otp", ignoreCase = true) ||
            message.contains("Headers:", ignoreCase = true) ->
            "Enter a valid email address and try again."
        message.contains("expired", ignoreCase = true) ||
            message.contains("JWT", ignoreCase = true) ->
            "Your session expired. Please request a new code."
        else -> "Authentication failed. Please try again."
    }
}
