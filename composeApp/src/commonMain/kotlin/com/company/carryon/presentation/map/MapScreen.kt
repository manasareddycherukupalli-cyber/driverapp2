package com.company.carryon.presentation.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.delivery.DeliveryViewModel
import com.company.carryon.presentation.navigation.Screen
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.ic_nav_arrow
import com.company.carryon.data.model.DeliveryLifecycleCommand
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.LatLng
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.util.contactPhoneForCurrentDestination
import com.company.carryon.presentation.util.telUriFor
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.math.roundToInt

private val NavBlue = Color(0xFF034094)
private val NavSoft = Color(0x4DA6D2F3)
private val NavWhite = Color(0xFFFFFFFF)
private val NavBlack = Color(0xFF000000)
private val DefaultCenter = LatLng(12.9716, 77.5946)

@Composable
fun MapScreen(navigator: AppNavigator, deliveryViewModel: DeliveryViewModel) {
    val jobId = navigator.selectedJobId
    val viewModel = remember { MapViewModel() }
    val cancelState by deliveryViewModel.cancelState.collectAsState()
    val isCancelling = cancelState is UiState.Loading
    var showCancelMenu by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val driverLocation by viewModel.driverLocation.collectAsState()
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val eta by viewModel.etaMinutes.collectAsState()
    val currentJob by viewModel.currentJob.collectAsState()
    val loadError by viewModel.error.collectAsState()
    val driverHeading by viewModel.driverHeading.collectAsState()
    val markers by viewModel.markers.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val routeError by viewModel.routeError.collectAsState()
    val statusUpdateState by deliveryViewModel.statusUpdateState.collectAsState()
    val animatedHeading by animateFloatAsState(
        targetValue = driverHeading,
        animationSpec = tween(durationMillis = 300)
    )
    val uriHandler = LocalUriHandler.current

    if (jobId == null) {
        ErrorState("No active job selected") { navigator.goBack() }
        return
    }

    LaunchedEffect(deliveryViewModel) {
        deliveryViewModel.cancelCompletedEvents.collect {
            navigator.clearPersistedDeliveryState()
            navigator.navigateAndClearStack(Screen.Home)
            deliveryViewModel.resetCancelState()
        }
    }

    LaunchedEffect(cancelState) {
        if (cancelState is UiState.Success) {
            navigator.clearPersistedDeliveryState()
            navigator.navigateAndClearStack(Screen.Home)
            deliveryViewModel.resetCancelState()
        }
    }

    LaunchedEffect(jobId) {
        viewModel.loadJob(jobId)
        deliveryViewModel.loadJob(jobId)
    }

    if (currentJob == null && loadError == null) {
        LoadingScreen("Loading job details...")
        return
    }

    if (currentJob == null && loadError != null) {
        ErrorState(loadError ?: "Failed to load job") {
            viewModel.loadJob(jobId)
        }
        return
    }

    LaunchedEffect(currentJob?.status) {
        currentJob?.let { job ->
            deliveryViewModel.redirectIfCurrentScreenInvalid(Screen.MapNavigation, job)
        }
    }

    val canCancelJob = currentJob?.let { job -> deliveryViewModel.canCancelBeforePickup(job) } == true
    LaunchedEffect(canCancelJob) {
        if (!canCancelJob) {
            showCancelMenu = false
            showCancelDialog = false
        }
    }
    val destination = currentJob?.let { job ->
        if (job.status.ordinal <= JobStatus.ARRIVED_AT_PICKUP.ordinal) job.pickup else job.dropoff
    }
    val destinationLabel = destination?.shortAddress?.ifBlank { destination.address } ?: "--"
    val orderIdLabel = currentJob?.displayOrderId
        ?.takeIf { it.isNotBlank() }
        ?: currentJob?.id?.takeLast(8)?.uppercase()
        ?: "--"
    val earningsLabel = currentJob?.estimatedEarnings?.let { "RM${it.toInt()}" } ?: "--"
    val distanceLabel = currentJob?.distance?.takeIf { it > 0 }?.let { "${formatOneDecimal(it)} km" } ?: "--"
    val destinationTelUri = telUriFor(currentJob?.contactPhoneForCurrentDestination())
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavWhite)
    ) {
        MapViewComposable(
            modifier = Modifier.fillMaxSize(),
            styleUrl = mapStyleUrl,
            centerLat = driverLocation?.first ?: DefaultCenter.lat,
            centerLng = driverLocation?.second ?: DefaultCenter.lng,
            zoom = if (driverLocation != null) 14.2 else 12.0,
            markers = markers.filterNot { it.id == "driver" },
            routeGeometry = routeGeometry
        )

        if (driverLocation == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NavBlue)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Getting your location...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(NavSoft.copy(alpha = 0.1f)))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .background(NavWhite, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavBlue, modifier = Modifier.clickable { navigator.goBack() })
                Spacer(Modifier.width(10.dp))
                Text("Delivery Instruction", color = NavBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = NavBlue)
                if (canCancelJob) {
                    Box {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            tint = NavBlue,
                            modifier = Modifier.clickable { showCancelMenu = true }
                        )
                        DropdownMenu(
                            expanded = showCancelMenu,
                            onDismissRequest = { showCancelMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cancel Job", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showCancelMenu = false
                                    showCancelDialog = true
                                }
                            )
                        }
                    }
                }
            }

            if (showCancelDialog && canCancelJob) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = { Text("Cancel Job?") },
                    text = { Text("The job will be returned to the queue for another driver.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showCancelDialog = false
                            jobId?.let { deliveryViewModel.cancelJob(it) }
                        }) {
                            Text("Cancel Job", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text("Keep Job")
                        }
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 72.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = NavBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(48.dp).background(NavWhite.copy(alpha = 0.22f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, tint = NavWhite)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("NEXT INSTRUCTION", color = NavWhite.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(destinationLabel, color = NavWhite, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val job = currentJob ?: return@OutlinedButton
                                    val destination = if (job.status.ordinal <= JobStatus.ARRIVED_AT_PICKUP.ordinal) job.pickup else job.dropoff
                                    if (destination.latitude != 0.0 || destination.longitude != 0.0) {
                                        uriHandler.openUri(
                                            "https://www.google.com/maps/dir/?api=1&destination=${destination.latitude},${destination.longitude}&travelmode=driving"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavWhite)
                            ) {
                                Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Directions",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    destinationTelUri?.let(uriHandler::openUri)
                                },
                                enabled = destinationTelUri != null,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavWhite)
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Call",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (eta > 0) eta.toString() else "--", color = NavWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("MIN", color = NavWhite.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(if (isTracking) "TRACKING" else "PAUSED", color = NavWhite.copy(alpha = 0.85f), fontSize = 12.sp)
                    if (routeError != null) {
                        Text("Route unavailable", color = NavWhite.copy(alpha = 0.85f), fontSize = 11.sp)
                    } else {
                        Text(distanceLabel, color = NavWhite.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
            }
        }

        Image(
            painter = painterResource(Res.drawable.ic_nav_arrow),
            contentDescription = "Your location",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 80.dp)
                .size(48.dp)
                .graphicsLayer { rotationZ = animatedHeading }
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp, top = 210.dp)
                .size(40.dp)
                .background(NavWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MyLocation,
                contentDescription = null,
                tint = NavBlue,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { viewModel.refreshLocation() }
            )
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NavWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ChipText("ORDER ID")
                    ChipText("PICKUP TASK")
                }
                Text("#$orderIdLabel", color = NavBlack, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Route, contentDescription = null, tint = NavBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(destinationLabel, color = NavBlue, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        currentJob?.id?.let { id ->
                            navigator.selectedJobId = id
                            deliveryViewModel.updateStatus(id, JobStatus.ARRIVED_AT_PICKUP)
                        }
                    },
                    enabled = currentJob?.let { deliveryViewModel.canRun(DeliveryLifecycleCommand.ARRIVE_PICKUP, it) } == true &&
                        statusUpdateState !is UiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (statusUpdateState is UiState.Loading) {
                        CircularProgressIndicator(color = NavWhite, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Text("I’ve Arrived!  ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Icon(Icons.Filled.Navigation, contentDescription = null, tint = NavWhite, modifier = Modifier.size(16.dp))
                    }
                }

                if (canCancelJob) {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        enabled = !isCancelling,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.error,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("Cancel pickup", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                val arrivalError = (statusUpdateState as? UiState.Error)?.message
                if (!arrivalError.isNullOrBlank()) {
                    Text(arrivalError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavSoft)
                    ) {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = NavBlue, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("EST. PICKUP", color = NavBlack.copy(alpha = 0.55f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(if (eta > 0) "$eta min" else "--", color = NavBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavSoft)
                    ) {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MyLocation, contentDescription = null, tint = NavBlue, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("EARNINGS", color = NavBlack.copy(alpha = 0.55f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(earningsLabel, color = NavBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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

@Composable
private fun ChipText(text: String) {
    Box(
        modifier = Modifier
            .background(NavSoft, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = NavBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
