package com.company.carryon.presentation.jobs

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.data.network.LocationApi
import com.company.carryon.data.network.getLastKnownLocation
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.job_dispatch_section
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.math.roundToInt

private val DispatchBlue = Color(0xFF4D7EE7)
private val DispatchBg = Color(0xFFF6F7FB)
private val CardBg = Color(0xFFF3F5FA)
private val TextDark = Color(0xFF222631)
private val TextMuted = Color(0xFF4F5B72)

@Composable
fun JobDetailsScreen(navigator: AppNavigator) {
    val viewModel = remember { JobsViewModel() }
    val jobDetailsState by viewModel.jobDetails.collectAsState()
    val jobId = navigator.selectedJobId

    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJobDetails(it) }
    }

    when (val state = jobDetailsState) {
        is UiState.Loading, UiState.Idle -> LoadingScreen()
        is UiState.Error -> ErrorState(
            message = state.message,
            onRetry = { jobId?.let { viewModel.loadJobDetails(it) } }
        )
        is UiState.Success -> JobDetailsContent(
            job = state.data,
            navigator = navigator,
            onUpdateStatus = { status -> viewModel.updateJobStatus(state.data.id, status) }
        )
    }
}

@Composable
private fun JobDetailsContent(
    job: DeliveryJob,
    navigator: AppNavigator,
    onUpdateStatus: (JobStatus) -> Unit
) {
    var driverLocation by remember(job.id) { mutableStateOf<Pair<Double, Double>?>(null) }
    var routeGeometry by remember(job.id) { mutableStateOf<List<LatLng>?>(null) }
    var routeDistanceKm by remember(job.id) { mutableStateOf(0.0) }
    var routeEtaMinutes by remember(job.id) { mutableStateOf(0) }
    var routeLoading by remember(job.id) { mutableStateOf(true) }

    val hasPickupCoordinates = remember(job.pickup.latitude, job.pickup.longitude) {
        isValidCoordinate(job.pickup.latitude, job.pickup.longitude)
    }

    LaunchedEffect(job.id, job.pickup.latitude, job.pickup.longitude) {
        routeLoading = true
        while (isActive) {
            val location = getLastKnownLocation()
            driverLocation = location?.takeIf { isValidCoordinate(it.first, it.second) }

            val currentDriverLocation = driverLocation
            if (currentDriverLocation != null && hasPickupCoordinates) {
                LocationApi.calculateRoute(
                    originLat = currentDriverLocation.first,
                    originLng = currentDriverLocation.second,
                    destLat = job.pickup.latitude,
                    destLng = job.pickup.longitude
                ).onSuccess { result ->
                    routeDistanceKm = result.distance
                    routeEtaMinutes = result.duration.takeIf { it > 0 } ?: estimateMinutesFromDistance(result.distance)
                    routeGeometry = result.geometry
                }.onFailure {
                    routeGeometry = null
                }
            }
            routeLoading = false
            delay(15_000L)
        }
    }

    val effectiveDistanceKm = routeDistanceKm.takeIf { it > 0.0 } ?: job.distance
    val effectiveEtaMinutes = routeEtaMinutes.takeIf { it > 0 } ?: job.displayDurationMinutes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DispatchBg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DriveAppTopBar(
                title = "Dispatch",
                onBackClick = { navigator.switchTab(Screen.Home) },
                leadingIcon = Icons.Filled.Menu,
                onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
                showTitle = false
            )
            DispatchMapHero(
                pickupLat = job.pickup.latitude,
                pickupLng = job.pickup.longitude,
                driverLocation = driverLocation,
                routeGeometry = routeGeometry,
                distanceKm = effectiveDistanceKm,
                etaMinutes = effectiveEtaMinutes,
                loading = routeLoading
            )
            OrderSummaryCard(job)
            RouteCard(job)
            CustomerCard(
                job = job,
                onChatClick = { navigator.openCustomerChat(job.id, job.customerName.ifBlank { "Customer" }) }
            )
            PackageCard(job)
            StartDeliveryButton(job, navigator, onUpdateStatus)
            ReportIssueButton()
            Spacer(Modifier.height(6.dp))
        }
        DispatchBottomBar()
    }
}

@Composable
private fun DispatchMapHero(
    pickupLat: Double,
    pickupLng: Double,
    driverLocation: Pair<Double, Double>?,
    routeGeometry: List<LatLng>?,
    distanceKm: Double,
    etaMinutes: Int,
    loading: Boolean
) {
    val hasPickup = isValidCoordinate(pickupLat, pickupLng)

    val markers = buildList {
        if (driverLocation != null) {
            add(MapMarker(id = "driver", lat = driverLocation.first, lng = driverLocation.second, title = "You", color = MarkerColor.BLUE))
        }
        if (hasPickup) {
            add(MapMarker(id = "pickup", lat = pickupLat, lng = pickupLng, title = "Pickup", color = MarkerColor.GREEN))
        }
    }

    val centerLat = driverLocation?.first ?: pickupLat
    val centerLng = driverLocation?.second ?: pickupLng

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12202E))
    ) {
        Box(Modifier.fillMaxSize()) {
            if (hasPickup) {
                MapViewComposable(
                    modifier = Modifier.fillMaxSize(),
                    centerLat = centerLat,
                    centerLng = centerLng,
                    zoom = if (driverLocation != null) 14.0 else 13.0,
                    markers = markers,
                    routeGeometry = routeGeometry,
                    showDriverLocation = driverLocation != null
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.job_dispatch_section),
                    contentDescription = "Dispatch map placeholder",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    icon = Icons.Filled.MyLocation,
                    text = if (!loading && distanceKm > 0) "${formatOneDecimal(distanceKm)} km" else "-- km"
                )
                MetricPill(
                    icon = Icons.Filled.Schedule,
                    text = if (!loading && etaMinutes > 0) "$etaMinutes MIN" else "-- MIN"
                )
            }
        }
    }
}

@Composable
private fun MetricPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFEAEFF6))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = DispatchBlue, modifier = Modifier.size(14.dp))
        DisableSelection {
            Text(text, color = TextDark, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun OrderSummaryCard(job: DeliveryJob) {
    val strings = LocalStrings.current
    val displayOrderId = remember(job.id, job.displayOrderId) {
        job.displayOrderId.takeIf { it.isNotBlank() }
            ?: "ORD-${job.id.takeLast(6).uppercase()}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(strings.orderId, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    displayOrderId,
                    color = TextDark,
                    fontSize = 30.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HorizontalDivider(color = Color(0xFFE8ECF5))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.potentialEarnings,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "RM ${job.estimatedEarnings.toInt()}",
                    color = DispatchBlue,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RouteCard(job: DeliveryJob) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RouteStop(
                label = strings.pickup,
                title = job.pickup.shortAddress.ifBlank { job.pickup.address },
                subtitle = job.pickup.address,
                highlight = DispatchBlue
            )
            RouteStop(
                label = strings.dropOff,
                title = job.dropoff.shortAddress.ifBlank { job.dropoff.address },
                subtitle = job.dropoff.address,
                highlight = DispatchBlue,
                isLast = true
            )
        }
    }
}

@Composable
private fun RouteStop(
    label: String,
    title: String,
    subtitle: String,
    highlight: Color,
    isLast: Boolean = false
) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(highlight.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(highlight))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(34.dp).background(highlight.copy(alpha = 0.35f)))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(title, color = DispatchBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = TextDark, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CustomerCard(
    job: DeliveryJob,
    onChatClick: () -> Unit
) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFD5E5FF)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = DispatchBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(strings.customer, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text(job.customerName, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("★ 4.9", color = DispatchBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircleActionIcon(Icons.Filled.Phone)
                CircleActionIcon(Icons.Filled.ChatBubbleOutline, onClick = onChatClick)
            }
        }
    }
}

@Composable
private fun CircleActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFD5E5FF))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = DispatchBlue, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PackageCard(job: DeliveryJob) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.packageDetails, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFD5E5FF)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = DispatchBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(job.packageType, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TinyChip(text = job.packageSize.displayName.uppercase())
                        TinyChip(text = job.notes?.takeIf { it.isNotBlank() }?.uppercase() ?: "FRAGILE")
                    }
                }
            }
        }
    }
}

@Composable
private fun TinyChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFC9DCFA))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, color = DispatchBlue, fontWeight = FontWeight.Bold, fontSize = 8.sp)
    }
}

@Composable
private fun StartDeliveryButton(
    job: DeliveryJob,
    navigator: AppNavigator,
    onUpdateStatus: (JobStatus) -> Unit
) {
    val strings = LocalStrings.current
    val label = when (job.status) {
        JobStatus.ACCEPTED -> strings.startDelivery
        JobStatus.HEADING_TO_PICKUP, JobStatus.ARRIVED_AT_PICKUP, JobStatus.PICKED_UP, JobStatus.IN_TRANSIT -> strings.continueDelivery
        JobStatus.ARRIVED_AT_DROP -> strings.completeDelivery
        else -> strings.startDelivery
    }

    Button(
        onClick = {
            navigator.selectedJobId = job.id
            when (job.status) {
                JobStatus.ACCEPTED -> {
                    onUpdateStatus(JobStatus.HEADING_TO_PICKUP)
                    navigator.navigateTo(Screen.MapNavigation)
                }
                JobStatus.HEADING_TO_PICKUP, JobStatus.ARRIVED_AT_PICKUP, JobStatus.PICKED_UP, JobStatus.IN_TRANSIT -> {
                    navigator.navigateTo(Screen.MapNavigation)
                }
                JobStatus.ARRIVED_AT_DROP -> navigator.navigateTo(Screen.ProofOfDelivery)
                else -> navigator.navigateTo(Screen.MapNavigation)
            }
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DispatchBlue)
    ) {
        Icon(Icons.Filled.MailOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReportIssueButton() {
    val strings = LocalStrings.current
    OutlinedButton(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DispatchBlue),
        border = androidx.compose.foundation.BorderStroke(2.dp, DispatchBlue)
    ) {
        Text(strings.reportIssue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DispatchBottomBar() {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem(strings.deliveriesLabel, Icons.Filled.LocalShipping, selected = false)
        BottomItem(strings.earnings, Icons.Filled.AccountCircle, selected = true)
        BottomItem(strings.inboxLabel, Icons.Filled.MailOutline, selected = false)
        BottomItem(strings.accountLabel, Icons.Filled.Person, selected = false)
    }
}

@Composable
private fun BottomItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) Color(0xFFE4E8FF) else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(icon, contentDescription = label, tint = if (selected) DispatchBlue else TextMuted, modifier = Modifier.size(16.dp))
        }
        Text(label, color = if (selected) DispatchBlue else TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
    }
}

private fun isValidCoordinate(lat: Double, lng: Double): Boolean {
    return lat in -90.0..90.0 && lng in -180.0..180.0 && (lat != 0.0 || lng != 0.0)
}

private fun estimateMinutesFromDistance(distanceKm: Double): Int {
    if (distanceKm <= 0.0) return 0
    return kotlin.math.ceil((distanceKm / 30.0) * 60.0).toInt().coerceAtLeast(1)
}

private fun formatOneDecimal(value: Double): String {
    val scaled = (value * 10).roundToInt()
    val absScaled = abs(scaled)
    val whole = absScaled / 10
    val fraction = absScaled % 10
    val sign = if (scaled < 0) "-" else ""
    return "$sign$whole.$fraction"
}
