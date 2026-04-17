package com.company.carryon.data.network

import platform.Foundation.NSUserDefaults

private const val KEY_TOKEN = "jwt_token"
private const val KEY_LANGUAGE = "user_language"
private const val KEY_DELIVERY_RESUME_SCREEN = "delivery_resume_screen"
private const val KEY_DELIVERY_RESUME_JOB_ID = "delivery_resume_job_id"

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
