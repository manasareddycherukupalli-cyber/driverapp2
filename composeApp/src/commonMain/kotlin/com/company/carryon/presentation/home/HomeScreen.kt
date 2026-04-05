package com.company.carryon.presentation.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.EarningsSummary
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val HomeBlue = Color(0xFF2F80ED)
private val HomeDarkBlue = Color(0xFF011E60)
private val GlassBlue = Color(0x801D3772)
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
    val incomingJob by viewModel.incomingJob.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()
    val driverLocation by viewModel.driverLocation.collectAsState()
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshDriverLocation()
        viewModel.loadDashboardData()
    }

    val activeJob = (activeJobsState as? UiState.Success<List<DeliveryJob>>)?.data?.firstOrNull()
    val scope = rememberCoroutineScope()
    var onlineState by remember { mutableStateOf(HomeOnlineState.OFFLINE) }
    var startedOnlineFlow by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline, activeJob?.id, incomingJob?.id, startedOnlineFlow) {
        if (!startedOnlineFlow) {
            onlineState = HomeOnlineState.OFFLINE
            return@LaunchedEffect
        }
        if (!isOnline) {
            onlineState = HomeOnlineState.OFFLINE
            startedOnlineFlow = false
            return@LaunchedEffect
        }
        if (incomingJob == null && activeJob != null && onlineState == HomeOnlineState.JOBS_QUEUE) {
            onlineState = HomeOnlineState.ACTIVE_JOB
        }
    }

    if (incomingJob != null) {
        IncomingJobFullScreen(
            job = incomingJob!!,
            onReject = { viewModel.rejectIncomingJob() },
            onAccept = { viewModel.acceptIncomingJob() }
        )
        return
    }

    if (onlineState == HomeOnlineState.JOBS_QUEUE) {
        JobsQueueScreen(
            unreadCount = unreadCount,
            onMenuClick = { navigator.switchTab(Screen.Profile) },
            onNotificationsClick = { navigator.navigateTo(Screen.Notifications) }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(HomeBlue)) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar(
                unreadCount = unreadCount,
                onMenuClick = { navigator.switchTab(Screen.Profile) },
                onNotificationsClick = { navigator.navigateTo(Screen.Notifications) }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                BlueMap(driverLocation = driverLocation, mapStyleUrl = mapStyleUrl)

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FigmaMapTiles(earningsState = earningsState)
                }
            }

            HomeBottomSheet {
                when {
                    !isOnline -> {
                        OfflineCta(
                            onGoOnline = {
                                if (startedOnlineFlow) return@OfflineCta
                                startedOnlineFlow = true
                                scope.launch {
                                    viewModel.toggleOnlineStatus()
                                    onlineState = HomeOnlineState.JUST_ONLINE
                                    delay(1500)
                                    onlineState = HomeOnlineState.FINDING_JOB
                                    delay(1800)
                                    onlineState = HomeOnlineState.JOBS_QUEUE
                                }
                            }
                        )
                    }
                    onlineState == HomeOnlineState.ACTIVE_JOB && activeJob != null -> {
                        ActiveRideCard(
                            job = activeJob,
                            onPrimaryClick = {
                                navigator.selectedJobId = activeJob.id
                                navigator.navigateTo(Screen.JobDetails)
                            }
                        )
                    }
                    onlineState == HomeOnlineState.JUST_ONLINE -> {
                        OnlineSearchingCard()
                    }
                    else -> {
                        FindingJobCard()
                    }
                }
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
    val markers = buildList {
        if (driverLocation != null) {
            add(
                MapMarker(
                    id = "driver",
                    lat = driverLocation.first,
                    lng = driverLocation.second,
                    title = "You",
                    color = MarkerColor.BLUE
                )
            )
            add(MapMarker(id = "pickup-1", lat = driverLocation.first + 0.002, lng = driverLocation.second - 0.004, title = "Pickup", color = MarkerColor.RED))
            add(MapMarker(id = "pickup-2", lat = driverLocation.first - 0.003, lng = driverLocation.second + 0.005, title = "Drop", color = MarkerColor.RED))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapViewComposable(
            modifier = Modifier.fillMaxSize(),
            styleUrl = mapStyleUrl,
            centerLat = driverLocation?.first ?: DefaultCenter.lat,
            centerLng = driverLocation?.second ?: DefaultCenter.lng,
            zoom = if (driverLocation != null) 13.5 else 12.0,
            markers = markers
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x4D2F80ED))
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
                Text(value, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
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
                Text("≫", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Text("Go Online", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = HomeDarkBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OnlineSearchingCard() {
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HomeBlue),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text("You're Online!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FindingJobCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Finding Job", fontWeight = FontWeight.Bold, fontSize = 24.sp)
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
                    Text("Task", color = Color(0xFF596273), fontSize = 10.sp)
                    Text("Departed", color = Color(0xFF11131A), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text("Current Location", color = Color(0xFF596273), fontSize = 10.sp)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Chemical Delivery", color = Color(0xFF11131A), fontSize = 10.sp)
                    Text("20 Feb, 05:00 PM", color = Color(0xFF11131A), fontSize = 10.sp)
                    Text("123 Main Street, Anytown", color = Color(0xFF596273), fontSize = 10.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Name", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Trip Cost", color = Color(0xFF414755), fontSize = 12.sp)
                Text("Rs 10000", color = Color(0xFF414755), fontSize = 12.sp)
            }
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xFF97CBF1))) {
                Box(modifier = Modifier.fillMaxWidth(0.16f).height(6.dp).clip(RoundedCornerShape(99.dp)).background(HomeBlue))
            }
        }
    }
}

@Composable
private fun JobsQueueScreen(
    unreadCount: Int,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HomeTopBar(unreadCount = unreadCount, onMenuClick = onMenuClick, onNotificationsClick = onNotificationsClick)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text("Jobs", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF11131A), modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9FB)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E8F0))
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        JobsQueueItem()
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
private fun JobsQueueItem() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
        Text("These are the available truck", fontSize = 10.sp, color = Color(0xFF535A6A))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8ECF4)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = HomeBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("Task", fontSize = 9.sp, color = Color(0xFF667085))
                Text("Departed", fontSize = 9.sp, color = Color(0xFF121826))
                Text("Current Location", fontSize = 9.sp, color = Color(0xFF667085))
                Text("Trip Cost", fontSize = 9.sp, color = Color(0xFF667085))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("Chemical Delivery", fontSize = 9.sp, color = Color(0xFF121826))
                Text("20 Feb, 05:00 PM", fontSize = 9.sp, color = Color(0xFF121826))
                Text("123 Main Street,\nAnytown, IND 845103", fontSize = 9.sp, color = Color(0xFF121826), lineHeight = 10.sp)
                Text("Rs 10000", fontSize = 9.sp, color = Color(0xFF121826))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Name", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF11131A))
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Call, contentDescription = null, tint = HomeBlue, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Get in Contact", color = HomeBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("Decline", color = Color(0xFFA9C4F4), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(10.dp))
        }
    }
}

@Composable
private fun IncomingJobCard(job: DeliveryJob, onReject: () -> Unit, onAccept: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E8E8))
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Incoming Job Request", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(5) { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (idx in 1..2) 12.dp else 9.dp)
                            .clip(CircleShape)
                            .background(if (idx < 2) HomeBlue else Color(0xFFB7DAF5))
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Pickup: ${job.pickup.shortAddress.ifBlank { job.pickup.address }}\nDrop: ${job.dropoff.shortAddress.ifBlank { job.dropoff.address }}",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text("Accept Job?", fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7DAF5)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Reject", color = Color.Black, fontSize = 14.sp) }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HomeBlue),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Accept", fontSize = 14.sp) }
            }
        }
    }
}

@Composable
private fun IncomingJobFullScreen(job: DeliveryJob, onReject: () -> Unit, onAccept: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HomeTopBar(unreadCount = 0, onMenuClick = {}, onNotificationsClick = {})
        Spacer(Modifier.height(64.dp))
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            IncomingJobCard(job = job, onReject = onReject, onAccept = onAccept)
        }
    }
}

@Composable
private fun ActiveRideCard(job: DeliveryJob, onPrimaryClick: () -> Unit) {
    val statusText = when (job.status) {
        JobStatus.ACCEPTED, JobStatus.HEADING_TO_PICKUP -> "Ride Accepted!"
        JobStatus.ARRIVED_AT_PICKUP -> "Arrived!"
        JobStatus.PICKED_UP, JobStatus.IN_TRANSIT -> "Out for delivery"
        else -> "Ride Details"
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
        Text(if (job.status == JobStatus.ARRIVED_AT_PICKUP) "Arrived!" else "View details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    if (job.status == JobStatus.ARRIVED_AT_PICKUP) {
        Spacer(Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8F3)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Issues with pickup?",
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                textAlign = TextAlign.Center,
                color = Color(0xFF414755),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
