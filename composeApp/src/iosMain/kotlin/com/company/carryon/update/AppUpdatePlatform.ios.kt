package com.company.carryon.update

import platform.Foundation.NSBundle

actual fun appVersion(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "0"

actual fun appPlatform(): String = "ios"
actual fun fallbackStoreUrl(): String? = null
