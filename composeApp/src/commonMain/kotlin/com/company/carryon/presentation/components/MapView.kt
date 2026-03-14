package com.company.carryon.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.company.carryon.data.model.LatLng

data class MapMarker(
    val id: String,
    val lat: Double,
    val lng: Double,
    val title: String,
    val color: MarkerColor = MarkerColor.RED
)

enum class MarkerColor { RED, BLUE, GREEN }

@Composable
expect fun MapViewComposable(
    modifier: Modifier = Modifier,
    styleUrl: String = "",
    centerLat: Double = 3.1390,
    centerLng: Double = 101.6869,
    zoom: Double = 12.0,
    markers: List<MapMarker> = emptyList(),
    routeGeometry: List<LatLng>? = null,
    onMapClick: ((Double, Double) -> Unit)? = null
)
