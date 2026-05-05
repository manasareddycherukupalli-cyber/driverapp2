package com.company.carryon.presentation.delivery

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalUriHandler
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
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val ArriveBlue = Color(0xFF2F80ED)
private val ArriveSoft = Color(0x4DA6D2F3)
private val ArriveWhite = Color(0xFFFFFFFF)
private val ArriveBlack = Color(0xFF000000)

@Composable
fun ActiveDeliveryScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val jobState by viewModel.currentJob.collectAsState()
    val cancelState by viewModel.cancelState.collectAsState()
    val jobId = navigator.selectedJobId
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJob(it) }
    }

    LaunchedEffect(viewModel) {
        viewModel.cancelCompletedEvents.collect {
            navigator.clearPersistedDeliveryState()
            navigator.navigateAndClearStack(Screen.Home)
        }
    }

    LaunchedEffect(cancelState) {
        val state = cancelState as? UiState.Error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(state.message)
    }

    LaunchedEffect(jobState) {
        val job = (jobState as? UiState.Success)?.data ?: return@LaunchedEffect
        viewModel.redirectIfCurrentScreenInvalid(Screen.ActiveDelivery, job)
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
                is UiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { jobId?.let { viewModel.loadJob(it) } }
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveDeliveryContent(
    job: DeliveryJob,
    viewModel: DeliveryViewModel,
    navigator: AppNavigator,
    routeGeometry: List<LatLng>?,
    mapMarkers: List<MapMarker>
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showCancelMenu by remember { mutableStateOf(false) }
    val cancelState by viewModel.cancelState.collectAsState()
    val canCancelJob = viewModel.canCancelBeforePickup(job)
    val isCancelling = cancelState is UiState.Loading
    var activeSheet by remember { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(canCancelJob) {
        if (!canCancelJob) {
            showCancelMenu = false
            showCancelDialog = false
        }
    }

    if (showCancelDialog && canCancelJob) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Job?") },
            text = { Text("The job will be re-assigned to another driver.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelJob(job.id)
                    }
                ) { Text("Cancel Job", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep Job") }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArriveWhite)
    ) {
        MapViewComposable(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            styleUrl = "",
            markers = mapMarkers,
            routeGeometry = routeGeometry
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ArriveBlue, modifier = Modifier.clickable { navigator.goBack() })
                Spacer(Modifier.width(10.dp))
                Text("Arrived at Pickup", color = ArriveBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            if (canCancelJob) {
                Box {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = ArriveBlack.copy(alpha = 0.6f),
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StepLabel("ACCEPTED", false)
                StepLabel("ARRIVED", true)
                StepLabel("IN-TRANSIT", false)
                StepLabel("COMPLETED", false)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.weight(1f).height(4.dp).background(ArriveBlue, RoundedCornerShape(99.dp)))
                Box(modifier = Modifier.weight(1f).height(4.dp).background(ArriveBlue.copy(alpha = 0.5f), RoundedCornerShape(99.dp)))
                Box(modifier = Modifier.weight(1f).height(4.dp).background(ArriveSoft, RoundedCornerShape(99.dp)))
                Box(modifier = Modifier.weight(1f).height(4.dp).background(ArriveSoft, RoundedCornerShape(99.dp)))
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(ArriveSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(ArriveBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = ArriveWhite)
                    }
                }
            }

            Text("You've Arrived!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ArriveBlack)
            Text(
                "At ${job.pickup.shortAddress.ifBlank { job.pickup.address }}",
                color = ArriveBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArriveSoft)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("ORDER ID", fontSize = 10.sp, color = ArriveBlack.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                            Text("#${job.id.takeLast(8).uppercase()}", fontSize = 22.sp, color = ArriveBlack, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("TYPE", fontSize = 10.sp, color = ArriveBlack.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                            Text("${job.packageType} (${job.packageSize.displayName})", fontSize = 18.sp, color = ArriveBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("SENDER", fontSize = 10.sp, color = ArriveBlack.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                            Text(job.pickup.contactName ?: job.customerName.ifBlank { "--" }, fontSize = 16.sp, color = ArriveBlack, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .background(ArriveWhite, RoundedCornerShape(999.dp))
                                .clickable {
                                    navigator.openCustomerChat(job.id, job.customerName.ifBlank { "Customer" })
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null, tint = ArriveBlue, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Chat", color = ArriveBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { navigator.navigateTo(Screen.PickupInstructions) },
                enabled = !isCancelling,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArriveBlue)
            ) {
                Text("Package Picked Up", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ArriveWhite, modifier = Modifier.size(16.dp))
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

            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ArriveWhite, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeliveryBottomTab("ROUTE", true) { activeSheet = "ROUTE" }
            DeliveryBottomTab("EARNINGS", false) { activeSheet = "EARNINGS" }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(ArriveBlue, CircleShape)
                    .clickable {
                        uriHandler.openUri(
                            "https://www.google.com/maps/dir/?api=1&destination=${job.dropoff.latitude},${job.dropoff.longitude}&travelmode=driving"
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Navigation, contentDescription = "Navigate", tint = ArriveWhite)
            }
            DeliveryBottomTab("INBOX", false) {
                navigator.openCustomerChat(job.id, job.customerName.ifBlank { "Customer" })
            }
            DeliveryBottomTab("ACCOUNT", false) { navigator.switchTab(Screen.Profile) }
        }
    }

    if (activeSheet == "ROUTE") {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            DeliveryRouteSheet(
                pickupAddress = job.pickup.shortAddress.ifBlank { job.pickup.address },
                dropoffAddress = job.dropoff.shortAddress.ifBlank { job.dropoff.address },
                blue = ArriveBlue, black = ArriveBlack
            )
        }
    }
    if (activeSheet == "EARNINGS") {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            DeliveryEarningsSheet(
                earnings = job.estimatedEarnings,
                orderId = job.id,
                blue = ArriveBlue, black = ArriveBlack
            )
        }
    }
}

@Composable
private fun StepLabel(text: String, active: Boolean) {
    Text(
        text,
        color = if (active) ArriveBlack else ArriveBlack.copy(alpha = 0.55f),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun DeliveryBottomTab(label: String, selected: Boolean, onClick: () -> Unit = {}) {
    Text(
        text = label,
        color = if (selected) ArriveBlue else ArriveBlack.copy(alpha = 0.55f),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun DeliveryRouteSheet(pickupAddress: String, dropoffAddress: String, blue: Color, black: Color) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Delivery Route", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = black)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(10.dp).background(blue, CircleShape))
            Column {
                Text("PICKUP", fontSize = 10.sp, color = black.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                Text(pickupAddress.ifBlank { "--" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = black)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(10.dp).background(Color(0xFFE53935), CircleShape))
            Column {
                Text("DROP-OFF", fontSize = 10.sp, color = black.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                Text(dropoffAddress.ifBlank { "--" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = black)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DeliveryEarningsSheet(earnings: Double, orderId: String, blue: Color, black: Color) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Job Earnings", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = black)
        Text(
            "RM ${earnings.toInt()}",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = blue
        )
        Text("Order #${orderId.takeLast(8).uppercase()}", color = black.copy(alpha = 0.5f), fontSize = 13.sp)
        Text(
            "Final amount confirmed after delivery completion.",
            fontSize = 12.sp,
            color = black.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
    }
}
