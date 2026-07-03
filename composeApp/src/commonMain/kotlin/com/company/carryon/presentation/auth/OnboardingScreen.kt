package com.company.carryon.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.carryOnWordmarkFontFamily
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.truck_illustration
import org.jetbrains.compose.resources.painterResource
import kotlin.math.min

private const val ONBOARDING_HERO_ASPECT_RATIO = 1.096f
private const val ONBOARDING_HERO_MAX_HEIGHT_FRACTION = 0.60f

internal fun onboardingHeroHeight(availableWidth: androidx.compose.ui.unit.Dp, availableHeight: androidx.compose.ui.unit.Dp) =
    min(
        availableWidth.value / ONBOARDING_HERO_ASPECT_RATIO,
        availableHeight.value * ONBOARDING_HERO_MAX_HEIGHT_FRACTION
    ).dp

/**
 * OnboardingScreen — Welcome screen with Carry On branding and truck illustration.
 * Offers "Create an account" or "Log In" to proceed.
 */
@Composable
fun OnboardingScreen(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val brandBlue = Color(0xFF034094)
    val wordmarkFontFamily = carryOnWordmarkFontFamily()

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.TopCenter
    ) {
    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxHeight()
            .background(Color.White)
    ) {
        val shortViewport = maxHeight < 700.dp
        val heroHeight = onboardingHeroHeight(maxWidth, maxHeight)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = painterResource(Res.drawable.truck_illustration),
                contentDescription = "Carry On delivery driver and van",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(heroHeight),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(top = if (shortViewport) 16.dp else 24.dp)
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
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF2F80ED),
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic,
                                fontFamily = wordmarkFontFamily
                            )
                        ) {
                            append("CARRY ")
                        }
                        withStyle(
                            SpanStyle(
                                color = brandBlue,
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic,
                                fontFamily = wordmarkFontFamily
                            )
                        ) {
                            append("ON")
                        }
                        withStyle(SpanStyle(color = Color(0xFF333333), fontWeight = FontWeight.Bold)) {
                            append("!")
                        }
                    },
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(Modifier.height(if (shortViewport) 24.dp else 48.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
    }
}
