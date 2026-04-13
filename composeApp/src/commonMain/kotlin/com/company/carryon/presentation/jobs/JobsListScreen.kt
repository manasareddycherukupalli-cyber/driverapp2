package com.company.carryon.presentation.jobs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.UiState
import com.company.carryon.data.network.LocationApi
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val JobsBlue = Color(0xFF2F80ED)
private val JobsBg = Color(0xFFF4F5F8)
private val JobsText = Color(0xFF202124)
private val JobsMuted = Color(0xFF7A8499)
private val JobsDivider = Color(0x1A000000)
private val JobsCardBg = Color.White
private val JobsEarningsGreen = Color(0xFF27AE60)

@Composable
fun JobsListScreen(navigator: AppNavigator) {
    val viewModel = remember { JobsViewModel() }
    val completedJobs by viewModel.completedJobs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JobsBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color(0xFF6F7480), modifier = Modifier.size(26.dp))
            Text("Carry On", color = JobsBlue, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = Color(0xFF6F7480), modifier = Modifier.size(24.dp))
        }

        Text(
            "Past Jobs",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = JobsText,
            modifier = Modifier.padding(start = 24.dp, bottom = 14.dp)
        )

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 27.dp, topEnd = 27.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            when (completedJobs) {
                is UiState.Loading, UiState.Idle -> LoadingScreen()
                is UiState.Error -> ErrorState(
                    (completedJobs as UiState.Error).message,
                    onRetry = { viewModel.loadCompletedJobs() }
                )
                is UiState.Success -> {
                    val jobs = (completedJobs as UiState.Success<List<DeliveryJob>>).data
                    if (jobs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No past jobs yet", color = JobsMuted, fontSize = 15.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item { Spacer(Modifier.height(8.dp)) }
                            items(jobs) { job ->
                                PastJobCard(
                                    job = job,
                                    onClick = {
                                        navigator.selectedJobId = job.id
                                        navigator.navigateTo(Screen.JobReceipt)
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PastJobCard(job: DeliveryJob, onClick: () -> Unit) {
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

    var routeGeometry by remember { mutableStateOf<List<LatLng>?>(null) }
    LaunchedEffect(pickupLat, pickupLng, dropLat, dropLng) {
        if (pickupLat != 0.0 && dropLat != 0.0) {
            LocationApi.calculateRoute(pickupLat, pickupLng, dropLat, dropLng)
                .onSuccess { result -> if (result.geometry.isNotEmpty()) routeGeometry = result.geometry }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JobsCardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Map showing pickup → dropoff route
            if (hasCoords) {
                MapViewComposable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    centerLat = centerLat,
                    centerLng = centerLng,
                    zoom = 11.0,
                    markers = markers,
                    routeGeometry = routeGeometry
                )
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Order ID + earnings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            job.displayOrderId.let { if (it.isNotBlank()) if (it.startsWith("#")) it else "#$it" else "#${job.id.takeLast(8).uppercase()}" },
                            color = JobsBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (!job.completedAt.isNullOrBlank() || !job.deliveredAt.isNullOrBlank()) {
                            val date = job.completedAt ?: job.deliveredAt ?: ""
                            Text(date, color = JobsMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Text(
                        "RM ${job.estimatedEarnings.toInt()}",
                        color = JobsEarningsGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }

                HorizontalDivider(color = JobsDivider)

                // Route: pickup → dropoff
                RouteAddressRow(
                    isPickup = true,
                    address = job.pickup.address.ifBlank { "—" }
                )
                RouteAddressRow(
                    isPickup = false,
                    address = job.dropoff.address.ifBlank { "—" }
                )

                HorizontalDivider(color = JobsDivider)

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    StatChip("Distance", "${job.distance.toInt()} km")
                    StatChip("Duration", "${job.estimatedDuration} min")
                    if (job.packageType.isNotBlank()) StatChip("Package", job.packageType)
                }
            }
        }
    }
}

@Composable
private fun RouteAddressRow(isPickup: Boolean, address: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.Place,
            contentDescription = null,
            tint = if (isPickup) Color(0xFF27AE60) else Color(0xFFEB5757),
            modifier = Modifier.size(16.dp).padding(top = 1.dp)
        )
        Column {
            Text(
                if (isPickup) "Pickup" else "Dropoff",
                color = JobsMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                address,
                color = JobsText,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, color = JobsMuted, fontSize = 10.sp)
        Text(value, color = JobsText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
