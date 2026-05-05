package com.company.carryon.data.network

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.Manifest
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat

private var appContext: Context? = null
private var currentActivity: Activity? = null
private var latestLocation: Location? = null
private var pendingPermissionCallback: ((Boolean) -> Unit)? = null

const val LOCATION_PERMISSION_REQUEST_CODE = 4801

actual fun initLocationProvider(context: Any?) {
    (context as? Activity)?.let { currentActivity = it }
    appContext = (context as? Context)?.applicationContext ?: appContext
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

actual fun getLastKnownHeading(): Float = latestLocation?.bearing ?: 0f

actual fun hasLocationPermission(): Boolean {
    val ctx = appContext ?: return false
    val fine = ContextCompat.checkSelfPermission(
        ctx,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        ctx,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

actual fun requestLocationPermission(onResult: (Boolean) -> Unit) {
    if (hasLocationPermission()) {
        startLocationUpdates()
        onResult(true)
        return
    }

    val activity = currentActivity
    if (activity == null) {
        onResult(false)
        return
    }

    pendingPermissionCallback = onResult
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        LOCATION_PERMISSION_REQUEST_CODE
    )
}

fun handleLocationPermissionResult(requestCode: Int, grantResults: IntArray): Boolean {
    if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return false
    val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
    if (granted) startLocationUpdates()
    pendingPermissionCallback?.invoke(granted)
    pendingPermissionCallback = null
    return true
}

actual fun openAppSettings() {
    val ctx = currentActivity ?: appContext ?: return
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", ctx.packageName, null)
    ).apply {
        if (ctx !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    ctx.startActivity(intent)
}
