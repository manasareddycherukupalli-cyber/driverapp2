package com.company.carryon.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.company.carryon.data.model.LatLng

@Composable
actual fun MapViewComposable(
    modifier: Modifier,
    styleUrl: String,
    centerLat: Double,
    centerLng: Double,
    zoom: Double,
    markers: List<MapMarker>,
    routeGeometry: List<LatLng>?,
    onMapClick: ((Double, Double) -> Unit)?
) {
    Box(
        modifier = modifier.background(Color(0xFFE8EAF6)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Map View\n(${centerLat.toString().take(8)}, ${centerLng.toString().take(8)})",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
