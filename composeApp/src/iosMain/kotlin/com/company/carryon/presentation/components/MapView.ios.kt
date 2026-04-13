package com.company.carryon.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.company.carryon.data.model.LatLng
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
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
    UIKitView(
        factory = {
            MKMapView().apply {
                showsUserLocation = showDriverLocation
            }
        },
        modifier = modifier,
        update = { mapView ->
            mapView.showsUserLocation = showDriverLocation
            // Set the region based on center and zoom
            val zoomMeters = 40_000_000.0 / pow2(zoom)
            val center = CLLocationCoordinate2DMake(centerLat, centerLng)
            val region = MKCoordinateRegionMakeWithDistance(center, zoomMeters, zoomMeters)
            mapView.setRegion(region, animated = true)

            // Clear existing custom annotations (keep user location)
            val existingAnnotations = mapView.annotations
                .filterIsInstance<MKPointAnnotation>()
            if (existingAnnotations.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                mapView.removeAnnotations(existingAnnotations as List<MKAnnotationProtocol>)
            }

            // Add markers
            for (marker in markers) {
                val annotation = MKPointAnnotation()
                annotation.setCoordinate(CLLocationCoordinate2DMake(marker.lat, marker.lng))
                annotation.setTitle(marker.title)
                mapView.addAnnotation(annotation)
            }
        }
    )
}

private fun pow2(exp: Double): Double {
    var result = 1.0
    val intExp = exp.toInt()
    for (i in 0 until intExp) {
        result *= 2.0
    }
    return result
}
