package com.company.carryon.data.api

import com.company.carryon.data.model.*
import com.company.carryon.data.network.AuthSessionManager
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.data.network.currentPushPlatform
import com.company.carryon.data.network.getOrCreateDeviceId
import com.company.carryon.i18n.currentLanguageOrDefault
import com.company.carryon.data.network.withAuth
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RealAuthApi : AuthApi {
    private val client = HttpClientFactory.client

    override suspend fun sendOtp(request: OtpRequest): Result<Boolean> = runCatching {
        client.post("/api/driver/auth/send-otp") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        true
    }

    override suspend fun verifyOtp(request: OtpVerifyRequest): Result<AuthResponse> = runCatching {
        val endpoint = if (request.email.isBlank() && request.otp.isBlank()) {
            "/api/driver/auth/sync"
        } else {
            "/api/driver/auth/verify-otp"
        }
        val response: AuthResponse = client.post(endpoint) {
            if (endpoint.endsWith("/sync")) withAuth()
            contentType(ContentType.Application.Json)
            if (endpoint.endsWith("/sync")) {
                setBody(SyncRequest(language = currentLanguageOrDefault()))
            } else {
                setBody(request.copy(language = request.language.ifBlank { currentLanguageOrDefault() }))
            }
        }.body()
        response.token?.takeIf { it.isNotBlank() }?.let { AuthSessionManager.storeAccessToken(it) }
        response
    }

    override suspend fun registerDriver(driver: Driver): Result<AuthResponse> = runCatching {
        val response: AuthResponse = client.post("/api/driver/auth/register") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(
                name = driver.name,
                phone = driver.phone,
                emergencyContact = driver.emergencyContact,
                language = driver.preferredLanguage.ifBlank { currentLanguageOrDefault() }
            ))
        }.body()
        response
    }

    override suspend fun uploadDocument(driverId: String, document: Document): Result<Document> = runCatching {
        println("[RealAuthApi] Uploading document: type=${document.type}")
        val response: ApiResponse<Document> = client.post("/api/driver/documents") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(document)
        }.body()
        println("[RealAuthApi] Upload response: success=${response.success}")
        response.data ?: throw Exception(response.message ?: "Upload failed - no data returned")
    }

    override suspend fun updateVehicleDetails(driverId: String, vehicle: VehicleDetails): Result<VehicleDetails> = runCatching {
        val response: ApiResponse<VehicleDetails> = client.post("/api/driver/vehicle") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(vehicle)
        }.body()
        response.data ?: throw Exception("Failed to update vehicle")
    }

    override suspend fun getVerificationStatus(driverId: String): Result<Driver> = runCatching {
        val response: ApiResponse<Driver> = client.get("/api/driver/profile") {
            withAuth()
        }.body()
        response.data ?: throw Exception("Failed to get profile")
    }

    override suspend fun getDriverProfile(driverId: String): Result<Driver> = runCatching {
        val response: ApiResponse<Driver> = client.get("/api/driver/profile") {
            withAuth()
        }.body()
        response.data ?: throw Exception("Failed to get profile")
    }

    override suspend fun updateDriverProfile(driver: Driver): Result<Driver> = runCatching {
        val response: ApiResponse<Driver> = client.put("/api/driver/profile") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(driver)
        }.body()
        response.data ?: throw Exception("Failed to update profile")
    }

    override suspend fun toggleOnlineStatus(driverId: String, isOnline: Boolean, location: Pair<Double, Double>?): Result<Boolean> = runCatching {
        val response: ApiResponse<Driver> = client.post("/api/driver/profile/toggle-online") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(
                ToggleOnlineRequest(
                    isOnline = isOnline,
                    latitude = location?.first,
                    longitude = location?.second
                )
            )
        }.body()
        if (!response.success) throw Exception(response.message ?: "Toggle failed")
        response.data?.isOnline ?: throw Exception("Missing isOnline in response")
    }

    override suspend fun updateFcmToken(driverId: String, fcmToken: String): Result<Boolean> = runCatching {
        client.put("/api/driver/profile/push-token") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "token" to fcmToken,
                    "platform" to currentPushPlatform(),
                    "deviceId" to getOrCreateDeviceId()
                )
            )
        }
        true
    }

    override suspend fun deletePushToken(driverId: String): Result<Boolean> = runCatching {
        client.delete("/api/driver/profile/push-token") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("deviceId" to getOrCreateDeviceId()))
        }
        true
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double): Result<Boolean> = runCatching {
        client.post("/api/driver/profile/location") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("latitude" to latitude, "longitude" to longitude))
        }
        true
    }
}
