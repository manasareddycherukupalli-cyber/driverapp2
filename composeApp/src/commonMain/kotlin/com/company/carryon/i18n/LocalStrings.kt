package com.company.carryon.i18n

import androidx.compose.runtime.staticCompositionLocalOf

object EnDriverStrings : DriverStrings

val LocalStrings = staticCompositionLocalOf<DriverStrings> { EnDriverStrings }

fun getStringsForLanguage(code: String): DriverStrings = when (code) {
    "ms" -> MsDriverStrings
    "zh" -> ZhDriverStrings
    "ta" -> TaDriverStrings
    else -> EnDriverStrings
}

fun getLanguageDisplayName(code: String): String = when (code) {
    "ms" -> "Bahasa Melayu"
    "zh" -> "中文"
    "ta" -> "தமிழ்"
    else -> "English"
}
