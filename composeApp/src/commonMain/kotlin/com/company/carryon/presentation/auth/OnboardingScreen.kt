package com.company.carryon.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.truck_illustration
import drive_app.composeapp.generated.resources.ic_nav_home
import drive_app.composeapp.generated.resources.ic_nav_message
import drive_app.composeapp.generated.resources.ic_nav_person
import drive_app.composeapp.generated.resources.ic_nav_search
import org.jetbrains.compose.resources.painterResource

/**
 * OnboardingScreen — Welcome screen with Carry On branding and truck illustration.
 * Offers "Create an account" or "Log In" to proceed.
 */
@Composable
fun OnboardingScreen(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val onboardingButtonBlue = Color(0xFF2F80ED)

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
                text = strings.welcome,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = buildAnnotatedString {
                    append(strings.haveBetterExperience)
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
                colors = ButtonDefaults.buttonColors(containerColor = onboardingButtonBlue)
            ) {
                Text(strings.createAccount, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = { navigator.navigateTo(Screen.Login) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, onboardingButtonBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = onboardingButtonBlue)
            ) {
                Text(strings.logIn, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = onboardingButtonBlue)
            }
        }

        // ---- Bottom Navigation Bar ----
        OnboardingBottomBar()
    }
}

/**
 * OnboardingBottomBar — Static bottom navigation bar for onboarding/auth screens
 */
@Composable
fun OnboardingBottomBar() {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Gray,
        modifier = Modifier.height(64.dp)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Image(
                    painter = painterResource(Res.drawable.ic_nav_search),
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Image(
                    painter = painterResource(Res.drawable.ic_nav_message),
                    contentDescription = "Chat",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = {
                Image(
                    painter = painterResource(Res.drawable.ic_nav_home),
                    contentDescription = "Home",
                    modifier = Modifier.size(28.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Image(
                    painter = painterResource(Res.drawable.ic_nav_person),
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}
