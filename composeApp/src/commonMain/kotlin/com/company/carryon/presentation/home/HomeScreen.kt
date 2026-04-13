package com.company.carryon.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import drive_app.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.EarningsSummary
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.data.model.remainingOfferMillis
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.theme.*
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.time.Clock

private val HomeBlue = Color(0xFF2F80ED)
private val HomeDarkBlue = Color(0xFF000000)
private val GlassBlue = Color(0x4DA6D2F3)
private val SoftBlue20 = Color(0x4DA6D2F3)
private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)
private val DefaultCenter = LatLng(12.9716, 77.5946)

private enum class HomeOnlineState {
    OFFLINE,
    JUST_ONLINE,
    FINDING_JOB,
    JOBS_QUEUE,
    ACTIVE_JOB
}

@Composable
fun HomeScreen(navigator: AppNavigator, viewModel: HomeViewModel) {
    val isOnline by viewModel.isOnline.collectAsState()
    val earningsState by viewModel.earningsSummary.collectAsState()
    val activeJobsState by viewModel.activeJobs.collectAsState()
    val incomingJobs by viewModel.incomingJobs.collectAsState()
    val expiringIncomingJobIds by viewModel.expiringIncomingJobIds.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()
    val driverLocation by viewModel.driverLocation.collectAsState()
    val currentLocationName by viewModel.currentLocationName.collectAsState()
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshDriverLocation()
        viewModel.loadDashboardData()
    }

    val activeJob = (activeJobsState as? UiState.Success<List<DeliveryJob>>)?.data?.firstOrNull()
    val scope = rememberCoroutineScope()
    var onlineState by remember { mutableStateOf(HomeOnlineState.OFFLINE) }
    var startedOnlineFlow by remember { mutableStateOf(false) }
    var showLiveQueue by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline, activeJob?.id, incomingJobs.size, startedOnlineFlow) {
        if (!startedOnlineFlow) {
            onlineState = HomeOnlineState.OFFLINE
            return@LaunchedEffect
        }
        if (!isOnline) {
            onlineState = HomeOnlineState.OFFLINE
            startedOnlineFlow = false
            return@LaunchedEffect
        }
        if (incomingJobs.isEmpty() && activeJob != null && onlineState == HomeOnlineState.JOBS_QUEUE) {
            onlineState = HomeOnlineState.ACTIVE_JOB
        }
    }

    if (incomingJobs.isNotEmpty()) {
        IncomingJobFullScreen(
            jobs = incomingJobs,
            expiringJobIds = expiringIncomingJobIds,
            onReject = { jobId -> viewModel.rejectIncomingJob(jobId) },
            onAccept = { jobId ->
                viewModel.acceptIncomingJob(jobId) { acceptedJob ->
                    navigator.selectedJobId = acceptedJob.id
                    navigator.navigateTo(Screen.JobDetails)
                }
            }
        )
        return
    }

    if (onlineState == HomeOnlineState.JOBS_QUEUE) {
        JobsQueueScreen(
            unreadCount = unreadCount,
            onMenuClick = { navigator.switchTab(Screen.Profile) },
            onNotificationsClick = { navigator.navigateTo(Screen.Notifications) },
            onJobClick = {
                activeJob?.id?.let {
                    navigator.selectedJobId = it
                    navigator.navigateTo(Screen.JobDetails)
                }
            }
        )
        return
    }

    if (showLiveQueue) {
        JobsQueueScreen(
            unreadCount = unreadCount,
            onMenuClick = { navigator.switchTab(Screen.Profile) },
            onNotificationsClick = { navigator.navigateTo(Screen.Notifications) },
            onJobClick = {
                activeJob?.id?.let {
                    navigator.selectedJobId = it
                    navigator.navigateTo(Screen.JobDetails)
                }
            }
        )
        return
    }

    FinalHomeDashboard(
        isOnline = isOnline,
        earningsState = earningsState,
        activeJobsState = activeJobsState,
        driverLocation = driverLocation,
        currentLocationName = currentLocationName,
        mapStyleUrl = mapStyleUrl,
        onMenuClick = { navigator.switchTab(Screen.Profile) },
        onProfileClick = { navigator.switchTab(Screen.Profile) },
        onToggleOnline = {
            if (startedOnlineFlow) return@FinalHomeDashboard
            startedOnlineFlow = true
            scope.launch {
                viewModel.toggleOnlineStatus()
                delay(500)
                startedOnlineFlow = false
            }
        },
        onViewAll = { navigator.switchToJobsTab(2) }
    )
}

@Composable
private fun FinalHomeDashboard(
    isOnline: Boolean,
    earningsState: UiState<EarningsSummary>,
    activeJobsState: UiState<List<DeliveryJob>>,
    driverLocation: Pair<Double, Double>?,
    currentLocationName: String?,
    mapStyleUrl: String,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onToggleOnline: () -> Unit,
    onViewAll: () -> Unit
) {
    val strings = LocalStrings.current
    val todayEarnings = (earningsState as? UiState.Success)?.data?.todayEarnings
    val deliveries = (earningsState as? UiState.Success)?.data?.todayDeliveries
    val jobs = (activeJobsState as? UiState.Success)?.data.orEmpty().take(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Black.copy(alpha = 0.55f), modifier = Modifier.clickable { onMenuClick() })
            Text(strings.dispatch, color = HomeBlue, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(SoftBlue20, RoundedCornerShape(16.dp))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = "Profile", tint = HomeBlue, modifier = Modifier.size(18.dp))
            }
        }

        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = White), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(HomeBlue, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(strings.statusOnline, color = Black, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
                Switch(
                    checked = isOnline,
                    onCheckedChange = { onToggleOnline() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = HomeBlue,
                        uncheckedThumbColor = White,
                        uncheckedTrackColor = SoftBlue20
                    )
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = HomeBlue)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(strings.todaysEarnings, color = White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (todayEarnings != null) "RM ${todayEarnings.toInt()}" else "--",
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(strings.deliveriesHeader, color = HomeBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(deliveries?.toString() ?: "--", color = HomeBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = White), modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                // Live Google Maps with driver's real GPS position
                MapViewComposable(
                    modifier = Modifier.fillMaxSize(),
                    centerLat = driverLocation?.first ?: 3.1478,
                    centerLng = driverLocation?.second ?: 101.7147,
                    zoom = 14.0,
                    showDriverLocation = true
                )
                // Current location label (top-left) — resolved via reverse geocoding
                if (currentLocationName != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(White, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Place, contentDescription = null, tint = HomeBlue, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(currentLocationName, color = Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                // Crosshair / locate button (bottom-right)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .background(White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MyLocation,
                        contentDescription = "My Location",
                        tint = Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(strings.todaysSummary, color = Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(strings.viewAll, color = HomeBlue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onViewAll() })
        }

        if (jobs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Text(
                    text = strings.noActiveJobs,
                    color = Black.copy(alpha = 0.65f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            jobs.forEach { job ->
                val status = if (job.status == JobStatus.DELIVERED) "COMPLETED" else "PENDING"
                SummaryItem(
                    id = job.id.takeLast(6),
                    subtitle = if (status == "COMPLETED") "Delivered to ${job.dropoff.shortAddress}" else "Pickup: ${job.pickup.shortAddress}",
                    amount = "RM ${formatTwoDecimals(job.estimatedEarnings)}",
                    status = status
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun formatTwoDecimals(value: Double): String {
    val cents = round(value * 100.0).toLong()
    val absCents = abs(cents)
    val whole = absCents / 100
    val fraction = (absCents % 100).toString().padStart(2, '0')
    val sign = if (cents < 0) "-" else ""
    return "$sign$whole.$fraction"
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = HomeBlue, modifier = Modifier.size(20.dp))
        }
        Text(label, color = Black.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickActionImage(label: String, drawableRes: DrawableResource) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(drawableRes),
                contentDescription = label,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(label, color = Black.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SummaryItem(id: String, subtitle: String, amount: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(36.dp).background(SoftBlue20, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = HomeBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Parcel $id", color = HomeBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(subtitle, color = Black.copy(alpha = 0.65f), fontSize = 12.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amount, color = HomeBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(status, color = Black, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    unreadCount: Int,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(Icons.Filled.Menu, contentDescription = "Menu", modifier = Modifier.clickable { onMenuClick() })
        Text("Carry On", color = HomeBlue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(containerColor = Color.Red) {
                        Text(unreadCount.toString())
                    }
                }
            }
        ) {
            Icon(
                Icons.Filled.NotificationsNone,
                contentDescription = "Notifications",
                modifier = Modifier.clickable { onNotificationsClick() }
            )
        }
    }
}

@Composable
private fun BlueMap(driverLocation: Pair<Double, Double>?, mapStyleUrl: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        MapViewComposable(
            modifier = Modifier.fillMaxSize(),
            styleUrl = mapStyleUrl,
            centerLat = driverLocation?.first ?: DefaultCenter.lat,
            centerLng = driverLocation?.second ?: DefaultCenter.lng,
            zoom = if (driverLocation != null) 15.0 else 12.0,
        )

        // Arrow marker centred on driver's current location
        androidx.compose.foundation.Image(
            painter = painterResource(Res.drawable.ic_nav_arrow),
            contentDescription = "Your location",
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(140.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, HomeDarkBlue)
                    )
                )
        )
    }
}

@Composable
private fun FigmaMapTiles(earningsState: UiState<EarningsSummary>) {
    val completed = (earningsState as? UiState.Success)?.data?.todayDeliveries?.toString() ?: "425"
    val scheduled = "25"

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MapTile("Heat Map", Icons.Outlined.Tune, Modifier.weight(1f))
        MapTile("Destination", Icons.Filled.ChevronRight, Modifier.weight(1f))
        MapTile("Preferences", Icons.Filled.Settings, Modifier.weight(1f))
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile("Completed Rides", completed, Modifier.weight(1f))
        StatTile("Scheduled Rides", scheduled, Modifier.weight(1f))
        StatTile("Master Driver", "", Modifier.weight(1f), isRank = true)
    }
}

@Composable
private fun MapTile(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBlue),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier, isRank: Boolean = false) {
    Card(
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBlue),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRank) {
                Text("Master", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text("Driver", color = Color.White, fontSize = 9.sp)
            } else {
                Text(label, color = Color.White, fontSize = 9.sp)
                Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HomeBottomSheet(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun OfflineCta(onGoOnline: () -> Unit) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onGoOnline() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FA))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .fillMaxSize()
                    .background(HomeBlue),
                contentAlignment = Alignment.Center
            ) {
                Text("≫", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(strings.goOnline, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = HomeDarkBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OnlineSearchingCard() {
    val strings = LocalStrings.current
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HomeBlue),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(strings.youreOnline, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FindingJobCard() {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.findingJob, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(
                "Effortlessly find your perfect job match with our streamlined 'Finding Job' feature.",
                color = Color(0xFF414755),
                fontSize = 12.sp
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8ECF4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = HomeBlue, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(strings.task, color = Color(0xFF596273), fontSize = 10.sp)
                    Text(strings.departed, color = Color(0xFF11131A), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(strings.currentLocation, color = Color(0xFF596273), fontSize = 10.sp)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Chemical Delivery", color = Color(0xFF11131A), fontSize = 10.sp)
                    Text("20 Feb, 05:00 PM", color = Color(0xFF11131A), fontSize = 10.sp)
                    Text("123 Main Street, Anytown", color = Color(0xFF596273), fontSize = 10.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(strings.tripCost, color = Color(0xFF414755), fontSize = 12.sp)
                Text("Rs 10000", color = Color(0xFF414755), fontSize = 12.sp)
            }
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xFF97CBF1))) {
                Box(modifier = Modifier.fillMaxWidth(0.16f).height(6.dp).clip(RoundedCornerShape(99.dp)).background(HomeBlue))
            }

            Spacer(Modifier.height(12.dp))

            // Distance and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📦 Chemical Delivery",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "📏 12.0 km",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "⏱ 30 min",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun JobsQueueScreen(
    unreadCount: Int,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onJobClick: () -> Unit
) {
    val strings = LocalStrings.current
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HomeTopBar(unreadCount = unreadCount, onMenuClick = onMenuClick, onNotificationsClick = onNotificationsClick)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(strings.jobsTitle, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF11131A), modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9FB)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E8F0))
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        JobsQueueItem(onClick = onJobClick)
                        if (index != 2) {
                            HorizontalDivider(color = Color(0xFFE4E6EC), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobsQueueItem(onClick: () -> Unit) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(strings.theseAreAvailableTruck, fontSize = 10.sp, color = Color(0xFF535A6A))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8ECF4)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.jobs_profile_avatar),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(strings.task, fontSize = 9.sp, color = Color(0xFF667085))
                Text(strings.departed, fontSize = 9.sp, color = Color(0xFF121826))
                Text(strings.currentLocation, fontSize = 9.sp, color = Color(0xFF667085))
                Text(strings.tripCost, fontSize = 9.sp, color = Color(0xFF667085))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("Chemical Delivery", fontSize = 9.sp, color = Color(0xFF121826))
                Text("20 Feb, 05:00 PM", fontSize = 9.sp, color = Color(0xFF121826))
                Text("123 Main Street,\nAnytown, IND 845103", fontSize = 9.sp, color = Color(0xFF121826), lineHeight = 10.sp)
                Text("Rs 10000", fontSize = 9.sp, color = Color(0xFF121826))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(strings.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF11131A))
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Call, contentDescription = null, tint = HomeBlue, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.getInContact, color = HomeBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(strings.decline, color = Color(0xFFA9C4F4), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(10.dp))
        }
    }
}

@Composable
private fun IncomingJobCard(
    job: DeliveryJob,
    remainingSeconds: Long,
    onReject: (String) -> Unit,
    onAccept: (String) -> Unit
) {
    val strings = LocalStrings.current
    val distanceFormatted = ((job.distance * 10.0).roundToInt() / 10.0).toString()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Orange500.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Earnings row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "RM${job.estimatedEarnings.toInt()}",
                    fontSize = 36.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Orange500
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Orange100
                ) {
                    Text(
                        text = "$distanceFormatted km",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Orange500
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (remainingSeconds <= 15) Color(0xFFFFE2E0) else Color(0xFFFFF4D6)
            ) {
                Text(
                    text = "${remainingSeconds.coerceAtLeast(0)}s left",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (remainingSeconds <= 15) Color(0xFFD92D20) else Color(0xFFB54708)
                )
            }

            HorizontalDivider(color = Gray200)

            // Pickup row
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(8.dp).offset(y = 4.dp)
                        .background(Orange500, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(strings.pickup, fontSize = 11.sp, color = Gray500)
                    Text(
                        text = job.pickup.address.ifBlank { job.pickup.shortAddress },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Dropoff row
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(8.dp).offset(y = 4.dp)
                        .background(Gray400, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(strings.dropOff, fontSize = 11.sp, color = Gray500)
                    Text(
                        text = job.dropoff.address.ifBlank { job.dropoff.shortAddress },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Duration chip
            Surface(shape = RoundedCornerShape(8.dp), color = Gray100) {
                Text(
                    text = "~${job.displayDurationMinutes} min",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = Gray700,
                    fontWeight = FontWeight.Medium
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onReject(job.id) },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Gray100, CircleShape)
                ) {
                    Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray600)
                }
                Button(
                    onClick = { onAccept(job.id) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(strings.accept, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun IncomingJobFullScreen(
    jobs: List<DeliveryJob>,
    expiringJobIds: Set<String>,
    onReject: (String) -> Unit,
    onAccept: (String) -> Unit
) {
    val strings = LocalStrings.current
    var currentTimeMillis by remember {
        mutableStateOf(Clock.System.now().toEpochMilliseconds())
    }

    LaunchedEffect(jobs.map { "${it.id}:${it.expiresAt ?: it.createdAt}" }) {
        while (jobs.isNotEmpty()) {
            currentTimeMillis = Clock.System.now().toEpochMilliseconds()
            delay(1000L)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HomeTopBar(unreadCount = 0, onMenuClick = {}, onNotificationsClick = {})
        Spacer(Modifier.height(18.dp))
        Text(
            text = "${jobs.size} ${if (jobs.size == 1) strings.order else strings.orders}",
            modifier = Modifier.padding(horizontal = 18.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            jobs.forEach { job ->
                key(job.id) {
                    AnimatedVisibility(
                        visible = job.id !in expiringJobIds,
                        enter = fadeIn(animationSpec = tween(durationMillis = 150)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 250))
                    ) {
                        IncomingJobCard(
                            job = job,
                            remainingSeconds = (job.remainingOfferMillis(currentTimeMillis) + 999L) / 1000L,
                            onReject = onReject,
                            onAccept = onAccept
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveRideCard(job: DeliveryJob, onPrimaryClick: () -> Unit) {
    val strings = LocalStrings.current
    val statusText = when (job.status) {
        JobStatus.ACCEPTED, JobStatus.HEADING_TO_PICKUP -> strings.rideAccepted
        JobStatus.ARRIVED_AT_PICKUP -> strings.arrivedStatus
        JobStatus.PICKED_UP, JobStatus.IN_TRANSIT -> strings.outForDelivery
        else -> strings.rideDetails
    }

    Text(
        text = statusText,
        color = HomeBlue,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Pickup: ${job.pickup.shortAddress.ifBlank { job.pickup.address }}",
        fontSize = 15.sp,
        color = Color(0xFF414755)
    )
    Text(
        text = "Drop: ${job.dropoff.shortAddress.ifBlank { job.dropoff.address }}",
        fontSize = 15.sp,
        color = Color(0xFF414755)
    )
    Spacer(Modifier.height(12.dp))
    Button(
        onClick = onPrimaryClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HomeBlue),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(if (job.status == JobStatus.ARRIVED_AT_PICKUP) strings.arrivedStatus else strings.viewDetails, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    if (job.status == JobStatus.ARRIVED_AT_PICKUP) {
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8F3)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = strings.issuesWithPickup,
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                textAlign = TextAlign.Center,
                color = Color(0xFF414755),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
