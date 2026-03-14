package com.company.carryon.presentation.components

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.company.carryon.data.model.LatLng
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng as MLNLatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions

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
    val context = LocalContext.current

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync { map ->
                val effectiveStyleUrl = styleUrl.ifBlank {
                    "https://demotiles.maplibre.org/style.json"
                }

                fun applyMapContent() {
                    if (centerLat != 0.0 || centerLng != 0.0) {
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(MLNLatLng(centerLat, centerLng), zoom)
                        )
                    }

                    map.clear()

                    for (marker in markers) {
                        map.addMarker(
                            MarkerOptions()
                                .position(MLNLatLng(marker.lat, marker.lng))
                                .title(marker.title)
                        )
                    }

                    if (!routeGeometry.isNullOrEmpty()) {
                        val polylinePoints = routeGeometry.map { MLNLatLng(it.lat, it.lng) }
                        map.addPolyline(
                            PolylineOptions()
                                .addAll(polylinePoints)
                                .color(AndroidColor.parseColor("#2F80ED"))
                                .width(5f)
                        )

                        if (polylinePoints.size >= 2) {
                            val boundsBuilder = LatLngBounds.Builder()
                            polylinePoints.forEach { boundsBuilder.include(it) }
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80)
                            )
                        }
                    } else if (markers.size >= 2) {
                        val boundsBuilder = LatLngBounds.Builder()
                        markers.forEach { boundsBuilder.include(MLNLatLng(it.lat, it.lng)) }
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80)
                        )
                    }
                }

                if (map.style == null || map.style?.uri != effectiveStyleUrl) {
                    map.setStyle(Style.Builder().fromUri(effectiveStyleUrl)) {
                        onMapClick?.let { callback ->
                            map.addOnMapClickListener { latLng ->
                                callback(latLng.latitude, latLng.longitude)
                                true
                            }
                        }
                        applyMapContent()
                    }
                } else {
                    applyMapContent()
                }
            }
        }
    )
}
