package com.company.carryon.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResponsiveNavigationTest {
    @Test
    fun onlyMapLedScreensUseTheFullTabletCanvas() {
        assertTrue(Screen.MapNavigation.usesFullCanvas())
        assertTrue(Screen.InTransitNavigation.usesFullCanvas())
        assertFalse(Screen.Home.usesFullCanvas())
        assertFalse(Screen.Login.usesFullCanvas())
        assertFalse(Screen.JobDetails.usesFullCanvas())
    }
}
