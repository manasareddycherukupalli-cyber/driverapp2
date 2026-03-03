package com.example.drive_app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.truck_illustration
import org.jetbrains.compose.resources.painterResource

/**
 * OnboardingScreen — Welcome screen with Carry On branding and truck illustration.
 * Offers "Create an account" or "Log In" to proceed.
 */
@Composable
fun OnboardingScreen(navigator: AppNavigator) {
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
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.DarkGray)
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.DarkGray, fontWeight = FontWeight.Bold)) { append("Carry ") }
                    withStyle(SpanStyle(color = CarryBlue, fontWeight = FontWeight.Bold)) { append("On") }
                },
                fontSize = 22.sp
            )

            IconButton(onClick = {}) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Color.DarkGray)
            }
        }

        // ---- Truck Illustration ----
        Image(
            painter = painterResource(Res.drawable.truck_illustration),
            contentDescription = "Carry On Truck",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(24.dp))

        // ---- Welcome Text ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = "Welcome",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = buildAnnotatedString {
                    append("Have a Better Experience with ")
                    withStyle(SpanStyle(color = CarryBlue, fontWeight = FontWeight.SemiBold)) {
                        append("Carry On")
                    }
                },
                fontSize = 14.sp,
                color = Color(0xFF6B6B6B),
                lineHeight = 20.sp
            )
        }

        Spacer(Modifier.weight(1f))

        // ---- Buttons ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Button(
                onClick = { navigator.navigateTo(Screen.Registration) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CarryBlue)
            ) {
                Text("Create an account", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = { navigator.navigateTo(Screen.Login) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CarryBlue)
            ) {
                Text("Log In", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = CarryBlue)
            }
        }
    }
}
