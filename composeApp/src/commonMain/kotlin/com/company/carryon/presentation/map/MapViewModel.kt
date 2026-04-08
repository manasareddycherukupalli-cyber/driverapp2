package com.company.carryon.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.LatLng
import com.company.carryon.data.model.UiState
import com.company.carryon.data.network.LocationApi
import com.company.carryon.data.network.getLastKnownLocation
import com.company.carryon.di.ServiceLocator
import com.company.carryon.presentation.components.MapMarker
import com.company.carryon.presentation.components.MarkerColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val jobRepository = ServiceLocator.jobRepository

    // Driver's current location (real GPS)
    private val _driverLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val driverLocation: StateFlow<Pair<Double, Double>?> = _driverLocation.asStateFlow()

    // ETA in minutes
    private val _etaMinutes = MutableStateFlow(0)
    val etaMinutes: StateFlow<Int> = _etaMinutes.asStateFlow()

    // Loaded job details for destination metadata (contact, status)
    private val _currentJob = MutableStateFlow<DeliveryJob?>(null)
    val currentJob: StateFlow<DeliveryJob?> = _currentJob.asStateFlow()

    // Route geometry for polyline
    private val _routeGeometry = MutableStateFlow<List<LatLng>?>(null)
    val routeGeometry: StateFlow<List<LatLng>?> = _routeGeometry.asStateFlow()

    // Map markers
    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    // Whether tracking is active
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // Location polling job
    private var trackingJob: Job? = null

    init {
        refreshLocation()
    }

    /** Refresh driver's GPS location immediately */
    fun refreshLocation() {
        getLastKnownLocation()?.let { (lat, lng) ->
            if (isValidCoordinate(lat, lng)) {
                _driverLocation.value = Pair(lat, lng)
            }
        }
    }

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Whether route calculation failed (so UI can show error instead of "Calculating...")
    private val _routeError = MutableStateFlow<String?>(null)
    val routeError: StateFlow<String?> = _routeError.asStateFlow()

    // Cached job for retrying route calc once GPS arrives
    private var pendingRouteJob: DeliveryJob? = null

    /** Load the job and calculate route from driver to the relevant destination */
    fun loadJob(jobId: String) {
        viewModelScope.launch {
            jobRepository.getJobDetails(jobId)
                .onSuccess { job ->
                    _error.value = null
                    _currentJob.value = job
                    if (_driverLocation.value != null) {
                        setupRouteForJob(job)
                    } else {
                        // Cache the job and retry once location becomes available
                        pendingRouteJob = job
                    }
                    startTracking()
                }
                .onFailure { _error.value = it.message ?: "Failed to load job" }
        }
    }

    private fun setupRouteForJob(job: DeliveryJob) {
        val driverLoc = _driverLocation.value ?: return

        // Determine destination based on job status
        val isPickupPhase = job.status.ordinal <= JobStatus.ARRIVED_AT_PICKUP.ordinal
        val destination = if (isPickupPhase) job.pickup else job.dropoff
        if (!isValidCoordinate(destination.latitude, destination.longitude)) {
            _routeError.value = "Missing destination coordinates"
            _markers.value = emptyList()
            _routeGeometry.value = null
            _etaMinutes.value = 0
            return
        }

        // Set markers: driver (blue) + destination (green for pickup, red for dropoff)
        val destColor = if (isPickupPhase) MarkerColor.GREEN else MarkerColor.RED
        val destLabel = if (isPickupPhase) "Pickup" else "Drop-off"

        _markers.value = listOf(
            MapMarker("driver", driverLoc.first, driverLoc.second, "You", MarkerColor.BLUE),
            MapMarker("destination", destination.latitude, destination.longitude, destLabel, destColor)
        )

        // Calculate route from driver to destination
        viewModelScope.launch {
            LocationApi.calculateRoute(
                driverLoc.first, driverLoc.second,
                destination.latitude, destination.longitude
            ).onSuccess { result ->
                _routeGeometry.value = result.geometry
                _etaMinutes.value = if (result.duration > 0) {
                    result.duration
                } else {
                    estimateMinutesFromDistance(result.distance)
                }
                _routeError.value = null
            }.onFailure {
                _routeError.value = it.message ?: "Could not calculate route"
                _etaMinutes.value = 0
            }
        }
    }

    /** Start polling GPS every 10 seconds to keep map updated */
    fun startTracking() {
        if (trackingJob?.isActive == true) return
        _isTracking.value = true
        trackingJob = viewModelScope.launch {
            while (isActive) {
                getLastKnownLocation()?.let { (lat, lng) ->
                    if (isValidCoordinate(lat, lng)) {
                        _driverLocation.value = Pair(lat, lng)
                    }
                    // If route hadn't been calculated yet (no GPS at loadJob time), retry now
                    pendingRouteJob?.let { job ->
                        pendingRouteJob = null
                        setupRouteForJob(job)
                    }
                }
                delay(10_000L)
            }
        }
    }

    fun stopTracking() {
        _isTracking.value = false
        trackingJob?.cancel()
        trackingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }

    private fun isValidCoordinate(lat: Double, lng: Double): Boolean {
        return lat in -90.0..90.0 && lng in -180.0..180.0 && (lat != 0.0 || lng != 0.0)
    }

    private fun estimateMinutesFromDistance(distanceKm: Double): Int {
        if (distanceKm <= 0.0) return 0
        // Fallback when provider omits ETA, assuming ~30 km/h average traffic speed.
        return kotlin.math.ceil((distanceKm / 30.0) * 60.0).toInt().coerceAtLeast(1)
    }
}
