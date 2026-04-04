package com.company.carryon.presentation.profile

import androidx.compose.runtime.Composable
import com.company.carryon.presentation.navigation.AppNavigator

/**
 * Backward-compatible entry point.
 * Any old route or stale call site targeting ProfileScreen now shows the new Settings/Profile surface.
 */
@Composable
fun ProfileScreen(navigator: AppNavigator) {
    SettingsScreen(navigator)
}

