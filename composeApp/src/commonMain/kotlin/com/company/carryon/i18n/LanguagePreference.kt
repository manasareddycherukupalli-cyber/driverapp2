package com.company.carryon.i18n

import com.company.carryon.data.network.getLanguage
import com.company.carryon.data.network.saveLanguage

fun currentLanguageOrDefault(): String = SupportedLanguages.normalize(getLanguage())

fun hasStoredLanguagePreference(): Boolean = getLanguage()?.let(SupportedLanguages::isSupported) == true

fun storeLanguagePreference(code: String): String {
    val normalized = SupportedLanguages.normalize(code)
    saveLanguage(normalized)
    return normalized
}

