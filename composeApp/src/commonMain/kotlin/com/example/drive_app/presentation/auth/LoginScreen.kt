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

// ── Figma design tokens (node 1:258) ────────────────────────
// Black: #16161E | Blue: #2F80ED | Gray: #828282
private val DesignBlue  = Color(0xFF2F80ED)
private val DesignBlack = Color(0xFF16161E)
private val DesignGray  = Color(0xFF828282)

/**
 * LoginScreen — pixel-accurate recreation of the Figma
 * "SIGN IN [In Active]" screen (node 1:258).
 */
@Composable
fun LoginScreen(navigator: AppNavigator) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 39.dp)
    ) {
        Spacer(Modifier.height(100.dp))

        // ── Heading ────────────────────────────────────────────
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = DesignBlack, fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
                    append("Welcome to ")
                }
                withStyle(SpanStyle(color = DesignBlue, fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
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
            color    = DesignBlack.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(52.dp))

        // ── Email address field ────────────────────────────────
        CarryInputField(
            label         = "Email Address",
            value         = email,
            onValueChange = { email = it },
            placeholder   = "Enter your email",
            keyboardType  = KeyboardType.Email
        )

        Spacer(Modifier.height(25.dp))

        // ── Password field ─────────────────────────────────────
        CarryInputField(
            label            = "Password",
            value            = password,
            onValueChange    = { password = it },
            placeholder      = "Password",
            keyboardType     = KeyboardType.Password,
            isPassword       = true,
            passwordVisible  = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        // ── Forgot Password ────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text       = "Forgot Password ?",
                color      = DesignBlue,
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier
                    .padding(top = 10.dp)
                    .clickable { }
            )
        }

        Spacer(Modifier.height(30.dp))

        // ── Log In button ──────────────────────────────────────
        Button(
            onClick   = { navigator.navigateTo(Screen.LocationPermission) },
            modifier  = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape     = RoundedCornerShape(10.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = DesignBlue),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text       = "Log In",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 18.sp,
                color      = Color.White
            )
        }

        Spacer(Modifier.height(30.dp))

        // ── Or divider ─────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text(
                text     = "Or",
                modifier = Modifier.padding(horizontal = 9.dp),
                color    = DesignGray,
                fontSize = 16.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
        }

        Spacer(Modifier.height(24.dp))

        // ── Social login icons (Apple · Google · Facebook) ─────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            SocialLoginButton(Res.drawable.ic_apple) {}
            Spacer(Modifier.width(10.dp))
            SocialLoginButton(Res.drawable.ic_google) {}
            Spacer(Modifier.width(10.dp))
            SocialLoginButton(Res.drawable.ic_facebook) {}
        }

        Spacer(Modifier.height(20.dp))

        // ── Continue as a Guest ────────────────────────────────
        OutlinedButton(
            onClick  = { navigator.navigateTo(Screen.LocationPermission) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape    = RoundedCornerShape(10.dp),
            border   = BorderStroke(1.dp, DesignBlue),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = DesignBlue)
        ) {
            Text(
                text       = "Continue as a Guest",
                fontWeight = FontWeight.SemiBold,
                fontSize   = 16.sp,
                color      = DesignBlue
            )
        }

        Spacer(Modifier.height(36.dp))

        // ── Sign Up link ───────────────────────────────────────
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text     = "Don't have an Account ?",
                fontSize = 12.sp,
                color    = DesignBlack
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = "Sign up",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = DesignBlue,
                modifier   = Modifier.clickable { navigator.navigateTo(Screen.Registration) }
            )
        }

        Spacer(Modifier.height(48.dp))
    }
}

// ── Input field — white card with subtle drop shadow ─────────
@Composable
private fun CarryInputField(
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
        color      = DesignBlack
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
                Text(
                    text     = placeholder,
                    color    = DesignGray,
                    fontSize = 16.sp
                )
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
                        tint               = DesignGray
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
                focusedTextColor        = DesignBlack,
                unfocusedTextColor      = DesignBlack
            )
        )
    }
}

// ── Social login button — white card 80×60 with shadow ───────
@Composable
private fun SocialLoginButton(
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
