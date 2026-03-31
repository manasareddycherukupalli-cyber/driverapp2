package com.company.carryon.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.theme.*
import com.company.carryon.data.model.LatLng

@Composable
fun MapScreen(navigator: AppNavigator) {
    val viewModel = remember { MapViewModel() }
    val driverLocation by viewModel.driverLocation.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val eta by viewModel.etaMinutes.collectAsState()
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsState()
    val markers by viewModel.markers.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val routeError by viewModel.routeError.collectAsState()

    // Load the job route when screen opens
    val jobId = navigator.selectedJobId
    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJob(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Navigation",
            onBackClick = { navigator.goBack() }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val loc = driverLocation
            if (loc != null) {
                // ---- Google Maps ----
                MapViewComposable(
                    modifier = Modifier.fillMaxSize(),
                    styleUrl = mapStyleUrl,
                    centerLat = loc.first,
                    centerLng = loc.second,
                    zoom = 14.0,
                    markers = markers,
                    routeGeometry = routeGeometry
                )
            } else {
                // Loading state while waiting for GPS
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Orange500)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Getting your location...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ---- Bottom Navigation Card ----
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // ETA Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimated Arrival",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when {
                                    eta > 0 -> "$eta min"
                                    routeError != null -> "Unavailable"
                                    else -> "Calculating..."
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = if (routeError != null) MaterialTheme.colorScheme.error else Orange500
                            )
                        }
                        // Recenter button
                        FloatingActionButton(
                            onClick = { viewModel.refreshLocation() },
                            containerColor = Orange500,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = "Recenter", tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Navigation actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Open in external maps app
                        OutlinedButton(
                            onClick = { /* Open in external maps app */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Navigate", fontWeight = FontWeight.SemiBold)
                        }

                        // Call customer
                        OutlinedButton(
                            onClick = { /* Open phone dialer */ },
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

            // ---- My Location FAB ----
            if (driverLocation != null) {
                FloatingActionButton(
                    onClick = { viewModel.refreshLocation() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = Color.White,
                    contentColor = Orange500
                ) {
                    Icon(Icons.Filled.GpsFixed, contentDescription = "My Location")
                }
            }
        }
    }
}
