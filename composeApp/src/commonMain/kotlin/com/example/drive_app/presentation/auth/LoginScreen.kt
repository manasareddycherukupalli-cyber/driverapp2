package com.example.drive_app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*
import drive_app.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * LoginScreen — Email/password login with social login options.
 * Matches the Carry On design system.
 */
@Composable
fun LoginScreen(navigator: AppNavigator) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        // ---- Heading ----
        Text(
            text = buildAnnotatedString {
                append("Welcome to ")
                withStyle(SpanStyle(color = CarryBlue, fontWeight = FontWeight.Bold)) { append("Carry On") }
                append("!")
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Hello there, Sign in to Continue",
            fontSize = 14.sp,
            color = Color(0xFF6B6B6B)
        )

        Spacer(Modifier.height(32.dp))

        // ---- Email ----
        Text("Email Address", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1A1A2E))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email", color = Color(0xFFAAAAAA)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CarryBlue,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(Modifier.height(16.dp))

        // ---- Password ----
        Text("Password", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1A1A2E))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", color = Color(0xFFAAAAAA)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CarryBlue,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        // ---- Forgot Password ----
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = {}) {
                Text("Forgot Password ?", color = CarryBlue, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // ---- Log In Button ----
        Button(
            onClick = { navigator.navigateTo(Screen.LocationPermission) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CarryBlue)
        ) {
            Text("Log In", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(20.dp))

        // ---- Or Divider ----
        OrDivider()

        Spacer(Modifier.height(20.dp))

        // ---- Social Login Icons ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialIconButton(Res.drawable.ic_apple) {}
            Spacer(Modifier.width(20.dp))
            SocialIconButton(Res.drawable.ic_google) {}
            Spacer(Modifier.width(20.dp))
            SocialIconButton(Res.drawable.ic_facebook) {}
        }

        Spacer(Modifier.height(20.dp))

        // ---- Continue as Guest ----
        OutlinedButton(
            onClick = { navigator.navigateTo(Screen.LocationPermission) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CarryBlue)
        ) {
            Text("Continue as a Guest", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = CarryBlue)
        }

        Spacer(Modifier.height(24.dp))

        // ---- Sign Up Link ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an Account ? ", fontSize = 14.sp, color = Color(0xFF6B6B6B))
            Text(
                text = "Sign up",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CarryBlue,
                modifier = Modifier.clickable { navigator.navigateTo(Screen.Registration) }
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
        Text(
            text = "  Or  ",
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
    }
}

@Composable
private fun SocialIconButton(resource: org.jetbrains.compose.resources.DrawableResource, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFFF5F5F5))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            contentScale = ContentScale.Fit
        )
    }
}
