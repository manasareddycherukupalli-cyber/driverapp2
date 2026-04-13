package com.company.carryon.presentation.delivery

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import kotlin.math.abs
import kotlin.math.roundToInt

private val SDBlue = Color(0xFF2F80ED)
private val SDSoft = Color(0x4DA6D2F3)
private val SDWhite = Color(0xFFFFFFFF)
private val SDBlack = Color(0xFF000000)

@Composable
fun StartDeliveryScreen(navigator: AppNavigator) {
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

    val job = when (val state = jobState) {
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

    StartDeliveryContent(
        job = job,
        onStartNavigation = { navigator.navigateTo(Screen.InTransitNavigation) }
    )
}

@Composable
private fun StartDeliveryContent(
    job: DeliveryJob,
    onStartNavigation: () -> Unit
) {
    val strings = LocalStrings.current
    val pickupLabel = job.pickup.shortAddress.ifBlank { job.pickup.address }.ifBlank { "--" }
    val destinationLabel = job.dropoff.shortAddress.ifBlank { job.dropoff.address }.ifBlank { "--" }
    val receiverLabel = job.dropoff.contactName
        ?: job.customerName.takeIf { it.isNotBlank() }
        ?: "--"
    val etaMajor = job.displayDurationMinutes.takeIf { it > 0 }?.toString() ?: "--"
    val distanceMajor = job.distance.takeIf { it > 0 }?.let { formatOneDecimal(it) } ?: "--"
    val orderLabel = job.displayOrderId.takeIf { it.isNotBlank() } ?: job.id.takeLast(8).uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SDWhite)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).background(SDSoft, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = SDBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("#$orderLabel", color = SDBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = SDBlack.copy(alpha = 0.6f))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepDot("1", false)
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(SDBlack.copy(alpha = 0.25f)))
            StepDot("2", false)
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(SDBlack.copy(alpha = 0.25f)))
            StepDot("3", true)
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(SDBlack.copy(alpha = 0.25f)))
            StepDot("4", false)
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(SDSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = SDBlue, modifier = Modifier.size(38.dp))
            }
        }

        Text(strings.readyToDeliver, color = SDBlack, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Package collected from ", color = SDBlack.copy(alpha = 0.65f), fontSize = 14.sp)
            Text(pickupLabel, color = SDBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SDSoft)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.destination, color = SDBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text(destinationLabel, color = SDBlack, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = SDBlack.copy(alpha = 0.65f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(receiverLabel, color = SDBlack, fontSize = 14.sp)
                        }
                    }
                    Box(modifier = Modifier.size(30.dp).background(SDWhite, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PinDrop, contentDescription = null, tint = SDBlue, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(strings.eta, etaMajor, "min", Modifier.weight(1f))
            MetricCard(strings.distanceLabel, distanceMajor, "km", Modifier.weight(1f))
        }

        Button(
            onClick = onStartNavigation,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SDBlue)
        ) {
            Text(strings.startNavigation, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Map, contentDescription = null, tint = SDWhite, modifier = Modifier.size(16.dp))
        }

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SDWhite, contentColor = SDBlue)
        ) {
            Text(strings.reportAnIssue, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallTab("ROUTE", false)
            SmallTab("EARNINGS", false)
            SmallTab("ORDERS", true)
            SmallTab("WALLET", false)
            SmallTab("ACCOUNT", false)
        }
    }
}

private fun formatOneDecimal(value: Double): String {
    val scaled = (value * 10).roundToInt()
    val absScaled = abs(scaled)
    val whole = absScaled / 10
    val fraction = absScaled % 10
    val sign = if (scaled < 0) "-" else ""
    return "$sign$whole.$fraction"
}

@Composable
private fun StepDot(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .size(if (active) 38.dp else 28.dp)
            .background(SDBlue, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = SDWhite, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricCard(title: String, major: String, minor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SDWhite)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = SDBlack.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(major, color = SDBlue, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Spacer(Modifier.width(4.dp))
                Text(minor, color = SDBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SmallTab(label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (selected) SDSoft else SDWhite, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = if (selected) SDBlue else SDBlack.copy(alpha = 0.45f), modifier = Modifier.size(16.dp))
        }
        Text(label, color = if (selected) SDBlue else SDBlack.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
