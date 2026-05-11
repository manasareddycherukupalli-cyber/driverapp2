package com.company.carryon.data.network

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val SECURE_PREFS_NAME = "driver_secure_prefs"
private const val PLAIN_PREFS_NAME = "driver_app_prefs"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEYSTORE_ALIAS = "carryon_driver_token_key"
private const val GCM_TAG_LENGTH_BITS = 128
private const val GCM_IV_LENGTH_BYTES = 12
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
        securePrefs = context.applicationContext.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
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
        putEncrypted(KEY_TOKEN, oldToken)
        oldPrefs.edit().remove(KEY_TOKEN).commit()
    }
}

actual fun saveToken(token: String) {
    putEncrypted(KEY_TOKEN, token)
}

actual fun getToken(): String? {
    return getEncrypted(KEY_TOKEN)
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
    putEncrypted(KEY_PUSH_TOKEN, token)
}

actual fun getPushToken(): String? {
    return getEncrypted(KEY_PUSH_TOKEN)
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

private fun putEncrypted(key: String, value: String) {
    val prefs = securePrefs ?: return
    runCatching {
        prefs.edit().putString(key, encrypt(value)).apply()
    }.onFailure {
        prefs.edit().remove(key).apply()
    }
}

private fun getEncrypted(key: String): String? {
    val prefs = securePrefs ?: return null
    val encrypted = prefs.getString(key, null) ?: return null
    return runCatching { decrypt(encrypted) }
        .onFailure { prefs.edit().remove(key).apply() }
        .getOrNull()
}

private fun encrypt(value: String): String {
    val iv = ByteArray(GCM_IV_LENGTH_BYTES)
    SecureRandom().nextBytes(iv)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
    val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
    return "${Base64.encodeToString(iv, Base64.NO_WRAP)}:${Base64.encodeToString(ciphertext, Base64.NO_WRAP)}"
}

private fun decrypt(value: String): String {
    val parts = value.split(':', limit = 2)
    require(parts.size == 2) { "Invalid encrypted value" }
    val iv = Base64.decode(parts[0], Base64.NO_WRAP)
    val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
    return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
}

private fun getOrCreateSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    (keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let {
        return it.secretKey
    }

    val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
    val spec = KeyGenParameterSpec.Builder(
        KEYSTORE_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(true)
        .build()

    generator.init(spec)
    return generator.generateKey()
}
