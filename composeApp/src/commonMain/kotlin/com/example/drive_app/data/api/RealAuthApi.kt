package com.example.drive_app.data.api

import com.example.drive_app.data.model.*
import com.example.drive_app.data.network.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RealAuthApi : AuthApi {
    private val client = HttpClientFactory.client

    override suspend fun sendOtp(request: OtpRequest): Result<Boolean> = runCatching {
        // OTP is sent directly via Supabase client-side — this is a no-op
        true
    }

    override suspend fun verifyOtp(request: OtpVerifyRequest): Result<AuthResponse> = runCatching {
        // OTP verification happens via Supabase client-side
        // After that, we call sync to create/find driver in our backend
        val response: AuthResponse = client.post("/api/driver/auth/sync") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest())
        }.body()
        response
    }

    override suspend fun registerDriver(driver: Driver): Result<AuthResponse> = runCatching {
        val response: AuthResponse = client.post("/api/driver/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(
                name = driver.name,
                phone = driver.phone,
                emergencyContact = driver.emergencyContact
            ))
        }.body()
        response
    }

    override suspend fun uploadDocument(driverId: String, document: Document): Result<Document> = runCatching {
        val response: ApiResponse<Document> = client.post("/api/driver/documents") {
            contentType(ContentType.Application.Json)
            setBody(document)
        }.body()
        response.data ?: throw Exception("Upload failed")
    }

    override suspend fun updateVehicleDetails(driverId: String, vehicle: VehicleDetails): Result<VehicleDetails> = runCatching {
        val response: ApiResponse<VehicleDetails> = client.post("/api/driver/vehicle") {
            contentType(ContentType.Application.Json)
            setBody(vehicle)
        }.body()
        response.data ?: throw Exception("Failed to update vehicle")
    }

    override suspend fun getVerificationStatus(driverId: String): Result<Driver> = runCatching {
        val response: ApiResponse<Driver> = client.get("/api/driver/profile").body()
        response.data ?: throw Exception("Failed to get profile")
    }

    override suspend fun getDriverProfile(driverId: String): Result<Driver> = runCatching {
        val response: ApiResponse<Driver> = client.get("/api/driver/profile").body()
        response.data ?: throw Exception("Failed to get profile")
    }

    override suspend fun updateDriverProfile(driver: Driver): Result<Driver> = runCatching {
        val response: ApiResponse<Driver> = client.put("/api/driver/profile") {
            contentType(ContentType.Application.Json)
            setBody(driver)
        }.body()
        response.data ?: throw Exception("Failed to update profile")
    }

    override suspend fun toggleOnlineStatus(driverId: String, isOnline: Boolean): Result<Boolean> = runCatching {
        client.post("/api/driver/profile/toggle-online") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("isOnline" to isOnline))
        }
        true
    }

    override suspend fun updateFcmToken(driverId: String, fcmToken: String): Result<Boolean> = runCatching {
        client.put("/api/driver/profile/fcm-token") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("fcmToken" to fcmToken))
        }
        true
    }
}
