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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Navigation
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.DeliveryLifecycleCommand
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import kotlin.math.abs
import kotlin.math.roundToInt

private val SDBlue = Color(0xFF2F80ED)
private val SDSoft = Color(0x4DA6D2F3)
private val SDWhite = Color(0xFFFFFFFF)
private val SDBlack = Color(0xFF000000)
private val DefaultCenter = LatLng(12.9716, 77.5946)

@Composable
fun StartDeliveryScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val jobState by viewModel.currentJob.collectAsState()
    val startDeliveryState by viewModel.startDeliveryState.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val mapMarkers by viewModel.markers.collectAsState()
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

    LaunchedEffect(job.status) {
        viewModel.redirectIfCurrentScreenInvalid(Screen.StartDelivery, job)
    }

    val pickup = job.pickup
    val dropoff = job.dropoff
    val centerLat = if (pickup.latitude != 0.0) pickup.latitude else DefaultCenter.lat
    val centerLng = if (pickup.longitude != 0.0) pickup.longitude else DefaultCenter.lng

    val distanceKm = job.distance.takeIf { it > 0 }?.let { formatOneDecimal(it) } ?: "--"
    val durationMin = job.displayDurationMinutes.takeIf { it > 0 }?.toString() ?: "--"
    val orderLabel = job.displayOrderId.takeIf { it.isNotBlank() } ?: job.id.takeLast(8).uppercase()

    Box(modifier = Modifier.fillMaxSize().background(SDWhite)) {
        // Map background showing pickup to dropoff route
        MapViewComposable(
            modifier = Modifier.fillMaxSize(),
            styleUrl = "",
            centerLat = centerLat,
            centerLng = centerLng,
            zoom = if (pickup.latitude != 0.0) 12.0 else 12.0,
            markers = buildList {
                if (pickup.latitude != 0.0) {
                    add(MapMarker("pickup", pickup.latitude, pickup.longitude, "Pickup", MarkerColor.GREEN))
                }
                if (dropoff.latitude != 0.0) {
                    add(MapMarker("dropoff", dropoff.latitude, dropoff.longitude, "Drop-off", MarkerColor.RED))
                }
            },
            routeGeometry = routeGeometry
        )

        // Soft overlay
        Box(modifier = Modifier.fillMaxSize().background(SDSoft))

        // Top bar card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SDWhite)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(36.dp).background(SDBlue, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, tint = SDWhite, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        dropoff.shortAddress.ifBlank { dropoff.address }.ifBlank { "--" },
                        color = SDBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
                Text("#$orderLabel", color = SDBlack.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }

        // Bottom card with distance, time, and Start Delivery button
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 64.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SDWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("DISTANCE", color = SDBlack.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(distanceKm, color = SDBlue, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("km", color = SDBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("EST. TIME", color = SDBlack.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = SDBlue, modifier = Modifier.size(14.dp))
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(durationMin, color = SDBlue, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("min", color = SDBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Button(
                    onClick = { viewModel.startDelivery(jobId) },
                    enabled = viewModel.canRun(DeliveryLifecycleCommand.START_DELIVERY, job) &&
                        startDeliveryState !is UiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SDBlue)
                ) {
                    if (startDeliveryState is UiState.Loading) {
                        CircularProgressIndicator(color = SDWhite, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Start Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = SDWhite, modifier = Modifier.size(16.dp))
                    }
                }

                val errorState = startDeliveryState as? UiState.Error
                if (errorState != null) {
                    Text(errorState.message, color = Color(0xFFCC3D3D), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
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
