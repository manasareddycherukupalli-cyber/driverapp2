package com.company.carryon.data.network

/**
 * Platform-specific location provider.
 * Android: Uses LocationManager to get GPS coordinates.
 * iOS: Stub (returns null).
 */
expect fun initLocationProvider(context: Any? = null)
expect fun getLastKnownLocation(): Pair<Double, Double>?
