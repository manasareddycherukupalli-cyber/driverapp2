package com.company.carryon.presentation.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.DeliveryLifecycleCommand
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val PickBlue = Color(0xFF2F80ED)
private val PickSoft = Color(0x4DA6D2F3)
private val PickWhite = Color(0xFFFFFFFF)
private val PickBlack = Color(0xFF000000)
private const val PICKUP_OTP_LENGTH = 4

@Composable
fun PickupInstructionsScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val jobState by viewModel.currentJob.collectAsState()
    val otpError by viewModel.otpError.collectAsState()
    val otpVerifying by viewModel.otpVerifying.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    val jobId = navigator.selectedJobId

    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJob(it) }
    }
    LaunchedEffect(viewModel) {
        viewModel.cancelCompletedEvents.collect {
            navigator.clearPersistedDeliveryState()
            navigator.navigateAndClearStack(Screen.Home)
        }
    }
    LaunchedEffect(jobState) {
        val job = (jobState as? UiState.Success)?.data ?: return@LaunchedEffect
        viewModel.redirectIfCurrentScreenInvalid(Screen.PickupInstructions, job)
    }

    if (jobId == null) {
        ErrorState("No active job selected") { navigator.goBack() }
        return
    }

    when (val state = jobState) {
        is UiState.Loading, UiState.Idle -> LoadingScreen("Loading pickup details...")
        is UiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.loadJob(jobId) }
        )
        is UiState.Success -> PickupInstructionsContent(
            job = state.data,
            navigator = navigator,
            otpError = otpError,
            otpVerifying = otpVerifying,
            cancelError = (cancelState as? UiState.Error)?.message,
            isCancelling = cancelState is UiState.Loading,
            canVerifyPickup = viewModel.canRun(DeliveryLifecycleCommand.VERIFY_PICKUP_OTP, state.data),
            canCancelPickup = viewModel.canCancelBeforePickup(state.data),
            onOtpChanged = { viewModel.clearOtpError() },
            onConfirm = { otp -> viewModel.verifyPickupOtp(state.data.id, otp) },
            onCancelPickup = { viewModel.cancelJob(state.data.id) }
        )
    }
}

@Composable
private fun PickupInstructionsContent(
    job: DeliveryJob,
    navigator: AppNavigator,
    otpError: String?,
    otpVerifying: Boolean,
    cancelError: String?,
    isCancelling: Boolean,
    canVerifyPickup: Boolean,
    canCancelPickup: Boolean,
    onOtpChanged: () -> Unit,
    onConfirm: (String) -> Unit,
    onCancelPickup: () -> Unit
) {
    val checks = remember(job.id) { mutableStateListOf(false, false, false, false) }
    var pickupOtp by remember(job.id) { mutableStateOf("") }
    var showCancelDialog by remember(job.id) { mutableStateOf(false) }
    val allChecksComplete = checks.all { it }
    val displayOrderId = remember(job.id, job.displayOrderId) {
        job.displayOrderId.takeIf { it.isNotBlank() }
            ?: job.id.takeLast(8).uppercase()
    }
    val destinationLabel = job.dropoff.shortAddress.ifBlank { job.dropoff.address }.ifBlank { "--" }
    val instructionsLabel = job.pickup.instructions
        ?.takeIf { it.isNotBlank() }
        ?: job.notes?.takeIf { it.isNotBlank() }
        ?: "No special instructions provided."

    LaunchedEffect(canCancelPickup) {
        if (!canCancelPickup) {
            showCancelDialog = false
        }
    }

    if (showCancelDialog && canCancelPickup) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel pickup?") },
            text = { Text("The job will be returned to the queue for another driver.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancelPickup()
                    }
                ) {
                    Text("Cancel pickup", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep job")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PickWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PickBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PickBlue,
                    modifier = Modifier.size(18.dp).clickable { navigator.goBack() }
                )
                Spacer(Modifier.width(10.dp))
                Text("Pickup Details", color = PickBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Box(
                modifier = Modifier
                    .background(PickSoft, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("LIVE", color = PickBlue, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
            }
        }

        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = PickWhite), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CURRENT ASSIGNMENT", color = PickBlack.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text("Order #$displayOrderId", color = PickBlack, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(30.dp).background(PickSoft, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PickBlue, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Destination", color = PickBlack.copy(alpha = 0.5f), fontSize = 11.sp)
                        Text(destinationLabel, color = PickBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = PickBlue), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("EST. EARNINGS", color = PickWhite.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("RM ${job.estimatedEarnings.toInt()}", color = PickWhite, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
        }

        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = PickSoft), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(30.dp).background(PickBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = PickBlue, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("Special Instructions", color = PickBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        instructionsLabel,
                        color = PickBlack.copy(alpha = 0.75f),
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = PickWhite), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Verification Checklist", color = PickBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.background(PickSoft, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("MANDATORY", color = PickBlue, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                CheckRow("Package sealed securely", checks[0]) { checks[0] = !checks[0] }
                CheckRow("Label information correct", checks[1]) { checks[1] = !checks[1] }
                CheckRow("Fragile sticker attached", checks[2]) { checks[2] = !checks[2] }
                CheckRow("Verify weight with sender", checks[3]) { checks[3] = !checks[3] }
            }
        }

        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = PickWhite), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pickup OTP", color = PickBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                PickupOtpInput(
                    value = pickupOtp,
                    onValueChange = {
                        pickupOtp = it
                        onOtpChanged()
                    },
                    hasError = !otpError.isNullOrBlank()
                )
                Text(
                    "Ask sender for the pickup code",
                    color = PickBlack.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!otpError.isNullOrBlank()) {
                    Text(otpError, color = Color(0xFFCC3D3D), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Button(
            onClick = { onConfirm(pickupOtp) },
            enabled = canVerifyPickup && allChecksComplete && pickupOtp.length == PICKUP_OTP_LENGTH && !otpVerifying && !isCancelling,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PickBlue)
        ) {
            if (otpVerifying) {
                androidx.compose.material3.CircularProgressIndicator(color = PickWhite, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = PickWhite, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Confirm & Start Delivery", fontWeight = FontWeight.Bold)
            }
        }

        if (canCancelPickup) {
            OutlinedButton(
                onClick = { showCancelDialog = true },
                enabled = !otpVerifying && !isCancelling,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.error,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Cancel pickup", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (!cancelError.isNullOrBlank()) {
            Text(cancelError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PickupOtpInput(value: String, onValueChange: (String) -> Unit, hasError: Boolean) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    BasicTextField(
        value = value,
        onValueChange = { raw -> onValueChange(raw.filter { it.isDigit() }.take(PICKUP_OTP_LENGTH)) },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.Transparent,
            fontSize = 1.sp,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        decorationBox = { inner ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(PICKUP_OTP_LENGTH) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(PickWhite, RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (hasError) Color(0xFFCC3D3D) else PickBlue.copy(alpha = 0.35f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                value.getOrNull(index)?.toString().orEmpty(),
                                color = PickBlack,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Box(Modifier.size(1.dp)) {
                    inner()
                }
            }
        }
    )
}

@Composable
private fun CheckRow(text: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .background(PickWhite, RoundedCornerShape(10.dp))
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (checked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (checked) PickBlue else PickBlack.copy(alpha = 0.7f),
            modifier = Modifier
                .size(22.dp)
                .padding(start = 4.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = PickBlack, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
