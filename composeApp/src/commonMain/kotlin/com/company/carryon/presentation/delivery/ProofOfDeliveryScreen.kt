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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.util.rememberImagePickerLauncher
import com.company.carryon.presentation.util.toImageBitmap

private val PODBlue = Color(0xFF5A86E8)
private val PODSoft = Color(0xFFD9E5F7)
private val PODCard = Color(0xFFC9D3E0)
private val PODWhite = Color(0xFFFFFFFF)
private val PODBlack = Color(0xFF1E2530)
private const val RECIPIENT_OTP_LENGTH = 6

@Composable
fun ProofOfDeliveryScreen(navigator: AppNavigator) {
    val viewModel = remember { DeliveryViewModel() }
    val proofState by viewModel.proofState.collectAsState()
    val currentJobState by viewModel.currentJob.collectAsState()
    val deliveryOtpState by viewModel.deliveryOtpState.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val mapMarkers by viewModel.markers.collectAsState()
    val jobId = navigator.selectedJobId

    var photoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var recipientPresent by remember { mutableStateOf(false) }
    var packageHandover by remember { mutableStateOf(false) }
    val otpDigits = remember { mutableStateListOf(*Array(RECIPIENT_OTP_LENGTH) { "" }) }
    var resendInfo by remember { mutableStateOf<String?>(null) }
    var resendError by remember { mutableStateOf<String?>(null) }
    var sendingOtp by remember { mutableStateOf(false) }
    var adminOtpHint by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberImagePickerLauncher { bytes ->
        photoBytes = bytes
        resendError = null
    }

    if (jobId == null) {
        ErrorState("No active job selected") { navigator.goBack() }
        return
    }

    LaunchedEffect(jobId) {
        viewModel.loadJob(jobId)
        viewModel.requestDeliveryOtp(jobId)
    }

    LaunchedEffect(proofState) {
        if (proofState is UiState.Success) {
            navigator.navigateTo(Screen.DeliveryComplete)
        }
    }
    LaunchedEffect(deliveryOtpState) {
        when (val state = deliveryOtpState) {
            is UiState.Loading -> {
                sendingOtp = true
            }
            is UiState.Success -> {
                sendingOtp = false
                resendError = null
                adminOtpHint = state.data.adminOtp
                resendInfo = state.data.recipientEmail.takeIf { it.isNotBlank() }?.let { "OTP sent to $it" }
                    ?: "OTP generated"
            }
            is UiState.Error -> {
                sendingOtp = false
                resendError = state.message
            }
            else -> Unit
        }
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

    val recipientName = job.dropoff.contactName?.takeIf { it.isNotBlank() }
        ?: job.customerName.takeIf { it.isNotBlank() }
        ?: "--"
    val orderIdLabel = job.displayOrderId
        .takeIf { it.isNotBlank() }
        ?.let { if (it.startsWith("#")) it else "#$it" }
        ?: "#${jobId.takeLast(7).uppercase()}"
    val otpCode = otpDigits.joinToString("")
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
                tint = PODBlue,
                modifier = Modifier.clickable { navigator.goBack() }
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
                .clickable { imagePicker.launch() },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = PODWhite)
        ) {
            if (photoBytes != null) {
                androidx.compose.foundation.Image(
                    bitmap = photoBytes!!.toImageBitmap(),
                    contentDescription = "Proof photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
        }

        if (photoBytes != null) {
            Text(
                "Photo captured. Tap photo area to retake.",
                color = PODBlack.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("OTP from Recipient", color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                if (sendingOtp) "SENDING..." else "RESEND OTP",
                color = if (sendingOtp) PODBlack.copy(alpha = 0.45f) else PODBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                modifier = Modifier.clickable(enabled = !sendingOtp) {
                    resendInfo = null
                    resendError = null
                    adminOtpHint = null
                    viewModel.requestDeliveryOtp(jobId)
                }
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(RECIPIENT_OTP_LENGTH) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(Color(0xFFF7F9FD), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE0E6F0), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = otpDigits[index],
                        onValueChange = { raw ->
                            otpDigits[index] = raw.filter { it.isDigit() }.take(1)
                        },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = PODBlack.copy(alpha = 0.5f),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(40.dp),
                        decorationBox = { inner ->
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                if (otpDigits[index].isBlank()) {
                                    Text("0", color = PODBlack.copy(alpha = 0.35f), fontSize = 20.sp)
                                }
                                inner()
                            }
                        }
                    )
                }
            }
        }

        Text(
            "ASK CUSTOMER FOR THE 6-DIGIT CODE",
            color = PODBlack.copy(alpha = 0.55f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        if (!resendInfo.isNullOrBlank()) {
            Text(resendInfo!!, color = PODBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        if (!resendError.isNullOrBlank()) {
            Text(resendError!!, color = Color(0xFFCC3D3D), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        if (!adminOtpHint.isNullOrBlank()) {
            Text(
                "Admin test OTP: $adminOtpHint",
                color = PODBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FD)),
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

        val canSubmit =
            photoBytes != null && recipientPresent && packageHandover && otpCode.length == RECIPIENT_OTP_LENGTH

        Button(
            onClick = {
                viewModel.submitProof(
                    jobId = jobId,
                    photoTaken = photoBytes != null,
                    otpCode = otpCode,
                    recipientName = recipientName
                )
            },
            enabled = proofState !is UiState.Loading && canSubmit,
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
            is UiState.Error -> Text(state.message, color = Color(0xFFCC3D3D), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            else -> Unit
        }

        Text("By submitting, you confirm delivery completion.", color = PODBlack.copy(alpha = 0.5f), fontSize = 10.sp)
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
