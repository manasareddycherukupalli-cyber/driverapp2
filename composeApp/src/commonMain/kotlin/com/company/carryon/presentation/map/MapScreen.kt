package com.company.carryon.presentation.map

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.theme.*
import com.company.carryon.data.model.LatLng

/**
 * MapScreen — Google Maps integration placeholder with navigation UI.
 * In production, embed actual Google Maps / Apple Maps SDK here.
 * Supports: live tracking, route display, ETA, and turn-by-turn navigation.
 */
@Composable
fun MapScreen(navigator: AppNavigator) {
    val viewModel = remember { MapViewModel() }
    val driverLocation by viewModel.driverLocation.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val eta by viewModel.etaMinutes.collectAsState()
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsState()
    val markers by viewModel.markers.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()

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
            // ---- AWS Location Map ----
            MapViewComposable(
                modifier = Modifier.fillMaxSize(),
                styleUrl = mapStyleUrl,
                centerLat = driverLocation.first,
                centerLng = driverLocation.second,
                zoom = 14.0,
                markers = markers,
                routeGeometry = routeGeometry
            )

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
                                text = "$eta min",
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Orange500
                            )
                        }
                        // Recenter button
                        FloatingActionButton(
                            onClick = { /* Recenter map */ },
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
                        // Open in Google Maps
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
            FloatingActionButton(
                onClick = { viewModel.startTracking() },
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
