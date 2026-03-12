package com.company.carryon.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun LocationPermissionScreen(navigator: AppNavigator) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ---- Top App Bar ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                tint = Color(0xFF1A1A2E),
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Orange500, fontWeight = FontWeight.Bold)) {
                        append("Carry ")
                    }
                    withStyle(SpanStyle(color = Color(0xFF034094), fontWeight = FontWeight.Bold)) {
                        append("On")
                    }
                },
                fontSize = 24.sp
            )
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = "Notifications",
                tint = Color(0xFF1A1A2E),
                modifier = Modifier.size(28.dp)
            )
        }

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
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold)) {
                    append("Enable Your\n")
                }
                withStyle(SpanStyle(color = Orange500, fontWeight = FontWeight.Bold)) {
                    append("Location")
                }
            },
            fontSize = 28.sp,
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
                onClick = { navigator.navigateAndClearStack(Screen.Home) },
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
                    text = "Use current location",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Skip for now — outlined
            OutlinedButton(
                onClick = { navigator.navigateAndClearStack(Screen.Home) },
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
                    text = "Skip for now",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
