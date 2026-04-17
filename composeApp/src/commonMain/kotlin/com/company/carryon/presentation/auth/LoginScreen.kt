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
import com.company.carryon.getPlatform
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import kotlinx.coroutines.launch
import drive_app.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// ── Figma design tokens (node 1:258) ────────────────────────
// Black: #16161E | Blue: #2F80ED | Gray: #828282
private val DesignBlue  = Color(0xFF2F80ED)
private val DesignBlack = Color(0xFF16161E)
private val DesignGray  = Color(0xFF828282)

private fun mapAuthErrorMessage(error: Throwable): String {
    val message = error.message.orEmpty()
    return when {
        message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("No address associated with hostname", ignoreCase = true) ->
            "Cannot reach auth server (DNS/network issue). If using emulator, verify internet and set Android Private DNS to Automatic/Off."
        message.contains("Request timeout", ignoreCase = true) ||
            message.contains("request_timeout", ignoreCase = true) ->
            "Request timed out. Please check your internet and try again."
        else -> message.ifBlank { "Failed to send OTP" }
    }
}

/**
 * LoginScreen — pixel-accurate recreation of the Figma
 * "SIGN IN [In Active]" screen (node 1:258).
 */
@Composable
fun LoginScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val strings = LocalStrings.current
    var email           by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }
    var awaitingGoogleAuth by remember { mutableStateOf(false) }
    val coroutineScope  = rememberCoroutineScope()

    val otpSendState by authViewModel.otpSendState.collectAsState()
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

    // Listen for session changes (handles iOS OAuth callback)
    LaunchedEffect(Unit) {
        SupabaseConfig.client.auth.sessionStatus.collect { status ->
            if (awaitingGoogleAuth && status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
                awaitingGoogleAuth = false
                val session = SupabaseConfig.client.auth.currentSessionOrNull()
                if (session != null && !isLoading) {
                    authViewModel.authFlowType = AuthFlowType.LOGIN
                    authViewModel.driverEmail = session.user?.email ?: ""
                    authViewModel.onSupabaseTokenReceived(session.accessToken)
                }
            }
        }
    }

    // ── Google Sign-In via Supabase ComposeAuth ──────────────
    val googleSignInAction = SupabaseConfig.client.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    awaitingGoogleAuth = false
                    coroutineScope.launch {
                        val session = SupabaseConfig.client.auth.currentSessionOrNull()
                        if (session != null) {
                            authViewModel.authFlowType = AuthFlowType.LOGIN
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
                    errorMessage = strings.networkError
                }
            }
        }
    )

    LaunchedEffect(otpSendState) {
        when (otpSendState) {
            is UiState.Success -> {
                isLoading = false
                navigator.navigateTo(Screen.OtpVerification)
            }
            is UiState.Error -> {
                isLoading = false
                errorMessage = (otpSendState as UiState.Error).message
            }
            is UiState.Loading -> { isLoading = true }
            else -> {}
        }
    }

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
                    append(strings.welcomeTo)
                }
                withStyle(SpanStyle(color = DesignBlue, fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
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
            color    = DesignBlack.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(52.dp))

        // ── Email address field ────────────────────────────────
        CarryInputField(
            label         = strings.emailAddress,
            value         = email,
            onValueChange = { email = it },
            placeholder   = strings.enterYourEmail,
            keyboardType  = KeyboardType.Email
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = strings.otpHint,
            fontSize = 13.sp,
            color = DesignGray
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                fontSize = 13.sp,
                color = Color(0xFFE53935)
            )
        }

        Spacer(Modifier.height(30.dp))

        // ── Send OTP button ──────────────────────────────────
        Button(
            onClick = {
                errorMessage = null
                authViewModel.authFlowType = AuthFlowType.LOGIN
                authViewModel.driverEmail = email
                authViewModel.setOtpLoading()
                coroutineScope.launch {
                    try {
                        SupabaseConfig.client.auth.signInWith(OTP) {
                            this.email = email
                        }
                        authViewModel.onOtpSent(email)
                    } catch (e: Exception) {
                        authViewModel.onOtpSendError(mapAuthErrorMessage(e))
                    }
                }
            },
            modifier  = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape     = RoundedCornerShape(10.dp),
            enabled   = email.isNotBlank() && !isLoading,
            colors    = ButtonDefaults.buttonColors(
                containerColor = DesignBlue,
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
                    text       = strings.sendVerificationCode,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 18.sp,
                    color      = Color.White
                )
            }
        }

        Spacer(Modifier.height(30.dp))

        // ── Or divider ─────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text(
                text     = strings.or,
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
            SocialLoginButton(Res.drawable.ic_google) {
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
            SocialLoginButton(Res.drawable.ic_facebook) {}
        }

        Spacer(Modifier.height(36.dp))

        // ── Sign Up link ───────────────────────────────────────
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text     = strings.dontHaveAccount,
                fontSize = 12.sp,
                color    = DesignBlack
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = strings.signUpLink,
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
