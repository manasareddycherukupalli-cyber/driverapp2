package com.company.carryon.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LanguageCoverageTest {
    @Test
    fun supportsOnlyLaunchLanguages() {
        assertEquals(setOf("en", "ms", "ta", "zh"), SupportedLanguages.codes)
        assertEquals(4, SupportedLanguages.all.size)
    }

    @Test
    fun unsupportedLanguageFallsBackToEnglish() {
        assertEquals("en", SupportedLanguages.normalize("de"))
        assertSame(EnDriverStrings, getStringsForLanguage("de"))
    }

    @Test
    fun everySupportedLanguageResolvesStrings() {
        SupportedLanguages.codes.forEach { code ->
            val strings = getStringsForLanguage(code)
            assertTrue(strings.appName.isNotBlank())
            assertTrue(strings.continueText.isNotBlank())
            assertTrue(strings.selectYourLanguage.isNotBlank())
        }
    }
}

