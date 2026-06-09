package com.company.carryon

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.company.carryon.i18n.currentLanguageOrDefault
import com.company.carryon.i18n.hasStoredLanguagePreference
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.i18n.getStringsForLanguage
import com.company.carryon.i18n.storeLanguagePreference
import com.company.carryon.presentation.components.LanguageSelectionDialog
import com.company.carryon.presentation.navigation.AppNavHost
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.DriveAppTheme
import com.company.carryon.update.AppUpdateGate

/**
 * App.kt — Root composable entry point for the DriveApp.
 * Initialises the app theme, language, navigator, and nav host.
 */
@Composable
fun App() {
    AppUpdateGate {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    val navigator = remember { AppNavigator() }
    var currentLanguage by remember { mutableStateOf(currentLanguageOrDefault()) }
    var showLanguageDialog by remember { mutableStateOf(!hasStoredLanguagePreference()) }

    LaunchedEffect(Unit) {
        navigator.navigateAndClearStack(Screen.Splash)
    }

    val strings = remember(currentLanguage) { getStringsForLanguage(currentLanguage) }

    CompositionLocalProvider(LocalStrings provides strings) {
        DriveAppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                AppNavHost(
                    navigator = navigator,
                    currentLanguage = currentLanguage,
                    onLanguageChanged = { currentLanguage = it }
                )
                if (showLanguageDialog) {
                    LanguageSelectionDialog(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = { code ->
                        currentLanguage = storeLanguagePreference(code)
                        showLanguageDialog = false
                    }
                )
                }
            }
        }
    }
}
