package com.example.drive_app.presentation.auth

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
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.splash_illustration
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * SplashScreen — white background, illustration centred and fitted to screen width.
 */
@Composable
fun SplashScreen(navigator: AppNavigator) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        delay(2000)
        navigator.navigateAndClearStack(Screen.Login)
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
