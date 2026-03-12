package com.example.drive_app.presentation.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MapViewModel — Manages map state and location tracking.
 * In production, integrates with Google Maps SDK for real-time tracking.
 */
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
