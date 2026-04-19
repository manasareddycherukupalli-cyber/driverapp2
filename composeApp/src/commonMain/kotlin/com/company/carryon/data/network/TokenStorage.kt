package com.company.carryon.data.network

expect fun saveToken(token: String)
expect fun getToken(): String?
expect fun clearToken()
expect fun saveLanguage(language: String)
expect fun getLanguage(): String?
expect fun saveDeliveryResumeState(screenKey: String, jobId: String)
expect fun getDeliveryResumeScreenKey(): String?
expect fun getDeliveryResumeJobId(): String?
expect fun clearDeliveryResumeState()
expect fun saveOnboardingDraft(key: String, payload: String)
expect fun getOnboardingDraft(key: String): String?
expect fun clearOnboardingDraft(key: String)
