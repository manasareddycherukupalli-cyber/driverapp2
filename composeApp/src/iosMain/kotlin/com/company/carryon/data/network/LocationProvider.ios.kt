package com.company.carryon.data.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.darwin.NSObject

private val locationManager = CLLocationManager()
private val locationDelegate = LocationDelegate()
private var delegateAttached = false

actual fun initLocationProvider(context: Any?) {
    if (!delegateAttached) {
        locationManager.delegate = locationDelegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        delegateAttached = true
    }
    // Request permission if not yet determined
    val status = CLLocationManager.authorizationStatus()
    if (status == kCLAuthorizationStatusNotDetermined) {
        locationManager.requestWhenInUseAuthorization()
    }
    // Start updating so we get a location
    val authorized = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
    if (authorized) {
        locationManager.startUpdatingLocation()
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun getLastKnownLocation(): Pair<Double, Double>? {
    val location = locationManager.location ?: return null
    return location.coordinate.useContents {
        if (latitude != 0.0 || longitude != 0.0) {
            Pair(latitude, longitude)
        } else {
            null
        }
    }
}

private class LocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = CLLocationManager.authorizationStatus()
        val authorized = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
        if (authorized) {
            manager.startUpdatingLocation()
        }
    }
}
