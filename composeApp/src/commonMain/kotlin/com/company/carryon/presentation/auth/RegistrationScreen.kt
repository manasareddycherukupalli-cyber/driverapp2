package com.company.carryon.presentation.auth

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
import com.company.carryon.data.model.UiState
import com.company.carryon.data.network.SupabaseConfig
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.*
import com.company.carryon.getPlatform
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// ── Figma design tokens ──────────────────────────────────────
private val RegBlue  = Color(0xFF2F80ED)
private val RegBlack = Color(0xFF16161E)
private val RegGray  = Color(0xFF828282)

private fun mapAuthErrorMessage(error: Throwable): String {
    val message = error.message.orEmpty()
    return when {
        message.contains("Request timeout", ignoreCase = true) ||
            message.contains("request_timeout", ignoreCase = true) ->
            "Request timed out. Please check your internet and try again."
        else -> message.ifBlank { "Failed to send OTP" }
    }
}

/**
 * RegistrationScreen — Sign Up screen using the same Figma
 * design tokens and component patterns as LoginScreen (node 143:3000).
 */
@Composable
fun RegistrationScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val strings = LocalStrings.current
    var name         by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var phone        by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var awaitingGoogleAuth by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val otpVerifyState by authViewModel.otpVerifyState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.resetOtpState()
    }

    // Navigate after Google sign-in completes (skips OTP screen)
    LaunchedEffect(otpVerifyState) {
        when (val state = otpVerifyState) {
            is UiState.Success -> {
                isLoading = false
                val screen = authViewModel.determinePostAuthScreen(state.data)
                navigator.navigateTo(screen)
            }
            is UiState.Error -> {
                isLoading = false
                errorMessage = state.message
            }
            is UiState.Loading -> { isLoading = true }
            else -> {}
        }
    }

    val signUpEnabled = name.isNotBlank() && email.isNotBlank()

    // Listen for session changes (handles iOS OAuth callback)
    LaunchedEffect(Unit) {
        SupabaseConfig.client.auth.sessionStatus.collect { status ->
            if (awaitingGoogleAuth && status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
                awaitingGoogleAuth = false
                val session = SupabaseConfig.client.auth.currentSessionOrNull()
                if (session != null && !isLoading) {
                    authViewModel.authFlowType = AuthFlowType.SIGNUP
                    authViewModel.driverEmail = session.user?.email ?: ""
                    authViewModel.onSupabaseTokenReceived(session.accessToken)
                }
            }
        }
    }

    // Google Sign-In via Supabase ComposeAuth
    val googleSignInAction = SupabaseConfig.client.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    awaitingGoogleAuth = false
                    coroutineScope.launch {
                        val session = SupabaseConfig.client.auth.currentSessionOrNull()
                        if (session != null) {
                            authViewModel.authFlowType = AuthFlowType.SIGNUP
                            authViewModel.driverEmail = session.user?.email ?: ""
                            authViewModel.onSupabaseTokenReceived(session.accessToken)
                        }
                    }
                }
                is NativeSignInResult.Error -> {
                    awaitingGoogleAuth = false
                    errorMessage = result.message
                }
                is NativeSignInResult.ClosedByUser -> { awaitingGoogleAuth = false }
                is NativeSignInResult.NetworkError -> {
                    awaitingGoogleAuth = false
                    errorMessage = "Network error. Please check your connection."
                }
            }
        }
    )

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
                    append(strings.welcomeTo)
                }
                withStyle(SpanStyle(color = RegBlue, fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
                    append(strings.appName)
                }
                withStyle(SpanStyle(color = Color(0xFF333333), fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
                    append("!")
                }
            }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text     = strings.signInToContinue,
            fontSize = 16.sp,
            color    = RegBlack.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(25.dp))

        // ── Name field ────────────────────────────────────────
        RegInputField(
            label         = strings.name,
            value         = name,
            onValueChange = { name = it },
            placeholder   = strings.enterYourName
        )

        Spacer(Modifier.height(25.dp))

        // ── Email field ────────────────────────────────────────
        RegInputField(
            label         = strings.emailAddress,
            value         = email,
            onValueChange = { email = it },
            placeholder   = strings.enterYourEmail,
            keyboardType  = KeyboardType.Email
        )

        Spacer(Modifier.height(25.dp))

        // ── Phone field ────────────────────────────────────────
        RegInputField(
            label         = strings.phoneNumber,
            value         = phone,
            onValueChange = { phone = it },
            placeholder   = strings.phonePlaceholder,
            keyboardType  = KeyboardType.Phone
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = strings.otpHint,
            fontSize = 13.sp,
            color = RegGray
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                fontSize = 13.sp,
                color = Color(0xFFE53935)
            )
        }

        Spacer(Modifier.height(40.dp))

        // ── Sign Up button ─────────────────────────────────────
        Button(
            onClick = {
                errorMessage = null
                isLoading = true
                authViewModel.authFlowType = AuthFlowType.SIGNUP
                authViewModel.driverName = name
                authViewModel.driverEmail = email
                authViewModel.driverPhone = phone
                coroutineScope.launch {
                    try {
                        SupabaseConfig.client.auth.signInWith(OTP) {
                            this.email = email
                        }
                        authViewModel.onOtpSent(email)
                        navigator.navigateTo(Screen.OtpVerification)
                    } catch (e: Exception) {
                        errorMessage = mapAuthErrorMessage(e)
                        isLoading = false
                    }
                }
            },
            modifier  = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape     = RoundedCornerShape(10.dp),
            enabled   = signUpEnabled && !isLoading,
            colors    = ButtonDefaults.buttonColors(
                containerColor         = RegBlue,
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text       = strings.signUpButton,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 18.sp
                )
            }
        }

        Spacer(Modifier.height(30.dp))   // outer gap-[30px] between form and options

        // ── Or divider ─────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text(
                text     = strings.or,
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
            RegSocialButton(Res.drawable.ic_google) {
                if (getPlatform().name.startsWith("Android")) {
                    awaitingGoogleAuth = true
                    googleSignInAction.startFlow()
                } else {
                    // iOS: use OAuth web flow
                    coroutineScope.launch {
                        try {
                            awaitingGoogleAuth = true
                            SupabaseConfig.client.auth.signInWith(Google)
                        } catch (e: Exception) {
                            awaitingGoogleAuth = false
                            errorMessage = e.message ?: "Google sign-in failed"
                        }
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            RegSocialButton(Res.drawable.ic_facebook) {}
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
