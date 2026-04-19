package com.company.carryon.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*
import drive_app.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * LocationPermissionScreen — Shown after login.
 * Prompts the driver to enable location or skip for now.
 */
@Composable
fun LocationPermissionScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val latestAuthResponse by authViewModel.latestAuthResponse.collectAsState()
    if (latestAuthResponse == null) {
        LaunchedEffect(Unit) {
            navigator.navigateAndClearStack(Screen.Onboarding)
        }
        return
    }
    val postLocationRoute = authViewModel.determinePostLocationScreen(latestAuthResponse)
    val nextScreen = if (postLocationRoute == Screen.Home) Screen.Home else Screen.VerificationStatus
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        DriveAppTopBar(
            title = strings.enableYourLocation,
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
            showTitle = false
        )

        Spacer(Modifier.height(24.dp))

        // ---- Illustration ----
        Image(
            painter = painterResource(Res.drawable.location_illustration),
            contentDescription = "Enable location illustration",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(300.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(48.dp))

        // ---- Title ----
        Text(
            text = strings.enableYourLocation,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Orange500,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))

        // ---- Buttons ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Use current location — filled
            Button(
                onClick = { navigator.navigateAndClearStack(nextScreen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange500,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = strings.useCurrentLocation,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Skip for now — outlined
            OutlinedButton(
                onClick = { navigator.navigateAndClearStack(nextScreen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Orange500
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Orange500)
            ) {
                Text(
                    text = strings.skipForNow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
