package com.company.carryon.data.network

import platform.Foundation.NSUserDefaults

private const val KEY_TOKEN = "jwt_token"
private const val KEY_LANGUAGE = "user_language"
private const val KEY_DELIVERY_RESUME_SCREEN = "delivery_resume_screen"
private const val KEY_DELIVERY_RESUME_JOB_ID = "delivery_resume_job_id"
private const val KEY_PUSH_TOKEN = "push_token"
private const val KEY_PUSH_DEVICE_ID = "push_device_id"
private const val KEY_PENDING_INCOMING_JOB = "pending_incoming_job"

actual fun saveToken(token: String) {
    NSUserDefaults.standardUserDefaults.setObject(token, forKey = KEY_TOKEN)
}

actual fun getToken(): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(KEY_TOKEN)
}

actual fun clearToken() {
    NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_TOKEN)
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
    NSUserDefaults.standardUserDefaults.setObject(token, forKey = KEY_PUSH_TOKEN)
}

actual fun getPushToken(): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(KEY_PUSH_TOKEN)
}

actual fun clearPushToken() {
    NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY_PUSH_TOKEN)
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
