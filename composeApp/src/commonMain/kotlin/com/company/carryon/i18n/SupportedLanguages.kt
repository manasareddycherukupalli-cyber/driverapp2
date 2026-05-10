package com.company.carryon.i18n

data class SupportedLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val description: String,
    val iconText: String
)

object SupportedLanguages {
    const val DEFAULT_CODE = "en"

    val all = listOf(
        SupportedLanguage("en", "English", "English", "English (Malaysia)", "A"),
        SupportedLanguage("ms", "Malay", "Bahasa Melayu", "Malay", "B"),
        SupportedLanguage("ta", "Tamil", "தமிழ்", "Tamil", "த"),
        SupportedLanguage("zh", "Mandarin", "中文", "Mandarin Chinese", "文")
    )

    val codes: Set<String> = all.map { it.code }.toSet()

    fun isSupported(code: String): Boolean = code in codes

    fun normalize(code: String?): String {
        val normalized = code?.trim()?.lowercase().orEmpty()
        return if (isSupported(normalized)) normalized else DEFAULT_CODE
    }

    fun displayName(code: String): String {
        return all.firstOrNull { it.code == normalize(code) }?.nativeName ?: "English"
    }
}

