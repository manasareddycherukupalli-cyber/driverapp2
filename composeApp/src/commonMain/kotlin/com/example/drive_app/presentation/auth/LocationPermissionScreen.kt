package com.example.drive_app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEBF0FF))
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD6E2FF)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFBDD0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    // Phone + pin icon representation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Orange500,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Orange500.copy(alpha = 0.3f))
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Orange500.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }

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
