package com.company.carryon.data.network

actual fun initLocationProvider(context: Any?) {
    // No-op on iOS — location handled natively
}

actual fun getLastKnownLocation(): Pair<Double, Double>? {
    // iOS stub — native CLLocationManager integration needed
    return null
}
