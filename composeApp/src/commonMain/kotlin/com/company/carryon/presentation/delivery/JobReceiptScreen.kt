package com.company.carryon.presentation.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.data.model.isSettlementEligible
import com.company.carryon.data.network.LocationApi
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator

private val ReceiptBlue = Color(0xFF5A86E8)
private val ReceiptBg = Color(0xFFF7F8FC)
private val ReceiptCard = Color(0xFFEFF4FF)
private val ReceiptMuted = Color(0xFF7A8499)
private val ReceiptDivider = Color(0xFFE0E8F5)
private val ReceiptText = Color(0xFF242A36)

@Composable
fun JobReceiptScreen(navigator: AppNavigator) {
    val viewModel = remember { DeliveryViewModel() }
    val jobState by viewModel.currentJob.collectAsState()
    val jobId = navigator.selectedJobId

    if (jobId == null) {
        ErrorState("No job selected") { navigator.goBack() }
        return
    }

    LaunchedEffect(jobId) {
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
    LaunchedEffect(pickupLat, pickupLng, dropLat, dropLng) {
        if (pickupLat != 0.0 && dropLat != 0.0) {
            LocationApi.calculateRoute(pickupLat, pickupLng, dropLat, dropLng)
                .onSuccess { result -> if (result.geometry.isNotEmpty()) routeGeometry = result.geometry }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReceiptBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ReceiptBlue)
            }
            Text(
                "Receipt",
                color = ReceiptBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("ORDER RECEIPT", color = ReceiptMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "#${job.id.takeLast(8).uppercase()}",
                        color = ReceiptBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    val dateStr = job.completedAt ?: job.deliveredAt
                    if (!dateStr.isNullOrBlank()) {
                        Text(dateStr, color = ReceiptMuted, fontSize = 12.sp)
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReceiptDetailRow("From", job.pickup.address.ifBlank { "—" })
                    HorizontalDivider(color = ReceiptDivider)
                    ReceiptDetailRow("To", job.dropoff.address.ifBlank { "—" })
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ReceiptCard),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("SUMMARY", color = ReceiptMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ReceiptDetailRow(
                        "Earnings",
                        if (job.isSettlementEligible) "RM ${job.estimatedEarnings.toInt()}" else "Pending handover",
                        valueColor = ReceiptBlue,
                        valueBold = true
                    )
                    HorizontalDivider(color = ReceiptDivider)
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

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReceiptDetailRow(
    label: String,
    value: String,
    valueColor: Color = ReceiptText,
    valueBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = ReceiptMuted, fontSize = 13.sp)
        Text(
            value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.End)
        )
    }
}
