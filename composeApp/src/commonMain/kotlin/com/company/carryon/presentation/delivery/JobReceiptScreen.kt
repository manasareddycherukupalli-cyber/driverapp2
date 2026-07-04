package com.company.carryon.presentation.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.ExtraChargeType
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.data.model.isSettlementEligible
import com.company.carryon.data.network.LocationApi
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.util.rememberImagePickerLauncher

@Composable
fun JobReceiptScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val jobState by viewModel.currentJob.collectAsState()
    val extraChargeProofState by viewModel.extraChargeProofState.collectAsState()
    val extraChargeSubmitState by viewModel.extraChargeSubmitState.collectAsState()
    val jobId = navigator.selectedJobId
    val colors = MaterialTheme.colorScheme

    if (jobId == null) {
        ErrorState("No job selected") { navigator.goBack() }
        return
    }

    LaunchedEffect(jobId) {
        viewModel.resetExtraChargeForm()
        viewModel.loadJob(jobId)
    }

    val job = when (val state = jobState) {
        is UiState.Success -> state.data
        is UiState.Loading, UiState.Idle -> {
            LoadingScreen("Loading receipt...")
            return
        }
        is UiState.Error -> {
            ErrorState(state.message) { viewModel.loadJob(jobId) }
            return
        }
    }

    LaunchedEffect(job.status) {
        viewModel.redirectIfCurrentScreenInvalid(Screen.JobReceipt, job)
    }

    val pickupLat = job.pickup.latitude
    val pickupLng = job.pickup.longitude
    val dropLat = job.dropoff.latitude
    val dropLng = job.dropoff.longitude
    val hasCoords = pickupLat != 0.0 || pickupLng != 0.0 || dropLat != 0.0 || dropLng != 0.0
    val centerLat = when {
        pickupLat != 0.0 && dropLat != 0.0 -> (pickupLat + dropLat) / 2
        pickupLat != 0.0 -> pickupLat
        else -> dropLat
    }
    val centerLng = when {
        pickupLng != 0.0 && dropLng != 0.0 -> (pickupLng + dropLng) / 2
        pickupLng != 0.0 -> pickupLng
        else -> dropLng
    }

    val markers = buildList {
        if (pickupLat != 0.0 || pickupLng != 0.0) add(MapMarker("pickup", pickupLat, pickupLng, "Pickup", MarkerColor.GREEN))
        if (dropLat != 0.0 || dropLng != 0.0) add(MapMarker("drop", dropLat, dropLng, "Dropoff", MarkerColor.RED))
    }

    // Fetch route geometry for the blue polyline between pickup and dropoff
    var routeGeometry by remember { mutableStateOf<List<LatLng>?>(null) }
    var extraChargeType by remember { mutableStateOf(ExtraChargeType.TOLL) }
    var extraChargeAmount by remember { mutableStateOf("") }
    var extraChargeNote by remember { mutableStateOf("") }
    var chargeTypeMenuExpanded by remember { mutableStateOf(false) }
    var receiptPickError by remember { mutableStateOf<String?>(null) }
    val receiptPicker = rememberImagePickerLauncher(
        onImagePickFailed = { message ->
            receiptPickError = message
        },
        onImagePicked = { bytes ->
            receiptPickError = null
            viewModel.uploadExtraChargeProof(job.id, bytes)
        }
    )
    LaunchedEffect(pickupLat, pickupLng, dropLat, dropLng) {
        if (pickupLat != 0.0 && dropLat != 0.0) {
            LocationApi.calculateRoute(pickupLat, pickupLng, dropLat, dropLng)
                .onSuccess { result -> if (result.geometry.isNotEmpty()) routeGeometry = result.geometry }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        DriveAppTopBar(
            title = "Receipt",
            onBackClick = { navigator.goBack() },
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
            onProfileClick = { navigator.navigateTo(Screen.Profile) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("ORDER RECEIPT", color = colors.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "#${job.id.takeLast(8).uppercase()}",
                        color = colors.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val dateStr = job.completedAt ?: job.deliveredAt
                    if (!dateStr.isNullOrBlank()) {
                        Text(dateStr, color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }

            if (hasCoords) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    MapViewComposable(
                        modifier = Modifier.fillMaxSize(),
                        centerLat = centerLat,
                        centerLng = centerLng,
                        zoom = 11.0,
                        markers = markers,
                        routeGeometry = routeGeometry
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReceiptDetailRow("From", job.pickup.address.ifBlank { "—" })
                    HorizontalDivider(color = colors.outlineVariant)
                    ReceiptDetailRow("To", job.dropoff.address.ifBlank { "—" })
                }
            }

            val summaryCardShape = RoundedCornerShape(16.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = summaryCardShape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    ),
                shape = summaryCardShape,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("SUMMARY", color = colors.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ReceiptDetailRow(
                        "Earnings",
                        if (job.isSettlementEligible) "RM ${job.estimatedEarnings.toInt()}" else "Pending handover",
                        valueColor = colors.primary,
                        valueBold = true
                    )
                    HorizontalDivider(color = colors.outlineVariant)
                    ReceiptDetailRow("Distance", "${job.distance.toInt()} km")
                    ReceiptDetailRow("Duration", "${job.displayDurationMinutes} min")
                    if (job.packageType.isNotBlank()) {
                        ReceiptDetailRow("Package", job.packageType)
                    }
                    if (job.customerName.isNotBlank()) {
                        ReceiptDetailRow("Customer", job.customerName)
                    }
                }
            }

            ExtraChargeSubmissionCard(
                selectedType = extraChargeType,
                amount = extraChargeAmount,
                note = extraChargeNote,
                proofState = extraChargeProofState,
                submitState = extraChargeSubmitState,
                pickerError = receiptPickError,
                menuExpanded = chargeTypeMenuExpanded,
                onMenuExpandedChange = { chargeTypeMenuExpanded = it },
                onTypeSelected = {
                    extraChargeType = it
                    chargeTypeMenuExpanded = false
                },
                onAmountChange = { extraChargeAmount = it },
                onNoteChange = { extraChargeNote = it },
                onPickReceipt = { receiptPicker.launch() },
                onSubmit = {
                    viewModel.submitExtraCharge(
                        job.id,
                        extraChargeType,
                        extraChargeAmount,
                        extraChargeNote
                    )
                }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReceiptDetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueBold: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = colors.onSurfaceVariant, fontSize = 13.sp)
        Text(
            value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.End)
        )
    }
}

@Composable
private fun ExtraChargeSubmissionCard(
    selectedType: ExtraChargeType,
    amount: String,
    note: String,
    proofState: UiState<String>,
    submitState: UiState<com.company.carryon.data.model.BookingExtraCharge>,
    pickerError: String?,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onTypeSelected: (ExtraChargeType) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onPickReceipt: () -> Unit,
    onSubmit: () -> Unit
) {
    val isUploading = proofState is UiState.Loading
    val isSubmitting = submitState is UiState.Loading
    val submittedCharge = (submitState as? UiState.Success)?.data
    val errorText = pickerError
        ?: (submitState as? UiState.Error)?.message
        ?: (proofState as? UiState.Error)?.message
    val proofUrl = (proofState as? UiState.Success)?.data.orEmpty()
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("TOLL / PARKING", color = colors.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Submit approved pass-through charges with receipt proof.", color = colors.onSurface, fontSize = 13.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Type", color = colors.onSurfaceVariant, fontSize = 12.sp)
                    TextButton(onClick = { onMenuExpandedChange(true) }) {
                        Text(selectedType.displayName, color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { onMenuExpandedChange(false) }
                    ) {
                        ExtraChargeType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = { onTypeSelected(type) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount RM") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Note") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onPickReceipt,
                enabled = !isUploading && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryContainer,
                    contentColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp, color = colors.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Uploading receipt")
                } else {
                    Text(if (proofUrl.isBlank()) "Upload receipt proof" else "Receipt uploaded")
                }
            }

            if (!errorText.isNullOrBlank()) {
                Text(errorText, color = colors.error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            if (submittedCharge != null) {
                Text(
                    "Submitted for admin review: ${submittedCharge.type.displayName} RM ${submittedCharge.amount}",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onSubmit,
                enabled = !isSubmitting && !isUploading && submittedCharge == null,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Submitting")
                } else {
                    Text("Submit for review")
                }
            }
        }
    }
}
