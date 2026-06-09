package com.company.carryon.update

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppUpdateGateTest {
    @Test fun comparesSemanticVersions() {
        assertTrue(isVersionBelow("1.0", "1.0.1"))
        assertTrue(isVersionBelow("1.9.9", "2.0"))
        assertFalse(isVersionBelow("1.0.1", "1.0.1"))
        assertFalse(isVersionBelow("1.1", "1.0.9"))
    }
}
