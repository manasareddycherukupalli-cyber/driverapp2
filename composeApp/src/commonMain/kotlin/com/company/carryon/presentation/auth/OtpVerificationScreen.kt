package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.data.network.SupabaseConfig
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * OtpVerificationScreen — 6-digit code entry with custom phone keypad.
 * Matches the Carry On design with back button and resend option.
 */
@Composable
fun OtpVerificationScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val strings = LocalStrings.current
    val otpBlue = Color(0xFF2F80ED)
    val otpLength = 6
    var otpValues by remember { mutableStateOf(List(otpLength) { "" }) }
    var resendTimer by remember { mutableStateOf(30) }
    var canResend by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
        canResend = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ---- Back Arrow ----
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF5F5F5))
                .clickable { navigator.goBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A1A2E))
        }

        // ---- Title ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            Text(
                text = strings.enterTheCode,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = strings.verificationCodeSentToEmail,
                fontSize = 14.sp,
                color = Color(0xFF6B6B6B)
            )
            Text(
                text = authViewModel.driverEmail.ifEmpty { "your email" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E)
            )
        }

        Spacer(Modifier.height(32.dp))

        // ---- OTP Digit Boxes ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(otpLength) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.5.dp,
                            color = if (otpValues[index].isNotEmpty()) otpBlue else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = otpValues[index],
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---- Resend ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(strings.dontReceiveCode, fontSize = 14.sp, color = Color(0xFF6B6B6B))
            if (canResend) {
                Text(
                    text = strings.resendAgain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = otpBlue,
                    modifier = Modifier.clickable {
                        canResend = false
                        resendTimer = 30
                        otpValues = List(otpLength) { "" }
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                SupabaseConfig.client.auth.signInWith(OTP) {
                                    email = authViewModel.driverEmail
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to resend code"
                            }
                        }
                    }
                )
            } else {
                Text(strings.resendTimer(resendTimer), fontSize = 14.sp, color = Color(0xFFAAAAAA))
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---- Error Message ----
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                fontSize = 13.sp,
                color = Color(0xFFE53935),
                modifier = Modifier.padding(horizontal = 28.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        // ---- Next Button ----
        Button(
            onClick = {
                errorMessage = null
                isVerifying = true
                val otp = otpValues.joinToString("")
                coroutineScope.launch {
                    try {
                        // Use the OTP provider's verifyEmailOtp method
                        SupabaseConfig.client.auth.verifyEmailOtp(
                            type = OtpType.Email.EMAIL,
                            email = authViewModel.driverEmail,
                            token = otp
                        )
                        // Get the session after successful verification
                        val session = SupabaseConfig.client.auth.currentSessionOrNull()
                        val accessToken = session?.accessToken
                        if (accessToken != null) {
                            authViewModel.onSupabaseTokenReceived(accessToken)
                            // Wait for the first terminal state from sync result.
                            when (val state = authViewModel.otpVerifyState.first {
                                it is UiState.Success || it is UiState.Error
                            }) {
                                is UiState.Success -> {
                                    val screen = authViewModel.determinePostAuthScreen(state.data)
                                    navigator.navigateAndClearStack(screen)
                                }
                                is UiState.Error -> {
                                    errorMessage = state.message
                                }
                                else -> {}
                            }
                            isVerifying = false
                        } else {
                            errorMessage = "Verification succeeded but no session found"
                            isVerifying = false
                        }
                    } catch (e: Exception) {
                        val msg = e.message ?: "OTP verification failed"
                        // Log the full error for debugging
                        println("OTP Verification Error: $msg")
                        println("Email used: ${authViewModel.driverEmail}")
                        errorMessage = msg
                        isVerifying = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = otpBlue),
            enabled = otpValues.all { it.isNotEmpty() } && !isVerifying
        ) {
            if (isVerifying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(strings.next, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        // ---- Custom Phone Keypad ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(bottom = 16.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        KeypadButton(
                            label = key,
                            onClick = {
                                when (key) {
                                    "⌫" -> {
                                        val lastFilled = otpValues.indexOfLast { it.isNotEmpty() }
                                        if (lastFilled >= 0) {
                                            otpValues = otpValues.toMutableList().also { it[lastFilled] = "" }
                                        }
                                    }
                                    "" -> {}
                                    else -> {
                                        val nextEmpty = otpValues.indexOfFirst { it.isEmpty() }
                                        if (nextEmpty >= 0) {
                                            otpValues = otpValues.toMutableList().also { it[nextEmpty] = key }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 64.dp)
            .clickable(enabled = label.isNotEmpty()) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            label == "⌫" -> Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = Color(0xFF1A1A2E),
                modifier = Modifier.size(24.dp)
            )
            label.isEmpty() -> {}
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A2E)
                )
                val subLabel = when (label) {
                    "2" -> "ABC"; "3" -> "DEF"; "4" -> "GHI"; "5" -> "JKL"
                    "6" -> "MNO"; "7" -> "PQRS"; "8" -> "TUV"; "9" -> "WXYZ"
                    else -> ""
                }
                if (subLabel.isNotEmpty()) {
                    Text(text = subLabel, fontSize = 9.sp, color = Color(0xFF9E9E9E), letterSpacing = 1.sp)
                }
            }
        }
    }
}
