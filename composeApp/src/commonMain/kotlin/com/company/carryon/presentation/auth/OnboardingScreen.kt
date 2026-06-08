package com.company.carryon.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.truck_illustration
import org.jetbrains.compose.resources.painterResource

/**
 * OnboardingScreen — Welcome screen with Carry On branding and truck illustration.
 * Offers "Create an account" or "Log In" to proceed.
 */
@Composable
fun OnboardingScreen(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val brandBlue = Color(0xFF034094)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(Res.drawable.truck_illustration),
            contentDescription = "Carry On delivery driver and van",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.096f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 24.dp)
        ) {
            Text(
                text = strings.welcome,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = buildAnnotatedString {
                    append(strings.haveBetterExperience)
                    withStyle(SpanStyle(color = brandBlue, fontWeight = FontWeight.SemiBold)) {
                        append("Carry On")
                    }
                },
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = Color(0xFF666666)
            )
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { navigator.navigateTo(Screen.Registration) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
            ) {
                Text(strings.createAccount, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }

            OutlinedButton(
                onClick = { navigator.navigateTo(Screen.Login) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, brandBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = brandBlue)
            ) {
                Text(strings.logIn, fontWeight = FontWeight.Medium, fontSize = 17.sp)
            }
        }
    }
}
