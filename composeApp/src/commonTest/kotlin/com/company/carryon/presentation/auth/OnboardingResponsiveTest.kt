package com.company.carryon.presentation.auth

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingResponsiveTest {
    @Test
    fun heroHeightIsWidthDrivenOnTallPhones() {
        assertEquals(355.dp, onboardingHeroHeight(390.dp, 844.dp).value.toInt().dp)
    }

    @Test
    fun heroHeightIsCappedByViewportOnTablets() {
        assertEquals(420f, onboardingHeroHeight(840.dp, 700.dp).value, 0.01f)
    }

}
