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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.navigation.AppNavigator

private val PODBlue = Color(0xFF2F80ED)
private val PODSoft = Color(0x4DA6D2F3)
private val PODWhite = Color(0xFFFFFFFF)
private val PODBlack = Color(0xFF000000)

@Composable
fun ProofOfDeliveryScreen(navigator: AppNavigator) {
    val viewModel = remember { DeliveryViewModel() }
    val proofState by viewModel.proofState.collectAsState()
    val jobId = navigator.selectedJobId ?: "CD-92841"

    var photoTaken by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    var recipientPresent by remember { mutableStateOf(true) }
    var packageHandover by remember { mutableStateOf(true) }

    LaunchedEffect(proofState) {
        if (proofState is UiState.Success) {
            navigator.switchToJobsTab(tabIndex = 2)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PODWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PODBlue, modifier = Modifier.clickable { navigator.goBack() })
            Spacer(Modifier.width(10.dp))
            Text("Proof of Delivery", color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("ORDER ID", color = PODBlack.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text("#${jobId.takeLast(7).uppercase()}", color = PODBlue, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            Box(modifier = Modifier.background(PODSoft, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                Text("EST. $14.50", color = PODBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Text("Proof of Drop-off", color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PODBlue, RoundedCornerShape(10.dp))
                .clickable { photoTaken = true },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = PODWhite)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(52.dp).background(PODSoft, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = PODBlue)
                }
                Text(if (photoTaken) "Photo Captured" else "Take Photo", color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Capture the package at the doorstep", color = PODBlack.copy(alpha = 0.65f), fontSize = 14.sp)
            }
        }

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = PODSoft), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Delivery Confirmation", color = PODBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                ConfirmRow("Recipient present", recipientPresent) { recipientPresent = !recipientPresent }
                ConfirmRow("Package handed over", packageHandover) { packageHandover = !packageHandover }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("OTP from Recipient", color = PODBlack, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("RESEND OTP", color = PODBlue, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        }

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) otp = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("•   •   •   •", color = PODBlack.copy(alpha = 0.4f), fontSize = 16.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(10.dp)
        )

        Text("ASK CUSTOMER FOR THE 4-DIGIT CODE", color = PODBlack.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

        Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = PODBlack.copy(alpha = 0.45f)), modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(20.dp).background(PODWhite, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = PODBlue, modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("2440 N Kedzie Blvd, Chicago", color = PODWhite, fontSize = 10.sp)
                }
            }
        }

        Button(
            onClick = {
                viewModel.submitProof(
                    jobId = jobId,
                    photoTaken = photoTaken,
                    otpCode = otp,
                    recipientName = "Nurul Ain"
                )
            },
            enabled = photoTaken && recipientPresent && packageHandover && otp.length == 4 && proofState !is UiState.Loading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PODBlue)
        ) {
            Text("Complete Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PODWhite, modifier = Modifier.size(16.dp))
        }

        Text("By submitting, you confirm delivery completion.", color = PODBlack.copy(alpha = 0.5f), fontSize = 10.sp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PODWhite, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniTab("ROUTE", false)
            MiniTab("TASKS", true)
            MiniTab("EARNINGS", false)
            MiniTab("PROFILE", false)
        }
    }
}

@Composable
private fun ConfirmRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onToggle() }) {
        Icon(
            imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Filled.Person,
            contentDescription = null,
            tint = if (checked) PODBlack else PODBlack.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = PODBlack, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MiniTab(label: String, selected: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(22.dp).background(if (selected) PODBlue else PODSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = if (selected) PODWhite else PODBlue, modifier = Modifier.size(12.dp))
        }
        Spacer(Modifier.width(4.dp))
        Text(label, color = if (selected) PODBlue else PODBlack.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
