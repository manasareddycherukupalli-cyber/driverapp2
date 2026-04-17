package com.company.carryon

import androidx.compose.runtime.*
import com.company.carryon.data.network.getLanguage
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.i18n.getStringsForLanguage
import com.company.carryon.presentation.navigation.AppNavHost
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.DriveAppTheme

/**
 * App.kt — Root composable entry point for the DriveApp.
 * Initialises the app theme, language, navigator, and nav host.
 */
@Composable
fun App() {
    val navigator = remember { AppNavigator() }
    var currentLanguage by remember { mutableStateOf(getLanguage() ?: "en") }

    LaunchedEffect(Unit) {
        navigator.navigateAndClearStack(Screen.Splash)
    }

    val strings = remember(currentLanguage) { getStringsForLanguage(currentLanguage) }

    CompositionLocalProvider(LocalStrings provides strings) {
        DriveAppTheme {
            AppNavHost(
                navigator = navigator,
                currentLanguage = currentLanguage,
                onLanguageChanged = { currentLanguage = it }
            )
        }
    }
}
