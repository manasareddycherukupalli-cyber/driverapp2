package com.company.carryon.update

import android.content.Context

private lateinit var applicationContext: Context

fun initAppUpdatePlatform(context: Context) {
    applicationContext = context.applicationContext
}

actual fun appVersion(): String =
    applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName ?: "0"

actual fun appPlatform(): String = "android"
actual fun fallbackStoreUrl(): String? =
    "https://play.google.com/store/apps/details?id=${applicationContext.packageName}"
