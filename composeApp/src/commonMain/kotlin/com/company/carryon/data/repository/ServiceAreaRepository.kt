package com.company.carryon.data.repository

import com.company.carryon.data.model.ServiceArea
import com.company.carryon.data.network.LocationApi
import com.company.carryon.util.GeoUtils
import kotlinx.datetime.Clock

interface ServiceAreaRepository {
    suspend fun getServiceAreas(): List<ServiceArea>
    suspend fun isInServiceArea(lat: Double, lng: Double): Boolean
    fun clearCache()
}

class ServiceAreaRepositoryImpl : ServiceAreaRepository {
    private var cachedAreas: List<ServiceArea>? = null
    private var cacheExpiryMs: Long = 0
    private val cacheTtlMs = 60_000L

    override suspend fun getServiceAreas(): List<ServiceArea> {
        val now = Clock.System.now().toEpochMilliseconds()
        cachedAreas?.let { if (now < cacheExpiryMs) return it }

        val areas = LocationApi.getServiceAreas().getOrNull() ?: emptyList()
        cachedAreas = areas
        cacheExpiryMs = now + cacheTtlMs
        return areas
    }

    override suspend fun isInServiceArea(lat: Double, lng: Double): Boolean {
        return GeoUtils.isInServiceArea(lat, lng, getServiceAreas())
    }

    override fun clearCache() {
        cachedAreas = null
        cacheExpiryMs = 0
    }
}
