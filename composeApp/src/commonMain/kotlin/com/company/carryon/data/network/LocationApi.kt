package com.company.carryon.data.network

import com.company.carryon.data.model.ApiResponse
import com.company.carryon.data.model.CalculateRouteRequest
import com.company.carryon.data.model.MapConfig
import com.company.carryon.data.model.RouteResult
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

object LocationApi {
    private val client get() = HttpClientFactory.client

    suspend fun getMapConfig(): Result<MapConfig> = runCatching {
        val response = client.get("/api/location/map-config") {
            withAuth()
        }.body<ApiResponse<MapConfig>>()
        response.data ?: MapConfig()
    }

    suspend fun calculateRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Result<RouteResult> = runCatching {
        val response = client.post("/api/location/calculate-route") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(CalculateRouteRequest(originLat, originLng, destLat, destLng))
        }.body<ApiResponse<RouteResult>>()
        response.data ?: RouteResult()
    }

    suspend fun updatePosition(deviceId: String, latitude: Double, longitude: Double): Result<Unit> = runCatching {
        client.post("/api/location/update-position") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("deviceId" to deviceId, "latitude" to latitude, "longitude" to longitude))
        }
        Unit
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): Result<String> = runCatching {
        val response = client.get("/api/location/reverse-geocode") {
            withAuth()
            parameter("lat", lat)
            parameter("lng", lng)
        }.body<ApiResponse<Map<String, String>>>()
        response.data?.get("address") ?: ""
    }
}
