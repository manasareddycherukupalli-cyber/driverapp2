package com.company.carryon.util

import com.company.carryon.data.model.ServiceArea
import kotlin.math.*

object GeoUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return EARTH_RADIUS_KM * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun isInServiceArea(lat: Double, lng: Double, areas: List<ServiceArea>): Boolean {
        if (areas.isEmpty()) return true
        return areas.any { haversineKm(lat, lng, it.latitude, it.longitude) <= it.radiusKm }
    }
}
