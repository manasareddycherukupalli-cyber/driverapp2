package com.company.carryon.presentation.delivery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.company.carryon.data.model.*
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*
import com.company.carryon.data.model.LatLng

/**
 * ActiveDeliveryScreen — Step-by-step delivery progress UI.
 * Shows current status, route info, customer details, and action button.
 */
@Composable
fun ActiveDeliveryScreen(navigator: AppNavigator) {
    val viewModel = remember { DeliveryViewModel() }
    val jobState by viewModel.currentJob.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    val jobId = navigator.selectedJobId
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJob(it) }
    }

    LaunchedEffect(cancelState) {
        when (val state = cancelState) {
            is UiState.Success -> navigator.goBack()
            is UiState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            DriveAppTopBar(
                title = "Active Delivery",
                onBackClick = { navigator.goBack() }
            )

            val routeGeometry by viewModel.routeGeometry.collectAsState()
            val mapMarkers by viewModel.markers.collectAsState()

            when (val state = jobState) {
                is UiState.Loading -> LoadingScreen("Loading delivery details...")
                is UiState.Error -> ErrorState(state.message)
                is UiState.Success -> ActiveDeliveryContent(
                    job = state.data,
                    viewModel = viewModel,
                    navigator = navigator,
                    routeGeometry = routeGeometry,
                    mapMarkers = mapMarkers
                )
                is UiState.Idle -> LoadingScreen()
            }
        }
    }
}

@Composable
private fun ActiveDeliveryContent(
    job: DeliveryJob,
    viewModel: DeliveryViewModel,
    navigator: AppNavigator,
    routeGeometry: List<LatLng>?,
    mapMarkers: List<MapMarker>
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val cancelState by viewModel.cancelState.collectAsState()
    val isCancelling = cancelState is UiState.Loading

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Delivery?") },
            text = { Text("The job will be re-assigned to another driver.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelJob(job.id)
                    }
                ) { Text("Cancel Delivery", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep Job") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---- Google Maps ----
        MapViewComposable(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            styleUrl = "",
            markers = mapMarkers,
            routeGeometry = routeGeometry
        )

        // ---- Delivery Progress Steps ----
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Delivery Progress",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(16.dp))

                val steps = listOf(
                    "Accepted" to JobStatus.ACCEPTED,
                    "Heading to Pickup" to JobStatus.HEADING_TO_PICKUP,
                    "Arrived at Pickup" to JobStatus.ARRIVED_AT_PICKUP,
                    "Package Picked Up" to JobStatus.PICKED_UP,
                    "In Transit" to JobStatus.IN_TRANSIT,
                    "Arrived at Drop-off" to JobStatus.ARRIVED_AT_DROP,
                    "Delivered" to JobStatus.DELIVERED,
                )

                steps.forEachIndexed { index, (label, status) ->
                    val isCompleted = job.status.ordinal > status.ordinal
                    val isCurrent = job.status == status
                    val isLast = index == steps.size - 1

                    DeliveryStep(
                        label = label,
                        isCompleted = isCompleted,
                        isCurrent = isCurrent,
                        showConnector = !isLast
                    )
                }
            }
        }

        // ---- Current Step Info ----
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val isPickupPhase = job.status.ordinal <= JobStatus.ARRIVED_AT_PICKUP.ordinal
                val locationInfo = if (isPickupPhase) job.pickup else job.dropoff

                Text(
                    text = if (isPickupPhase) "📍 Pickup Location" else "🏁 Drop-off Location",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = locationInfo.address,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                locationInfo.contactName?.let { name ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Orange500, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(name, fontSize = 14.sp)
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = Orange500, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(locationInfo.contactPhone ?: "", fontSize = 14.sp, color = Blue500)
                    }
                }
                locationInfo.instructions?.let { instructions ->
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Yellow100),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "📝 $instructions",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 13.sp,
                            color = Dark900
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- Action Button / OTP Verification ----
        if (job.status == JobStatus.ARRIVED_AT_PICKUP) {
            // OTP verification section
            var otpInput by remember { mutableStateOf("") }
            val otpError by viewModel.otpError.collectAsState()
            val otpVerifying by viewModel.otpVerifying.collectAsState()
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Verify Pickup OTP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Ask the customer for their 4-digit code",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                otpInput = it
                                viewModel.clearOtpError()
                                if (it.length == 4) {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            }
                        },
                        label = { Text("Enter OTP") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true,
                        isError = otpError != null,
                        supportingText = otpError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    PrimaryButton(
                        text = if (otpVerifying) "Verifying..." else "Verify & Pick Up",
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (otpInput.length == 4 && !otpVerifying) {
                                viewModel.verifyPickupOtp(job.id, otpInput)
                            }
                        }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (job.status == JobStatus.ARRIVED_AT_DROP) {
                    PrimaryButton(
                        text = "Submit Proof of Delivery ✓",
                        onClick = {
                            navigator.selectedJobId = job.id
                            navigator.navigateTo(Screen.ProofOfDelivery)
                        }
                    )
                } else {
                    val nextStatus = viewModel.getNextStatus(job.status)
                    if (nextStatus != null) {
                        PrimaryButton(
                            text = viewModel.getActionText(job.status),
                            onClick = { viewModel.updateStatus(job.id, nextStatus) }
                        )
                    }
                }
            }
        }

        // Navigate button
        if (job.status != JobStatus.DELIVERED) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SecondaryButton(
                    text = "Open Navigation 🗺️",
                    onClick = { navigator.navigateTo(Screen.MapNavigation) }
                )
            }
        }

        // Cancel button — only before pickup (ACCEPTED or HEADING_TO_PICKUP)
        if (job.status == JobStatus.ACCEPTED || job.status == JobStatus.HEADING_TO_PICKUP) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    enabled = !isCancelling,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isCancelling) "Cancelling..." else "Cancel Delivery")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DeliveryStep(
    label: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    showConnector: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Green500
                            isCurrent -> Orange500
                            else -> Gray300
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else if (isCurrent) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                }
            }
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(if (isCompleted) Green500 else Gray300)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            color = when {
                isCurrent -> Orange500
                isCompleted -> Green500
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
