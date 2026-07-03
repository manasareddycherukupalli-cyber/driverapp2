package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
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

// ── Figma design tokens ──────────────────────────────────────
private val RegBlue  = Color(0xFF034094)
private val RegBlack = Color(0xFF16161E)
private val RegGray  = Color(0xFF828282)

/**
 * RegistrationScreen — Sign Up screen using the same Figma
 * design tokens and component patterns as LoginScreen (node 143:3000).
 */
@Composable
fun RegistrationScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val strings = LocalStrings.current
    var name         by remember { mutableStateOf("") }
    var phone        by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val wordmarkFontFamily = carryOnWordmarkFontFamily()
    val focusManager = LocalFocusManager.current

    val otpSendState by authViewModel.otpSendState.collectAsState()
    val otpVerifyState by authViewModel.otpVerifyState.collectAsState()

    LaunchedEffect(phone, name) {
        if (name.isNotBlank() && isValidPhoneInput(phone)) {
            delay(700)
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.resetOtpState()
    }

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

    val normalizedPhone = normalizePhoneInput(phone)
    val signUpEnabled = name.isNotBlank() && isValidPhoneInput(phone)

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
                if (!handled) navigator.navigateAndClearStack(Screen.Login)
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = RegBlack
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Heading ────────────────────────────────────────────
        // Whole heading is one Row (never wraps) at a reduced font size so
        // "Welcome to CARRY ON!" always fits on a single line.
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = strings.welcomeTo,
                color = RegBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                softWrap = false
            )
            Text(
                text = "CARRY ",
                color = Color(0xFF2F80ED),
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontFamily = wordmarkFontFamily,
                fontSize = 20.sp,
                softWrap = false
            )
            Text(
                text = "ON",
                color = Color(0xFF034094),
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontFamily = wordmarkFontFamily,
                fontSize = 20.sp,
                softWrap = false
            )
            Text(
                text = "!",
                color = Color(0xFF333333),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                softWrap = false
            )
        }

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
            placeholder   = strings.enterYourName,
            imeAction     = ImeAction.Next
        )

        Spacer(Modifier.height(25.dp))

        // ── Phone field ────────────────────────────────────────
        RegInputField(
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
                authViewModel.sendSignupOtp(name, normalizedPhone)
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

        Spacer(Modifier.height(28.dp))
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
        color      = RegBlack
    )
    Spacer(Modifier.height(8.dp))
    Surface(
        shape           = RoundedCornerShape(10.dp),
        shadowElevation = 1.2.dp,
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
