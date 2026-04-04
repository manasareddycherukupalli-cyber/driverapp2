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
import com.company.carryon.data.network.getToken
import com.company.carryon.data.network.saveToken
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.splash_illustration
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private const val FORCE_PRE_HOME_FLOW_ON_LAUNCH = true

/**
 * SplashScreen — white background, illustration centred and fitted to screen width.
 * Checks for existing session and routes accordingly.
 */
@Composable
fun SplashScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val alpha = remember { Animatable(0f) }
    val sessionSyncState by authViewModel.sessionSyncState.collectAsState()

    LaunchedEffect(Unit) {
        // Run minimum splash display and auth check in parallel
        val minDelay = launch { delay(800) }

        alpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 600)
        )

        if (FORCE_PRE_HOME_FLOW_ON_LAUNCH) {
            minDelay.join()
            navigator.navigateAndClearStack(Screen.Onboarding)
            return@LaunchedEffect
        }

        // 1. Try to get the existing Supabase session (SDK auto-loads from storage)
        var session = try {
            SupabaseConfig.client.auth.currentSessionOrNull()
        } catch (_: Exception) {
            null
        }

        // 2. If no session yet, try refreshing (handles expired access tokens with valid refresh tokens)
        if (session == null) {
            session = try {
                SupabaseConfig.client.auth.refreshCurrentSession()
                SupabaseConfig.client.auth.currentSessionOrNull()
            } catch (_: Exception) {
                null
            }
        }

        // Wait for minimum splash duration before navigating
        minDelay.join()

        if (session != null) {
            // Save the fresh access token for API calls
            saveToken(session.accessToken)
            // Has valid session — sync driver and route based on profile completeness
            authViewModel.syncDriverForSession()
        } else if (getToken() != null) {
            // Fallback: stored token exists but no Supabase session.
            // Try to sync with the stored token — if it's still valid, the API call will succeed.
            authViewModel.syncDriverForSession()
        } else {
            // No session and no stored token — go to onboarding
            navigator.navigateAndClearStack(Screen.Onboarding)
        }
    }

    // Observe sync result for existing session
    LaunchedEffect(sessionSyncState) {
        if (FORCE_PRE_HOME_FLOW_ON_LAUNCH) return@LaunchedEffect
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
