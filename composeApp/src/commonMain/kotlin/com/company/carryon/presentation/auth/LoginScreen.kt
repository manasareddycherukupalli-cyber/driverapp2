package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.carryOnWordmarkFontFamily
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Figma design tokens (node 1:258) ────────────────────────
// Black: #16161E | Blue: #034094 | Gray: #828282
private val DesignBlue  = Color(0xFF034094)
private val DesignBlack = Color(0xFF16161E)
private val DesignGray  = Color(0xFF828282)

/**
 * LoginScreen — pixel-accurate recreation of the Figma
 * "SIGN IN [In Active]" screen (node 1:258).
 */
@Composable
fun LoginScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val strings = LocalStrings.current
    var phone           by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }
    val coroutineScope  = rememberCoroutineScope()
    val normalizedPhone = normalizePhoneInput(phone)
    val wordmarkFontFamily = carryOnWordmarkFontFamily()
    val focusManager = LocalFocusManager.current

    val otpVerifyState by authViewModel.otpVerifyState.collectAsState()

    LaunchedEffect(phone) {
        if (isValidPhoneInput(phone)) {
            delay(700)
            focusManager.clearFocus()
        }
    }

    // Reset stale OTP state first, then collect future emissions to avoid navigating
    // to OTP with a Success value left over from a previous login session (e.g. after logout).
    LaunchedEffect(Unit) {
        authViewModel.resetOtpState()
        authViewModel.otpSendState.collect { state ->
            when (state) {
                is UiState.Success -> {
                    isLoading = false
                    navigator.navigateTo(Screen.OtpVerification)
                }
                is UiState.Error -> {
                    isLoading = false
                    errorMessage = (state as UiState.Error).message
                }
                is UiState.Loading -> { isLoading = true }
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        IconButton(
            onClick = {
                val handled = navigator.goBack()
                if (!handled) navigator.navigateAndClearStack(Screen.Onboarding)
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DesignBlack
            )
        }

        Spacer(Modifier.height(60.dp))

        // ── Heading ────────────────────────────────────────────
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = DesignBlack, fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
                    append(strings.welcomeTo)
                }
                withStyle(
                    SpanStyle(
                        color = Color(0xFF2F80ED),
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        fontFamily = wordmarkFontFamily,
                        fontSize = 30.sp,
                        letterSpacing = 0.sp
                    )
                ) { append("CARRY ") }
                withStyle(
                    SpanStyle(
                        color = Color(0xFF034094),
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        fontFamily = wordmarkFontFamily,
                        fontSize = 30.sp,
                        letterSpacing = 0.sp
                    )
                ) { append("ON") }
                withStyle(SpanStyle(color = Color(0xFF333333), fontWeight = FontWeight.Bold, fontSize = 30.sp)) {
                    append("!")
                }
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text     = strings.signInToContinue,
            fontSize = 16.sp,
            color    = DesignBlack.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(52.dp))

        // ── Phone number field ────────────────────────────────
        CarryInputField(
            label         = strings.phoneNumber,
            value         = phone,
            onValueChange = { phone = it },
            placeholder   = strings.phonePlaceholder,
            keyboardType  = KeyboardType.Phone,
            imeAction     = ImeAction.Done,
            onImeAction   = { focusManager.clearFocus() }
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
                authViewModel.sendLoginOtp(normalizedPhone)
            },
            modifier  = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape     = RoundedCornerShape(10.dp),
            enabled   = isValidPhoneInput(phone) && !isLoading,
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
    imeAction        : ImeAction = ImeAction.Default,
    onImeAction      : () -> Unit = {},
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
            keyboardOptions      = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions      = KeyboardActions(onDone = { onImeAction() }),
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
