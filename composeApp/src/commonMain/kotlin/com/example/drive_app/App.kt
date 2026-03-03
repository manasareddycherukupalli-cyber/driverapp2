package com.example.drive_app

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.drive_app.presentation.navigation.AppNavHost
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.theme.DriveAppTheme

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