package com.company.carryon.data.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val SERVICE_NAME = "com.company.carryon.driver"
private const val KEY_TOKEN = "jwt_token"
private const val KEY_LANGUAGE = "user_language"
private const val KEY_DELIVERY_RESUME_SCREEN = "delivery_resume_screen"
private const val KEY_DELIVERY_RESUME_JOB_ID = "delivery_resume_job_id"
private const val KEY_PUSH_TOKEN = "push_token"
private const val KEY_PUSH_DEVICE_ID = "push_device_id"
private const val KEY_PENDING_INCOMING_JOB = "pending_incoming_job"

@OptIn(ExperimentalForeignApi::class)
private fun keychainSave(key: String, value: String) {
    val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
    keychainDelete(key)

    val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 5, null, null)
    CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
    CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
    CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))
    CFDictionaryAddValue(query, kSecValueData, CFBridgingRetain(data))
    CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
    SecItemAdd(query, null)
}

@OptIn(ExperimentalForeignApi::class)
private fun keychainRead(key: String): String? {
    return memScoped {
        val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 5, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        if (status != errSecSuccess) return@memScoped null

        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun keychainDelete(key: String) {
    val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 3, null, null)
    CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
    CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
    CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))
    SecItemDelete(query)
}

// Migrate token from NSUserDefaults to Keychain
private var migrationDone = false
private fun migrateFromUserDefaults() {
    if (migrationDone) return
    migrationDone = true
    val defaults = NSUserDefaults.standardUserDefaults
    val oldToken = defaults.stringForKey(KEY_TOKEN)
    if (oldToken != null) {
        keychainSave(KEY_TOKEN, oldToken)
        defaults.removeObjectForKey(KEY_TOKEN)
    }
}

actual fun saveToken(token: String) {
    migrateFromUserDefaults()
    keychainSave(KEY_TOKEN, token)
}

actual fun getToken(): String? {
    migrateFromUserDefaults()
    return keychainRead(KEY_TOKEN)
}

actual fun clearToken() {
    keychainDelete(KEY_TOKEN)
}

actual fun saveLanguage(language: String) {
    NSUserDefaults.standardUserDefaults.setObject(language, forKey = KEY_LANGUAGE)
}

actual fun getLanguage(): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(KEY_LANGUAGE)
}

actual fun saveDeliveryResumeState(screenKey: String, jobId: String) {
    NSUserDefaults.standardUserDefaults.setObject(screenKey, forKey = KEY_DELIVERY_RESUME_SCREEN)
    NSUserDefaults.standardUserDefaults.setObject(jobId, forKey = KEY_DELIVERY_RESUME_JOB_ID)
}

actual fun getDeliveryResumeScreenKey(): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(KEY_DELIVERY_RESUME_SCREEN)
}

actual fun getDeliveryResumeJobId(): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(KEY_DELIVERY_RESUME_JOB_ID)
}

actual fun clearDeliveryResumeState() {
    NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_DELIVERY_RESUME_SCREEN)
    NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_DELIVERY_RESUME_JOB_ID)
}

actual fun saveOnboardingDraft(key: String, payload: String) {
    NSUserDefaults.standardUserDefaults.setObject(payload, forKey = "onboarding_$key")
}

actual fun getOnboardingDraft(key: String): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey("onboarding_$key")
}

actual fun clearOnboardingDraft(key: String) {
    NSUserDefaults.standardUserDefaults.removeObjectForKey("onboarding_$key")
}

actual fun savePushToken(token: String) {
    keychainSave(KEY_PUSH_TOKEN, token)
}

actual fun getPushToken(): String? {
    return keychainRead(KEY_PUSH_TOKEN)
}

actual fun clearPushToken() {
    keychainDelete(KEY_PUSH_TOKEN)
}

actual fun getOrCreateDeviceId(): String {
    val defaults = NSUserDefaults.standardUserDefaults
    val existing = defaults.stringForKey(KEY_PUSH_DEVICE_ID)
    if (!existing.isNullOrBlank()) return existing

    val created = platform.Foundation.NSUUID().UUIDString()
    defaults.setObject(created, forKey = KEY_PUSH_DEVICE_ID)
    return created
}

actual fun markPendingIncomingJob() {
    NSUserDefaults.standardUserDefaults.setBool(true, forKey = KEY_PENDING_INCOMING_JOB)
}

actual fun consumePendingIncomingJob(): Boolean {
    val defaults = NSUserDefaults.standardUserDefaults
    val pending = defaults.boolForKey(KEY_PENDING_INCOMING_JOB)
    if (pending) {
        defaults.removeObjectForKey(KEY_PENDING_INCOMING_JOB)
    }
    return pending
}
