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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.DeliveryLifecycleCommand
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.CarryBlue
import com.company.carryon.presentation.theme.CarryBlueLight
import com.company.carryon.presentation.theme.Gray900
import com.company.carryon.presentation.theme.Green500
import com.company.carryon.presentation.theme.Red500
import com.company.carryon.presentation.util.telUriFor
import com.company.carryon.presentation.util.rememberImagePickerLauncher
import com.company.carryon.presentation.util.toImageBitmap

private val PODBlue = CarryBlue
private val PODSoft = CarryBlueLight
private val PODCard = CarryBlueLight
private val PODWhite = Color.White
private val PODBlack = Gray900
private val PODBackIcon = Color(0xFF5E6470)

@Composable
fun ProofOfDeliveryScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val proofState by viewModel.proofState.collectAsState()
    val currentJobState by viewModel.currentJob.collectAsState()
    val photoUploadState by viewModel.photoUploadState.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val mapMarkers by viewModel.markers.collectAsState()
    val jobId = navigator.selectedJobId

    if (jobId == null) {
        ErrorState("No active job selected") { navigator.goBack() }
        return
    }

    var photoBytes by remember(jobId) { mutableStateOf<ByteArray?>(null) }
    var recipientPresent by remember(jobId) { mutableStateOf(false) }
    var packageHandover by remember(jobId) { mutableStateOf(false) }
    var captureStatusMessage by remember(jobId) { mutableStateOf<String?>(null) }
    var captureErrorMessage by remember(jobId) { mutableStateOf<String?>(null) }

    val imagePicker = rememberImagePickerLauncher(
        onImagePicked = { bytes ->
            photoBytes = bytes
            captureStatusMessage = "Photo captured. Uploading..."
            captureErrorMessage = null
            viewModel.uploadPhoto(jobId, bytes)
        },
        onImagePickFailed = { message ->
            captureStatusMessage = null
            captureErrorMessage = message
        }
    )

    LaunchedEffect(jobId) {
        viewModel.prepareProofOfDelivery(jobId)
        viewModel.loadJob(jobId)
    }

    val job = when (val state = currentJobState) {
        is UiState.Success -> state.data
        is UiState.Loading, UiState.Idle -> {
            LoadingScreen("Loading delivery details...")
            return
        }
        is UiState.Error -> {
            ErrorState(state.message) { viewModel.loadJob(jobId) }
            return
        }
    }
    LaunchedEffect(job.status) {
        viewModel.redirectIfCurrentScreenInvalid(Screen.ProofOfDelivery, job)
    }

    val recipientName = job.dropoff.contactName?.takeIf { it.isNotBlank() }
        ?: job.customerName.takeIf { it.isNotBlank() }
        ?: "--"
    val orderIdLabel = job.displayOrderId
        .takeIf { it.isNotBlank() }
        ?.let { if (it.startsWith("#")) it else "#$it" }
        ?: "#${jobId.takeLast(7).uppercase()}"
    val canCompleteDelivery = viewModel.canRun(DeliveryLifecycleCommand.COMPLETE_DELIVERY, job)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PODWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PODBackIcon,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { navigator.goBack() }
            )
            Spacer(Modifier.width(10.dp))
            Text("Proof of Delivery", color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(PODBlue.copy(alpha = 0.65f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ORDER ID", color = PODBlack.copy(alpha = 0.52f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(orderIdLabel, color = PODBlue, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
            }
            Box(modifier = Modifier.background(PODSoft, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                Text(
                    "EST. RM${job.estimatedEarnings.toInt()}",
                    color = PODBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text("Proof of Drop-off", color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 32.sp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, PODBlue, RoundedCornerShape(10.dp))
                .clickable(enabled = photoUploadState !is UiState.Loading) {
                    captureStatusMessage = "Opening camera..."
                    captureErrorMessage = null
                    imagePicker.launch()
                },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = PODWhite)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(196.dp)) {
                if (photoBytes != null) {
                    androidx.compose.foundation.Image(
                        bitmap = photoBytes!!.toImageBitmap(),
                        contentDescription = "Proof photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(52.dp).background(PODSoft, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = PODBlue)
                        }
                        Text("Tap to Capture Photo", color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                        Text("Capture the package at the doorstep", color = PODBlack.copy(alpha = 0.7f), fontSize = 16.sp)
                    }
                }

                // Upload overlay
                when (val uploadState = photoUploadState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PODBlack.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(color = PODWhite, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                                Text("Uploading...", color = PODWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                    is UiState.Success -> if (uploadState.data.jobId == jobId) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PODBlack.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(44.dp).background(Green500, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = PODWhite, modifier = Modifier.size(26.dp))
                                }
                                Text("Uploaded", color = PODWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    } else Unit
                    else -> Unit
                }
            }
        }

        when (val uploadState = photoUploadState) {
            is UiState.Success -> if (uploadState.data.jobId == jobId) {
                Text(
                    "Photo uploaded. Tap photo area to retake.",
                    color = Green500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else Unit
            is UiState.Error -> {
                Text(
                    uploadState.message,
                    color = Red500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            else -> {
                if (!captureErrorMessage.isNullOrBlank()) {
                    Text(
                        captureErrorMessage!!,
                        color = Red500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (!captureStatusMessage.isNullOrBlank()) {
                    Text(
                        captureStatusMessage!!,
                        color = PODBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (photoBytes != null) {
                    Text(
                        "Photo captured. Tap photo area to retake.",
                        color = PODBlack.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PODCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Delivery Confirmation", color = PODBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                ConfirmChecklistRow(
                    label = "Recipient present",
                    checked = recipientPresent,
                    onToggle = { recipientPresent = !recipientPresent }
                )
                ConfirmChecklistRow(
                    label = "Package handed over",
                    checked = packageHandover,
                    onToggle = { packageHandover = !packageHandover }
                )
            }
        }

        RecipientContactCard(name = recipientName, phone = job.dropoff.contactPhone ?: job.customerPhone)

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = PODSoft),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val destinationLat = job.dropoff.latitude
            val destinationLng = job.dropoff.longitude
            val hasCoordinates = destinationLat != 0.0 || destinationLng != 0.0

            if (hasCoordinates) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MapViewComposable(
                        modifier = Modifier.fillMaxSize(),
                        centerLat = destinationLat,
                        centerLng = destinationLng,
                        zoom = 14.0,
                        markers = mapMarkers.ifEmpty {
                            listOf(
                                MapMarker(
                                    id = "dropoff",
                                    lat = destinationLat,
                                    lng = destinationLng,
                                    title = "Drop-off",
                                    color = MarkerColor.RED
                                )
                            )
                        },
                        routeGeometry = routeGeometry ?: listOf(LatLng(destinationLat, destinationLng))
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .background(Color(0xAA000000), RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(20.dp).background(PODWhite, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PODBlue, modifier = Modifier.size(12.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(job.dropoff.address.ifBlank { "--" }, color = PODWhite, fontSize = 10.sp)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Drop-off map unavailable",
                        color = PODBlack.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        val hasPhoto = photoBytes != null ||
            (photoUploadState as? UiState.Success)?.data?.jobId == jobId
        val canSubmit = hasPhoto && recipientPresent && packageHandover

        Button(
            onClick = {
                val alreadyUploaded = (photoUploadState as? UiState.Success)?.data?.jobId == jobId
                viewModel.submitProof(
                    jobId = jobId,
                    photoBytes = if (alreadyUploaded) null else photoBytes,
                    recipientName = recipientName
                )
            },
            enabled = canCompleteDelivery && proofState !is UiState.Loading && canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PODBlue)
        ) {
            if (proofState is UiState.Loading) {
                CircularProgressIndicator(color = PODWhite, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text("Complete Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PODWhite, modifier = Modifier.size(16.dp))
            }
        }

        when (val state = proofState) {
            is UiState.Error -> Text(state.message, color = Red500, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            else -> Unit
        }

        Text("By submitting, you confirm delivery completion.", color = PODBlack.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
private fun RecipientContactCard(name: String, phone: String?) {
    val uriHandler = LocalUriHandler.current
    val telUri = telUriFor(phone)
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PODSoft),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("RECIPIENT CONTACT", color = PODBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text(name, color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(phone?.takeIf { it.isNotBlank() } ?: "--", color = PODBlack.copy(alpha = 0.65f), fontSize = 14.sp)
            }
            Button(
                onClick = { telUri?.let(uriHandler::openUri) },
                enabled = telUri != null,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PODBlue)
            ) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = PODWhite, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Call", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ConfirmChecklistRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PODWhite)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(if (checked) PODBlue else Color.Transparent, CircleShape)
                    .border(1.5.dp, if (checked) PODBlue else PODBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = PODWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(label, color = PODBlack, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}
