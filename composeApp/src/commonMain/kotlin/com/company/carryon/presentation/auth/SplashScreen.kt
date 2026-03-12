package com.company.carryon.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.company.carryon.data.model.UiState
import com.company.carryon.data.network.SupabaseConfig
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.splash_illustration
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * SplashScreen — white background, illustration centred and fitted to screen width.
 * Checks for existing session and routes accordingly.
 */
@Composable
fun SplashScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val alpha = remember { Animatable(0f) }
    val sessionSyncState by authViewModel.sessionSyncState.collectAsState()

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        delay(2000)

        // Check for existing Supabase session
        val session = try {
            SupabaseConfig.client.auth.currentSessionOrNull()
        } catch (_: Exception) {
            null
        }

        if (session != null) {
            // Has session — sync driver and route based on profile completeness
            authViewModel.syncDriverForSession()
        } else {
            // No session — go to onboarding
            navigator.navigateAndClearStack(Screen.Onboarding)
        }
    }

    // Observe sync result for existing session
    LaunchedEffect(sessionSyncState) {
        when (val state = sessionSyncState) {
            is UiState.Success -> {
                val screen = authViewModel.determinePostAuthScreen(state.data)
                navigator.navigateAndClearStack(screen)
            }
            is UiState.Error -> {
                // Session exists but sync failed — go to onboarding to re-auth
                navigator.navigateAndClearStack(Screen.Onboarding)
            }
            else -> {} // Loading or Idle — wait
        }
    }

    Box(
        modifier          = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment  = Alignment.Center
    ) {
        Image(
            painter            = painterResource(Res.drawable.splash_illustration),
            contentDescription = "Carry On",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .fillMaxWidth()
                .alpha(alpha.value)
        )
    }
}
