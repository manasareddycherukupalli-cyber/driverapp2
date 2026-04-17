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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
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

@Composable
fun PickupInstructionsScreen(navigator: AppNavigator) {
    val viewModel = remember { DeliveryViewModel() }
    val jobState by viewModel.currentJob.collectAsState()
    val jobId = navigator.selectedJobId

    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJob(it) }
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
            onConfirm = {
                viewModel.updateStatus(state.data.id, JobStatus.PICKED_UP)
                navigator.navigateTo(Screen.StartDelivery)
            }
        )
    }
}

@Composable
private fun PickupInstructionsContent(
    job: DeliveryJob,
    navigator: AppNavigator,
    onConfirm: () -> Unit
) {
    val checks = remember(job.id) { mutableStateListOf(false, false, false, false) }
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

        Button(
            onClick = onConfirm,
            enabled = allChecksComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PickBlue)
        ) {
            Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = PickWhite, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Confirm & Start Delivery", fontWeight = FontWeight.Bold)
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
