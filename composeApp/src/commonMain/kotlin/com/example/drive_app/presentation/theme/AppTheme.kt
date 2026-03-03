package com.example.drive_app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// DRIVEAPP COLOR PALETTE — Inspired by Lalamove/Uber
// ============================================================

// Primary Brand Blue
val Orange500 = Color(0xFF2F80ED)   // #2F80ED — main brand blue
val Orange600 = Color(0xFF1A6ED4)   // slightly darker
val Orange700 = Color(0xFF0D59B0)   // darkest
val Orange400 = Color(0xFF5B9BF2)   // slightly lighter
val Orange100 = Color(0xFFE8F2FD)   // very light tint

// Carry On Brand Blue
val CarryBlue = Color(0xFF4361EE)
val CarryBlueDark = Color(0xFF3451D1)
val CarryBlueLight = Color(0xFFEEF1FF)

// Secondary Dark
val Dark900 = Color(0xFF1A1A2E)
val Dark800 = Color(0xFF16213E)
val Dark700 = Color(0xFF0F3460)
val Dark600 = Color(0xFF2C2C3E)

// Neutrals
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)

// Status Colors
val Green500 = Color(0xFF4CAF50)
val Green100 = Color(0xFFE8F5E9)
val Red500 = Color(0xFFF44336)
val Red100 = Color(0xFFFFEBEE)
val Blue500 = Color(0xFF2196F3)
val Blue100 = Color(0xFFE3F2FD)
val Yellow500 = Color(0xFFFFC107)
val Yellow100 = Color(0xFFFFF8E1)

// ============================================================
// LIGHT THEME
// ============================================================
private val LightColorScheme = lightColorScheme(
    primary = Orange500,
    onPrimary = Color.White,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange700,
    secondary = Dark900,
    onSecondary = Color.White,
    secondaryContainer = Gray200,
    onSecondaryContainer = Dark900,
    tertiary = Green500,
    onTertiary = Color.White,
    tertiaryContainer = Green100,
    onTertiaryContainer = Color(0xFF1B5E20),
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray400,
    outlineVariant = Gray300,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Color(0xFFB71C1C),
)

// ============================================================
// DARK THEME
// ============================================================
private val DarkColorScheme = darkColorScheme(
    primary = Orange400,
    onPrimary = Color.Black,
    primaryContainer = Orange700,
    onPrimaryContainer = Orange100,
    secondary = Color(0xFFBBDEFB),
    onSecondary = Dark900,
    secondaryContainer = Dark700,
    onSecondaryContainer = Color(0xFFE3F2FD),
    tertiary = Color(0xFF81C784),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF1B5E20),
    onTertiaryContainer = Green100,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Gray400,
    outline = Gray600,
    outlineVariant = Gray700,
    error = Color(0xFFEF5350),
    onError = Color.Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

// ============================================================
// APP THEME COMPOSABLE
// ============================================================

@Composable
fun DriveAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Default Material3 typography
        content = content
    )
}
