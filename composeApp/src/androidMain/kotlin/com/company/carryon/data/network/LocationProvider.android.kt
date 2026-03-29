package com.company.carryon.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper

private var appContext: Context? = null
private var latestLocation: Location? = null

actual fun initLocationProvider(context: Any?) {
    appContext = (context as? Context)?.applicationContext
    // Start requesting location updates so we always have a fresh fix
    startLocationUpdates()
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates() {
    val ctx = appContext ?: return
    try {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                latestLocation = location
            }
            @Deprecated("Deprecated in API")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Request from GPS
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L, // every 5 seconds
                5f,    // or 5 meters
                listener,
                Looper.getMainLooper()
            )
        }

        // Also request from network as fallback
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000L,
                5f,
                listener,
                Looper.getMainLooper()
            )
        }

        // Seed with last known location if available
        val lastKnown = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (lastKnown != null) {
            latestLocation = lastKnown
        }
    } catch (_: Exception) {}
}

@SuppressLint("MissingPermission")
actual fun getLastKnownLocation(): Pair<Double, Double>? {
    val location = latestLocation
    if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
        return Pair(location.latitude, location.longitude)
    }

    // Fallback: try reading cached location directly
    val ctx = appContext ?: return null
    return try {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (loc != null && (loc.latitude != 0.0 || loc.longitude != 0.0)) {
            latestLocation = loc
            Pair(loc.latitude, loc.longitude)
        } else null
    } catch (_: Exception) { null }
}
