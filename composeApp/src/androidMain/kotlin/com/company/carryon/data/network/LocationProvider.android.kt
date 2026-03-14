package com.company.carryon.data.network

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager

private var appContext: Context? = null

actual fun initLocationProvider(context: Any?) {
    appContext = (context as? Context)?.applicationContext
}

@SuppressLint("MissingPermission")
actual fun getLastKnownLocation(): Pair<Double, Double>? {
    val ctx = appContext ?: return null
    return try {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Try GPS first, then network
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
            Pair(location.latitude, location.longitude)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
