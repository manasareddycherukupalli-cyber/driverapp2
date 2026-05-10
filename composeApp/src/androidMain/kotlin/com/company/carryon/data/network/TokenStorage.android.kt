package com.company.carryon.data.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val SECURE_PREFS_NAME = "driver_secure_prefs"
private const val PLAIN_PREFS_NAME = "driver_app_prefs"
private const val KEY_TOKEN = "jwt_token"
private const val KEY_LANGUAGE = "user_language"
private const val KEY_DELIVERY_RESUME_SCREEN = "delivery_resume_screen"
private const val KEY_DELIVERY_RESUME_JOB_ID = "delivery_resume_job_id"
private const val KEY_PUSH_TOKEN = "push_token"
private const val KEY_PUSH_DEVICE_ID = "push_device_id"
private const val KEY_PENDING_INCOMING_JOB = "pending_incoming_job"

private var securePrefs: SharedPreferences? = null
private var plainPrefs: SharedPreferences? = null

fun initTokenStorage(context: Context) {
    if (securePrefs == null) {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        securePrefs = EncryptedSharedPreferences.create(
            context.applicationContext,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    if (plainPrefs == null) {
        plainPrefs = context.applicationContext.getSharedPreferences(PLAIN_PREFS_NAME, Context.MODE_PRIVATE)
    }
    // Migrate tokens from old plain prefs to encrypted prefs
    migrateFromPlainPrefs()
}

private fun migrateFromPlainPrefs() {
    val oldPrefs = plainPrefs ?: return
    val oldToken = oldPrefs.getString(KEY_TOKEN, null)
    if (oldToken != null) {
        securePrefs?.edit()?.putString(KEY_TOKEN, oldToken)?.commit()
        oldPrefs.edit().remove(KEY_TOKEN).commit()
    }
}

actual fun saveToken(token: String) {
    securePrefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
}

actual fun getToken(): String? {
    return securePrefs?.getString(KEY_TOKEN, null)
}

actual fun clearToken() {
    securePrefs?.edit()?.remove(KEY_TOKEN)?.apply()
}

actual fun saveLanguage(language: String) {
    plainPrefs?.edit()?.putString(KEY_LANGUAGE, language)?.apply()
}

actual fun getLanguage(): String? {
    return plainPrefs?.getString(KEY_LANGUAGE, null)
}

actual fun saveDeliveryResumeState(screenKey: String, jobId: String) {
    plainPrefs?.edit()
        ?.putString(KEY_DELIVERY_RESUME_SCREEN, screenKey)
        ?.putString(KEY_DELIVERY_RESUME_JOB_ID, jobId)
        ?.apply()
}

actual fun getDeliveryResumeScreenKey(): String? {
    return plainPrefs?.getString(KEY_DELIVERY_RESUME_SCREEN, null)
}

actual fun getDeliveryResumeJobId(): String? {
    return plainPrefs?.getString(KEY_DELIVERY_RESUME_JOB_ID, null)
}

actual fun clearDeliveryResumeState() {
    plainPrefs?.edit()
        ?.remove(KEY_DELIVERY_RESUME_SCREEN)
        ?.remove(KEY_DELIVERY_RESUME_JOB_ID)
        ?.apply()
}

actual fun saveOnboardingDraft(key: String, payload: String) {
    plainPrefs?.edit()?.putString("onboarding_$key", payload)?.apply()
}

actual fun getOnboardingDraft(key: String): String? {
    return plainPrefs?.getString("onboarding_$key", null)
}

actual fun clearOnboardingDraft(key: String) {
    plainPrefs?.edit()?.remove("onboarding_$key")?.apply()
}

actual fun savePushToken(token: String) {
    securePrefs?.edit()?.putString(KEY_PUSH_TOKEN, token)?.apply()
}

actual fun getPushToken(): String? {
    return securePrefs?.getString(KEY_PUSH_TOKEN, null)
}

actual fun clearPushToken() {
    securePrefs?.edit()?.remove(KEY_PUSH_TOKEN)?.apply()
}

actual fun getOrCreateDeviceId(): String {
    val prefs = plainPrefs ?: return java.util.UUID.randomUUID().toString()
    val existing = prefs.getString(KEY_PUSH_DEVICE_ID, null)
    if (!existing.isNullOrBlank()) return existing

    val created = java.util.UUID.randomUUID().toString()
    prefs.edit().putString(KEY_PUSH_DEVICE_ID, created).apply()
    return created
}

actual fun markPendingIncomingJob() {
    plainPrefs?.edit()?.putBoolean(KEY_PENDING_INCOMING_JOB, true)?.apply()
}

actual fun consumePendingIncomingJob(): Boolean {
    val prefs = plainPrefs ?: return false
    val pending = prefs.getBoolean(KEY_PENDING_INCOMING_JOB, false)
    if (pending) {
        prefs.edit().remove(KEY_PENDING_INCOMING_JOB).apply()
    }
    return pending
}
