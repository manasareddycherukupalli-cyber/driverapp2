package com.company.carryon.data.network

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "driver_app_prefs"
private const val KEY_TOKEN = "jwt_token"
private const val KEY_LANGUAGE = "user_language"
private const val KEY_DELIVERY_RESUME_SCREEN = "delivery_resume_screen"
private const val KEY_DELIVERY_RESUME_JOB_ID = "delivery_resume_job_id"
private const val KEY_PUSH_TOKEN = "push_token"
private const val KEY_PUSH_DEVICE_ID = "push_device_id"
private const val KEY_PENDING_INCOMING_JOB = "pending_incoming_job"

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

actual fun savePushToken(token: String) {
    prefs.edit().putString(KEY_PUSH_TOKEN, token).apply()
}

actual fun getPushToken(): String? {
    return prefs.getString(KEY_PUSH_TOKEN, null)
}

actual fun clearPushToken() {
    prefs.edit().remove(KEY_PUSH_TOKEN).apply()
}

actual fun getOrCreateDeviceId(): String {
    val existing = prefs.getString(KEY_PUSH_DEVICE_ID, null)
    if (!existing.isNullOrBlank()) return existing

    val created = java.util.UUID.randomUUID().toString()
    prefs.edit().putString(KEY_PUSH_DEVICE_ID, created).apply()
    return created
}

actual fun markPendingIncomingJob() {
    prefs.edit().putBoolean(KEY_PENDING_INCOMING_JOB, true).apply()
}

actual fun consumePendingIncomingJob(): Boolean {
    val pending = prefs.getBoolean(KEY_PENDING_INCOMING_JOB, false)
    if (pending) {
        prefs.edit().remove(KEY_PENDING_INCOMING_JOB).apply()
    }
    return pending
}
