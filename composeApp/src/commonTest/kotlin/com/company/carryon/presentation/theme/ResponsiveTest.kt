package com.company.carryon.presentation.theme

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class ResponsiveTest {
    @Test
    fun widthClassesCoverPhoneAndTabletBoundaries() {
        assertEquals(WindowWidthClass.Compact, windowWidthClassFor(320.dp))
        assertEquals(WindowWidthClass.Compact, windowWidthClassFor(359.dp))
        assertEquals(WindowWidthClass.Medium, windowWidthClassFor(360.dp))
        assertEquals(WindowWidthClass.Medium, windowWidthClassFor(599.dp))
        assertEquals(WindowWidthClass.Expanded, windowWidthClassFor(600.dp))
        assertEquals(WindowWidthClass.Expanded, windowWidthClassFor(1280.dp))
    }
}
