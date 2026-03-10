package com.example.drive_app.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// ── Figma design tokens ──────────────────────────────────────
private val RegBlue  = Color(0xFF2F80ED)
private val RegBlack = Color(0xFF16161E)
private val RegGray  = Color(0xFF828282)

/**
 * RegistrationScreen — Sign Up screen using the same Figma
 * design tokens and component patterns as LoginScreen (node 143:3000).
 */
@Composable
fun RegistrationScreen(navigator: AppNavigator) {
    var name                  by remember { mutableStateOf("") }
    var email                 by remember { mutableStateOf("") }
    var password              by remember { mutableStateOf("") }
    var confirmPassword       by remember { mutableStateOf("") }
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordMismatchError  by remember { mutableStateOf(false) }

    // Enable once all four fields have content
    val signUpEnabled = name.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 39.dp)
    ) {
        Spacer(Modifier.height(35.dp))

        // ── Heading ────────────────────────────────────────────
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = RegBlack, fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
                    append("Welcome to ")
                }
                withStyle(SpanStyle(color = RegBlue, fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
                    append("Carry On")
                }
                withStyle(SpanStyle(color = Color(0xFF333333), fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
                    append("!")
                }
            }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text     = "Hello there, Sign in to Continue",
            fontSize = 16.sp,
            color    = RegBlack.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(25.dp))

        // ── Name field ────────────────────────────────────────
        RegInputField(
            label         = "Name",
            value         = name,
            onValueChange = { name = it },
            placeholder   = "Enter your Name"
        )

        Spacer(Modifier.height(25.dp))

        // ── Email field ────────────────────────────────────────
        RegInputField(
            label         = "Email Address",
            value         = email,
            onValueChange = { email = it },
            placeholder   = "Enter your email",
            keyboardType  = KeyboardType.Email
        )

        Spacer(Modifier.height(25.dp))

        // ── Password field ─────────────────────────────────────
        RegInputField(
            label            = "Password",
            value            = password,
            onValueChange    = { password = it },
            placeholder      = "Password",
            keyboardType     = KeyboardType.Password,
            isPassword       = true,
            passwordVisible  = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Spacer(Modifier.height(25.dp))

        // ── Confirm Password field ─────────────────────────────
        RegInputField(
            label            = "Confirm Password",
            value            = confirmPassword,
            onValueChange    = { confirmPassword = it; passwordMismatchError = false },
            placeholder      = "Password",
            keyboardType     = KeyboardType.Password,
            isPassword       = true,
            passwordVisible  = confirmPasswordVisible,
            onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible }
        )

        if (passwordMismatchError) {
            Spacer(Modifier.height(6.dp))
            Text(
                text     = "Passwords do not match",
                color    = Color(0xFFE53935),
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        // ── Sign Up button ─────────────────────────────────────
        Button(
            onClick = {
                if (password != confirmPassword) {
                    passwordMismatchError = true
                } else {
                    passwordMismatchError = false
                    navigator.navigateTo(Screen.OtpVerification)
                }
            },
            modifier  = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape     = RoundedCornerShape(10.dp),
            enabled   = signUpEnabled,
            colors    = ButtonDefaults.buttonColors(
                containerColor         = RegBlue,
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text       = "Sign Up",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 18.sp
            )
        }

        Spacer(Modifier.height(30.dp))   // outer gap-[30px] between form and options

        // ── Or divider ─────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text(
                text     = "Or",
                modifier = Modifier.padding(horizontal = 9.dp),
                color    = RegGray,
                fontSize = 16.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
        }

        Spacer(Modifier.height(20.dp))

        // ── Social icons (Apple · Google · Facebook) ──────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            RegSocialButton(Res.drawable.ic_apple) {}
            Spacer(Modifier.width(10.dp))
            RegSocialButton(Res.drawable.ic_google) {}
            Spacer(Modifier.width(10.dp))
            RegSocialButton(Res.drawable.ic_facebook) {}
        }

        Spacer(Modifier.height(20.dp))

        // ── Continue as a Guest ────────────────────────────────
        OutlinedButton(
            onClick  = { navigator.navigateAndClearStack(Screen.Home) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape    = RoundedCornerShape(10.dp),
            border   = BorderStroke(1.dp, RegBlue),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = RegBlue)
        ) {
            Text(
                text       = "Continue as a Guest",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 16.sp,
                color      = RegBlue
            )
        }

        Spacer(Modifier.height(28.dp))   // bottom padding
    }
}

// ── Input field — white card with drop shadow ────────────────
@Composable
private fun RegInputField(
    label            : String,
    value            : String,
    onValueChange    : (String) -> Unit,
    placeholder      : String,
    keyboardType     : KeyboardType = KeyboardType.Text,
    isPassword       : Boolean      = false,
    passwordVisible  : Boolean      = false,
    onTogglePassword : (() -> Unit)? = null
) {
    Text(
        text       = label,
        fontWeight = FontWeight.Medium,
        fontSize   = 18.sp,
        color      = RegBlack
    )
    Spacer(Modifier.height(8.dp))
    Surface(
        shape           = RoundedCornerShape(10.dp),
        shadowElevation = 4.dp,
        tonalElevation  = 0.dp,
        color           = Color.White,
        modifier        = Modifier
            .fillMaxWidth()
            .height(55.dp)
    ) {
        TextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(text = placeholder, color = RegGray, fontSize = 16.sp)
            },
            modifier             = Modifier.fillMaxSize(),
            singleLine           = true,
            keyboardOptions      = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {{
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        imageVector        = if (passwordVisible) Icons.Filled.Visibility
                                             else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password"
                                             else "Show password",
                        tint               = RegGray
                    )
                }
            }} else null,
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor  = Color.White,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor  = Color.Transparent,
                focusedTextColor        = RegBlack,
                unfocusedTextColor      = RegBlack
            )
        )
    }
}

// ── Social login button — white card 80×60 with shadow ───────
@Composable
private fun RegSocialButton(
    resource : DrawableResource,
    onClick  : () -> Unit
) {
    Surface(
        shape           = RoundedCornerShape(10.dp),
        shadowElevation = 4.dp,
        tonalElevation  = 0.dp,
        color           = Color.White,
        modifier        = Modifier
            .width(80.dp)
            .height(60.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter            = painterResource(resource),
                contentDescription = null,
                modifier           = Modifier.size(32.dp),
                contentScale       = ContentScale.Fit
            )
        }
    }
}
