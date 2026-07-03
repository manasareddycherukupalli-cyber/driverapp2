package com.company.carryon.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen-width buckets for responsive layouts.
 * Compact  : phones under 360 dp  (iPhone SE, Galaxy A-series)
 * Medium   : typical phones 360-600 dp
 * Expanded : tablets / foldables > 600 dp
 */
enum class WindowWidthClass { Compact, Medium, Expanded }

val LocalWindowWidthClass = compositionLocalOf { WindowWidthClass.Medium }

/** Maximum width for phone-style, single-pane content on larger displays. */
val ExpandedContentMaxWidth = 840.dp

/** Kept outside composition so width-class boundaries can be unit tested. */
fun windowWidthClassFor(width: Dp): WindowWidthClass = when {
    width < 360.dp -> WindowWidthClass.Compact
    width < 600.dp -> WindowWidthClass.Medium
    else -> WindowWidthClass.Expanded
}

// ─── Global adaptive density scaling ─────────────────────────────

/** Width (dp) the screens were designed against. */
const val DesignScreenWidthDp = 390f

/**
 * Scale applied to [androidx.compose.ui.unit.Density] app-wide so fixed dp/sp
 * values shrink proportionally on phones narrower than the design baseline.
 * Never upscales (screens >= 390 dp render exactly as designed); the floor
 * keeps touch targets usable on very narrow displays.
 */
fun adaptiveDensityScaleFor(screenWidth: Dp): Float =
    (screenWidth.value / DesignScreenWidthDp).coerceIn(0.85f, 1f)

// ─── Responsive scale factors ────────────────────────────────────

/**
 * Returns a scale factor (0.85 / 1.0 / 1.1) depending on screen width.
 * Use to multiply dp or sp values that must shrink on small screens.
 */
@Composable
fun responsiveScale(): Float = when (LocalWindowWidthClass.current) {
    WindowWidthClass.Compact -> 0.85f
    WindowWidthClass.Medium -> 1.0f
    WindowWidthClass.Expanded -> 1.1f
}

/** Scales a Dp value by the current window-width class. */
@Composable
fun Dp.responsive(): Dp = this * responsiveScale()

/** Scales a TextUnit (sp) value by the current window-width class. */
@Composable
fun TextUnit.responsive(): TextUnit = (this.value * responsiveScale()).sp
