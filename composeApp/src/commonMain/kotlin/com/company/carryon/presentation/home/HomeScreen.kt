package com.company.carryon.presentation.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.*
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.jobs.JobRequestPopup
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*
import com.company.carryon.data.model.LatLng

/**
 * HomeScreen — Main dashboard for the driver.
 * Shows online toggle, earnings, active jobs, quick actions, and job request popup.
 */
@Composable
fun HomeScreen(navigator: AppNavigator, viewModel: HomeViewModel) {
    val isOnline by viewModel.isOnline.collectAsState()
    val earningsState by viewModel.earningsSummary.collectAsState()
    val activeJobsState by viewModel.activeJobs.collectAsState()
    val incomingJob by viewModel.incomingJob.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()
    val driver by viewModel.currentDriver.collectAsState()
    val driverLocation by viewModel.driverLocation.collectAsState()
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsState()

    // Refresh location when screen appears
    LaunchedEffect(Unit) {
        viewModel.refreshDriverLocation()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // ---- Header with gradient ----
            HomeHeader(
                driverName = driver?.name,
                isOnline = isOnline,
                onToggleOnline = { viewModel.toggleOnlineStatus() },
                unreadCount = unreadCount,
                onNotificationsClick = { navigator.navigateTo(Screen.Notifications) }
            )

            // ---- Driver Location Map ----
            DriverLocationMap(
                driverLocation = driverLocation,
                mapStyleUrl = mapStyleUrl
            )

            Spacer(Modifier.height(16.dp))

            // ---- Today's Earnings Card ----
            TodayEarningsCard(
                earningsState = earningsState,
                onClick = { navigator.switchTab(Screen.Earnings) }
            )

            Spacer(Modifier.height(16.dp))

            // ---- Active Job Card ----
            ActiveJobSection(
                activeJobsState = activeJobsState,
                onJobClick = { jobId ->
                    navigator.selectedJobId = jobId
                    navigator.navigateTo(Screen.JobDetails)
                }
            )

            Spacer(Modifier.height(16.dp))

            // ---- Quick Actions Grid ----
            SectionHeader(title = "Quick Actions")
            QuickActionsGrid(navigator)

            Spacer(Modifier.height(24.dp))
        }

        // ---- Incoming Job Request Popup ----
        if (incomingJob != null) {
            JobRequestPopup(
                job = incomingJob!!,
                onAccept = { viewModel.acceptIncomingJob() },
                onReject = { viewModel.rejectIncomingJob() },
                onDismiss = { viewModel.dismissIncomingJob() }
            )
        }
    }
}

// ============================================================
// HOME HEADER
// ============================================================

@Composable
private fun HomeHeader(
    driverName: String?,
    isOnline: Boolean,
    onToggleOnline: () -> Unit,
    unreadCount: Int,
    onNotificationsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Orange500, Orange600)
                )
            )
            .padding(20.dp)
            .padding(top = 12.dp)
    ) {
        Column {
            // Top row: Avatar + Notification bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Generate initials from driver name, or show "?" if not loaded
                    val initials = driverName
                        ?.split(" ")
                        ?.filter { it.isNotBlank() }
                        ?.take(2)
                        ?.joinToString("") { it.first().uppercase() }
                        ?: "?"
                    AvatarCircle(
                        initials = initials,
                        size = 44.dp,
                        backgroundColor = Color.White.copy(alpha = 0.2f),
                        textColor = Color.White
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        val greeting = if (!driverName.isNullOrBlank()) {
                            "Hello, ${driverName.split(" ").first()}!"
                        } else {
                            "Hello!"
                        }
                        Text(
                            text = greeting,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isOnline) "You're online" else "You're offline",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                // Notification bell with badge
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = Red500) {
                                Text("$unreadCount", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Online/Offline Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isOnline) "🟢 Online" else "🔴 Offline",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isOnline) "Receiving job requests" else "Tap to go online",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { onToggleOnline() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Green500,
                            checkedTrackColor = Green500.copy(alpha = 0.3f),
                            uncheckedThumbColor = Gray400,
                            uncheckedTrackColor = Gray400.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}

// ============================================================
// DRIVER LOCATION MAP
// ============================================================

@Composable
private fun DriverLocationMap(
    driverLocation: Pair<Double, Double>?,
    mapStyleUrl: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (driverLocation != null) {
            val lat = driverLocation.first
            val lng = driverLocation.second

            val markers = listOf(
                MapMarker(
                    id = "driver",
                    lat = lat,
                    lng = lng,
                    title = "You are here",
                    color = MarkerColor.BLUE
                )
            )

            Column {
                MapViewComposable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    styleUrl = mapStyleUrl,
                    centerLat = lat,
                    centerLng = lng,
                    zoom = 14.0,
                    markers = markers
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Orange500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Your current location",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Location not available — prompt user
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.LocationOff,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Location unavailable",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Please enable location permissions to see your position on the map.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ============================================================
// TODAY EARNINGS CARD
// ============================================================

@Composable
private fun TodayEarningsCard(
    earningsState: UiState<EarningsSummary>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        when (earningsState) {
            is UiState.Success -> {
                val earnings = earningsState.data
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Earnings",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "View all",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "RM${earnings.todayEarnings.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Orange500
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        MiniStat("Deliveries", "${earnings.todayDeliveries}")
                        MiniStat("Online Hrs", "${earnings.onlineHours}h")
                        MiniStat("Tips", "RM${earnings.tipEarnings.toInt()}")
                    }
                }
            }
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange500, modifier = Modifier.size(32.dp))
                }
            }
            is UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = earningsState.message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tap to retry",
                        color = Orange500,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("Tap to load earnings", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================
// ACTIVE JOB SECTION
// ============================================================

@Composable
private fun ActiveJobSection(
    activeJobsState: UiState<List<DeliveryJob>>,
    onJobClick: (String) -> Unit
) {
    when (activeJobsState) {
        is UiState.Success -> {
            val jobs = activeJobsState.data
            if (jobs.isNotEmpty()) {
                SectionHeader(title = "Active Jobs", action = "View All")
                jobs.take(2).forEach { job ->
                    ActiveJobCard(job = job, onClick = { onJobClick(job.id) })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        is UiState.Error -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activeJobsState.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp
                    )
                }
            }
        }
        else -> {} // Loading or Idle
    }
}

@Composable
private fun ActiveJobCard(job: DeliveryJob, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status badge + earnings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = job.status.displayName,
                    color = Orange600,
                    backgroundColor = Orange100
                )
                Text(
                    text = "RM${job.estimatedEarnings.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Orange500
                )
            }

            Spacer(Modifier.height(12.dp))

            // Pickup
            Row(verticalAlignment = Alignment.Top) {
                Text("📍", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Pickup", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = job.pickup.shortAddress.ifBlank { job.pickup.address },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Dropoff
            Row(verticalAlignment = Alignment.Top) {
                Text("🏁", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Drop-off", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = job.dropoff.shortAddress.ifBlank { job.dropoff.address },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Distance and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📦 ${job.packageType}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "📏 ${job.distance} km",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "⏱ ${job.estimatedDuration} min",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================
// QUICK ACTIONS GRID
// ============================================================

data class QuickAction(val icon: ImageVector, val label: String, val screen: Screen)

@Composable
private fun QuickActionsGrid(navigator: AppNavigator) {
    val actions = listOf(
        QuickAction(Icons.Filled.AccountBalanceWallet, "My Earnings", Screen.Earnings),
        QuickAction(Icons.Filled.LocalShipping, "My Jobs", Screen.Jobs),
        QuickAction(Icons.Filled.Star, "Ratings", Screen.Ratings),
        QuickAction(Icons.Filled.Wallet, "Wallet", Screen.Wallet),
        QuickAction(Icons.Filled.HelpCenter, "Help Center", Screen.HelpCenter),
        QuickAction(Icons.Filled.Settings, "Settings", Screen.Settings),
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // 2 rows of 3
        for (rowIndex in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (colIndex in 0..2) {
                    val index = rowIndex * 3 + colIndex
                    if (index < actions.size) {
                        QuickActionItem(
                            action = actions[index],
                            onClick = { navigator.navigateTo(actions[index].screen) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            if (rowIndex == 0) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickActionItem(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Orange100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = Orange500,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = action.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
