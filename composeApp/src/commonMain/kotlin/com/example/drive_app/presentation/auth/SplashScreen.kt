package com.example.drive_app.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.CarryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.carry_on_logo
import androidx.compose.foundation.Image

/**
 * SplashScreen — Animated splash with the CARRY ON logo on brand-blue background.
 * Auto-navigates to Onboarding after 2.5 seconds.
 */
@Composable
fun SplashScreen(navigator: AppNavigator) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade + scale in simultaneously
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = EaseOutBack)
            )
        }
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        // Hold for user to see splash then navigate
        delay(2000)
        navigator.navigateAndClearStack(Screen.Onboarding)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarryBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Image(
                painter = painterResource(Res.drawable.carry_on_logo),
                contentDescription = "Carry On Logo",
                modifier = Modifier.size(220.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "CARRY ON",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )
            Text(
                text = "Driver App",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )
        }
    }
}
