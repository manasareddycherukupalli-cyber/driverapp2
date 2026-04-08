package com.company.carryon.presentation.map

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.LatLng
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MapViewComposable
import com.company.carryon.presentation.components.MarkerColor
import com.company.carryon.presentation.navigation.AppNavigator

private val NavBlue = Color(0xFF2F80ED)
private val NavSoft = Color(0x4DA6D2F3)
private val NavWhite = Color(0xFFFFFFFF)
private val NavBlack = Color(0xFF000000)
private val DefaultCenter = LatLng(12.9716, 77.5946)

@Composable
fun MapScreen(navigator: AppNavigator) {
    val viewModel = remember { MapViewModel() }
    val driverLocation by viewModel.driverLocation.collectAsState()
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val eta by viewModel.etaMinutes.collectAsState()
    val currentJob by viewModel.currentJob.collectAsState()
    val markers by viewModel.markers.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val routeError by viewModel.routeError.collectAsState()
    val uriHandler = LocalUriHandler.current

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
            markers = buildList {
                if (driverLocation != null) {
                    add(
                        MapMarker(
                            id = "driver",
                            lat = driverLocation!!.first,
                            lng = driverLocation!!.second,
                            title = "You",
                            color = MarkerColor.BLUE
                        )
                    )
                }
                addAll(markers)
            }
            ,
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
                Icon(Icons.Filled.MoreVert, contentDescription = null, tint = NavBlue)
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
                        Text("Terus di Jalan\nDamansara", color = NavWhite, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Navigate", fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val job = currentJob ?: return@OutlinedButton
                                    val destination = if (job.status.ordinal <= JobStatus.ARRIVED_AT_PICKUP.ordinal) job.pickup else job.dropoff
                                    val phone = destination.contactPhone?.filter { it.isDigit() || it == '+' }.orEmpty()
                                    if (phone.isNotBlank()) {
                                        uriHandler.openUri("tel:$phone")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Call", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (eta > 0) eta.toString() else "8", color = NavWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("MIN", color = NavWhite.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(if (isTracking) "TRACKING" else "PAUSED", color = NavWhite.copy(alpha = 0.85f), fontSize = 12.sp)
                    if (routeError != null) {
                        Text("Route unavailable", color = NavWhite.copy(alpha = 0.85f), fontSize = 11.sp)
                    } else {
                        Text("1.4 km", color = NavWhite.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 80.dp)
                .size(42.dp)
                .background(NavBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Navigation, contentDescription = null, tint = NavWhite, modifier = Modifier.size(20.dp))
        }

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
                .padding(horizontal = 14.dp, vertical = 76.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NavWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ChipText("ORDER ID")
                    ChipText("PICKUP TASK")
                }
                Text("#DE-9921", color = NavBlack, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Route, contentDescription = null, tint = NavBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Kedai Shahril", color = NavBlue, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { navigator.navigateTo(com.company.carryon.presentation.navigation.Screen.ActiveDelivery) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("I’ve Arrived!  ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Filled.Navigation, contentDescription = null, tint = NavWhite, modifier = Modifier.size(16.dp))
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
                            Text("12:45 PM", color = NavBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                            Text("RM14.50", color = NavBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(NavWhite, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab("ROUTE", true)
            BottomTab("EARNINGS", false)
            Box(modifier = Modifier.size(52.dp).background(NavBlue, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Navigation, contentDescription = null, tint = NavWhite)
            }
            BottomTab("INBOX", false)
            BottomTab("ACCOUNT", false)
        }
    }
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

@Composable
private fun BottomTab(label: String, selected: Boolean) {
    Text(
        text = label,
        color = if (selected) NavBlue else NavBlack.copy(alpha = 0.55f),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
    )
}
