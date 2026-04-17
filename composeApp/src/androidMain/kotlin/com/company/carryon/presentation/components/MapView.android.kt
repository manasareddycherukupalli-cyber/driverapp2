package com.company.carryon.presentation.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.company.carryon.data.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng as GmsLatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@SuppressLint("MissingPermission")
@Composable
actual fun MapViewComposable(
    modifier: Modifier,
    styleUrl: String,
    centerLat: Double,
    centerLng: Double,
    zoom: Double,
    markers: List<MapMarker>,
    routeGeometry: List<LatLng>?,
    showDriverLocation: Boolean,
    onMapClick: ((Double, Double) -> Unit)?
) {
    val cameraPositionState = rememberCameraPositionState()

    // Move camera when center/zoom changes
    LaunchedEffect(centerLat, centerLng, zoom, routeGeometry, markers) {
        if (!routeGeometry.isNullOrEmpty()) {
            val polylinePoints = routeGeometry.map { GmsLatLng(it.lat, it.lng) }
            if (polylinePoints.size >= 2) {
                val boundsBuilder = LatLngBounds.builder()
                polylinePoints.forEach { boundsBuilder.include(it) }
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80)
                )
            }
        } else if (markers.size >= 2) {
            val boundsBuilder = LatLngBounds.builder()
            markers.forEach { boundsBuilder.include(GmsLatLng(it.lat, it.lng)) }
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80)
            )
        } else if (centerLat != 0.0 || centerLng != 0.0) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(GmsLatLng(centerLat, centerLng), zoom.toFloat())
            )
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
        )
    }

    val mapProperties = remember(showDriverLocation) {
        MapProperties(isMyLocationEnabled = showDriverLocation)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = mapProperties,
        onMapClick = { latLng ->
            onMapClick?.invoke(latLng.latitude, latLng.longitude)
        }
    ) {
        // Add markers
        for (marker in markers) {
            key(marker.id) {
                val hue = when (marker.color) {
                    MarkerColor.RED -> BitmapDescriptorFactory.HUE_RED
                    MarkerColor.BLUE -> BitmapDescriptorFactory.HUE_BLUE
                    MarkerColor.GREEN -> BitmapDescriptorFactory.HUE_GREEN
                }
                val markerState = rememberMarkerState(key = marker.id, position = GmsLatLng(marker.lat, marker.lng))
                LaunchedEffect(marker.lat, marker.lng) {
                    markerState.position = GmsLatLng(marker.lat, marker.lng)
                }
                Marker(
                    state = markerState,
                    title = marker.title,
                    icon = BitmapDescriptorFactory.defaultMarker(hue),
                )
            }
        }

        // Draw route polyline
        if (!routeGeometry.isNullOrEmpty()) {
            val polylinePoints = routeGeometry.map { GmsLatLng(it.lat, it.lng) }
            Polyline(
                points = polylinePoints,
                color = androidx.compose.ui.graphics.Color(AndroidColor.parseColor("#2F80ED")),
                width = 10f,
            )
        }
    }
}
