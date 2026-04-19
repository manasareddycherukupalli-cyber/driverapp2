package com.company.carryon.data.network

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "driver_app_prefs"
private const val KEY_TOKEN = "jwt_token"
private const val KEY_LANGUAGE = "user_language"
private const val KEY_DELIVERY_RESUME_SCREEN = "delivery_resume_screen"
private const val KEY_DELIVERY_RESUME_JOB_ID = "delivery_resume_job_id"

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

actual fun saveLanguage(language: String) {
    prefs.edit().putString(KEY_LANGUAGE, language).apply()
}

actual fun getLanguage(): String? {
    return prefs.getString(KEY_LANGUAGE, null)
}

actual fun saveDeliveryResumeState(screenKey: String, jobId: String) {
    prefs.edit()
        .putString(KEY_DELIVERY_RESUME_SCREEN, screenKey)
        .putString(KEY_DELIVERY_RESUME_JOB_ID, jobId)
        .apply()
}

actual fun getDeliveryResumeScreenKey(): String? {
    return prefs.getString(KEY_DELIVERY_RESUME_SCREEN, null)
}

actual fun getDeliveryResumeJobId(): String? {
    return prefs.getString(KEY_DELIVERY_RESUME_JOB_ID, null)
}

actual fun clearDeliveryResumeState() {
    prefs.edit()
        .remove(KEY_DELIVERY_RESUME_SCREEN)
        .remove(KEY_DELIVERY_RESUME_JOB_ID)
        .apply()
}

actual fun saveOnboardingDraft(key: String, payload: String) {
    prefs.edit().putString("onboarding_$key", payload).apply()
}

actual fun getOnboardingDraft(key: String): String? {
    return prefs.getString("onboarding_$key", null)
}

actual fun clearOnboardingDraft(key: String) {
    prefs.edit().remove("onboarding_$key").apply()
}
