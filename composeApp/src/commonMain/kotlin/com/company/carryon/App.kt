package com.company.carryon

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.company.carryon.presentation.navigation.AppNavHost
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.theme.DriveAppTheme

/**
 * App.kt — Root composable entry point for the DriveApp.
 * Initializes the app theme, navigator, and nav host.
 */
@Composable
fun App() {
    val navigator = remember { AppNavigator() }

    DriveAppTheme {
        AppNavHost(navigator = navigator)
    }
}