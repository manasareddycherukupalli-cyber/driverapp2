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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import com.company.carryon.presentation.util.rememberImagePickerLauncher
import com.company.carryon.presentation.util.toImageBitmap

private val PickBlue = Color(0xFF034094)
private val PickSoft = Color(0x4DA6D2F3)
private val PickWhite = Color(0xFFFFFFFF)
private val PickBlack = Color(0xFF000000)

@Composable
fun PickupInstructionsScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val jobState by viewModel.currentJob.collectAsState()
    val otpVerifying by viewModel.otpVerifying.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    val pickupPhotoUploadState by viewModel.pickupPhotoUploadState.collectAsState()
    val jobId = navigator.selectedJobId

    LaunchedEffect(jobId) {
        jobId?.let {
            viewModel.loadJob(it)
            viewModel.preparePickupProof(it)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.cancelCompletedEvents.collect {
            navigator.clearPersistedDeliveryState()
            navigator.navigateAndClearStack(Screen.Home)
            viewModel.resetCancelState()
        }
    }
    LaunchedEffect(cancelState) {
        if (cancelState is UiState.Success) {
            navigator.clearPersistedDeliveryState()
            navigator.navigateAndClearStack(Screen.Home)
            viewModel.resetCancelState()
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
            otpVerifying = otpVerifying,
            cancelError = (cancelState as? UiState.Error)?.message,
            isCancelling = cancelState is UiState.Loading,
            canVerifyPickup = viewModel.canRun(DeliveryLifecycleCommand.VERIFY_PICKUP_OTP, state.data),
            canCancelPickup = viewModel.canCancelBeforePickup(state.data),
            photoUploadState = pickupPhotoUploadState,
            onCapturePhoto = { bytes -> viewModel.uploadPickupPhoto(state.data.id, bytes) },
            onConfirm = { photoUrl -> viewModel.confirmPickup(state.data.id, photoUrl) },
            onCancelPickup = { viewModel.cancelJob(state.data.id) }
        )
    }
}

@Composable
private fun PickupInstructionsContent(
    job: DeliveryJob,
    navigator: AppNavigator,
    otpVerifying: Boolean,
    cancelError: String?,
    isCancelling: Boolean,
    canVerifyPickup: Boolean,
    canCancelPickup: Boolean,
    photoUploadState: UiState<ProofPhotoUpload>,
    onCapturePhoto: (ByteArray) -> Unit,
    onConfirm: (String?) -> Unit,
    onCancelPickup: () -> Unit
) {
    val checks = remember(job.id) { mutableStateListOf(false, false, false, false) }
    var showCancelDialog by remember(job.id) { mutableStateOf(false) }
    var photoBytes by remember(job.id) { mutableStateOf<ByteArray?>(null) }
    var captureError by remember(job.id) { mutableStateOf<String?>(null) }
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

    val imagePicker = rememberImagePickerLauncher(
        onImagePicked = { bytes ->
            photoBytes = bytes
            captureError = null
            onCapturePhoto(bytes)
        },
        onImagePickFailed = { message -> captureError = message }
    )
    val uploadedPhotoUrl = (photoUploadState as? UiState.Success)?.data?.takeIf { it.jobId == job.id }?.url
    val hasPhoto = photoBytes != null || uploadedPhotoUrl != null

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

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PickWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, PickBlue, RoundedCornerShape(14.dp))
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Proof of Pickup", color = PickBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PickSoft)
                        .clickable(enabled = photoUploadState !is UiState.Loading) {
                            captureError = null
                            imagePicker.launch()
                        }
                ) {
                    if (photoBytes != null) {
                        androidx.compose.foundation.Image(
                            bitmap = photoBytes!!.toImageBitmap(),
                            contentDescription = "Pickup proof photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.size(44.dp).background(PickWhite, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = PickBlue)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Tap to capture the package", color = PickBlack.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (photoUploadState is UiState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PickBlack.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PickWhite, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                        }
                    } else if (uploadedPhotoUrl != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                                .background(Color(0xFF2E7D32), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = PickWhite, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                when {
                    !captureError.isNullOrBlank() -> Text(captureError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    photoUploadState is UiState.Error -> Text(photoUploadState.message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    hasPhoto -> Text("Photo captured. Tap to retake.", color = PickBlack.copy(alpha = 0.6f), fontSize = 12.sp)
                    else -> Text("Required before starting delivery.", color = PickBlack.copy(alpha = 0.6f), fontSize = 12.sp)
                }
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

        val instructionsShape = RoundedCornerShape(14.dp)
        Card(
            shape = instructionsShape,
            colors = CardDefaults.cardColors(containerColor = PickWhite),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 10.dp,
                    shape = instructionsShape,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
        ) {
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
                    Box(modifier = Modifier.background(PickBlue, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("MANDATORY", color = PickWhite, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                CheckRow("Package sealed securely", checks[0]) { checks[0] = !checks[0] }
                CheckRow("Label information correct", checks[1]) { checks[1] = !checks[1] }
                CheckRow("Fragile sticker attached", checks[2]) { checks[2] = !checks[2] }
                CheckRow("Sender confirms package handover", checks[3]) { checks[3] = !checks[3] }
            }
        }

        Button(
            onClick = { onConfirm(uploadedPhotoUrl) },
            enabled = canVerifyPickup && allChecksComplete && uploadedPhotoUrl != null && !otpVerifying && !isCancelling,
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
