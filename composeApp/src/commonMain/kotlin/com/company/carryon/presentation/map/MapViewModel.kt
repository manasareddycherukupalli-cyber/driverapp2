package com.company.carryon.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.network.LocationApi
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MarkerColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    // Driver's current location
    private val _driverLocation = MutableStateFlow(Pair(3.1390, 101.6869)) // Kuala Lumpur default
    val driverLocation: StateFlow<Pair<Double, Double>> = _driverLocation.asStateFlow()

    // Whether tracking is active
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // ETA in minutes
    private val _etaMinutes = MutableStateFlow(15)
    val etaMinutes: StateFlow<Int> = _etaMinutes.asStateFlow()

    // Map style URL from AWS Location
    private val _mapStyleUrl = MutableStateFlow("")
    val mapStyleUrl: StateFlow<String> = _mapStyleUrl.asStateFlow()

    // Route geometry for polyline
    private val _routeGeometry = MutableStateFlow<List<LatLng>?>(null)
    val routeGeometry: StateFlow<List<LatLng>?> = _routeGeometry.asStateFlow()

    // Map markers
    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    init {
        loadMapConfig()
    }

    private fun loadMapConfig() {
        viewModelScope.launch {
            LocationApi.getMapConfig().onSuccess { config ->
                _mapStyleUrl.value = config.styleUrl
            }
        }
    }

    fun loadRoute(pickupLat: Double, pickupLng: Double, dropLat: Double, dropLng: Double) {
        viewModelScope.launch {
            _markers.value = listOf(
                MapMarker("pickup", pickupLat, pickupLng, "Pickup", MarkerColor.GREEN),
                MapMarker("dropoff", dropLat, dropLng, "Drop-off", MarkerColor.RED)
            )
            LocationApi.calculateRoute(pickupLat, pickupLng, dropLat, dropLng).onSuccess { result ->
                _routeGeometry.value = result.geometry
                _etaMinutes.value = result.duration
            }
        }
    }

    fun startTracking() {
        _isTracking.value = true
    }

    fun stopTracking() {
        _isTracking.value = false
    }

    fun updateLocation(lat: Double, lng: Double) {
        _driverLocation.value = Pair(lat, lng)
    }
}
