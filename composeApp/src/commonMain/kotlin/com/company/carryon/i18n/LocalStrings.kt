package com.company.carryon.i18n

import androidx.compose.runtime.staticCompositionLocalOf

object EnDriverStrings : DriverStrings

val LocalStrings = staticCompositionLocalOf<DriverStrings> { EnDriverStrings }

fun getStringsForLanguage(code: String): DriverStrings = when (SupportedLanguages.normalize(code)) {
    "ms" -> MsDriverStrings
    "zh" -> ZhDriverStrings
    "ta" -> TaDriverStrings
    else -> EnDriverStrings
}

fun getLanguageDisplayName(code: String): String = SupportedLanguages.displayName(code)
