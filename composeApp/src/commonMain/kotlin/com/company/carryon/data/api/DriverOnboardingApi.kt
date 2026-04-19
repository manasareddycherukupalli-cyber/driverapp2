package com.company.carryon.data.api

import com.company.carryon.data.model.ApiResponse
import com.company.carryon.data.model.Document
import com.company.carryon.data.model.Driver
import com.company.carryon.data.model.DriverDocumentSubmissionRequest
import com.company.carryon.data.model.DriverProfileUpdateRequest
import com.company.carryon.data.model.DriverVerificationStatusPayload
import com.company.carryon.data.model.DriverVehicleUpsertRequest
import com.company.carryon.data.model.VehicleDetails
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.data.network.withAuth
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class DriverOnboardingApi {
    private val client = HttpClientFactory.client

    suspend fun getProfile(): Result<Driver> = runCatching {
        val response: ApiResponse<Driver> = client.get("/api/driver/profile") {
            withAuth()
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to load driver profile")
    }

    suspend fun getVehicle(): Result<VehicleDetails?> = runCatching {
        val response: ApiResponse<VehicleDetails?> = client.get("/api/driver/vehicle") {
            withAuth()
        }.body()
        response.data
    }

    suspend fun getVerificationStatus(): Result<DriverVerificationStatusPayload> = runCatching {
        val response: ApiResponse<DriverVerificationStatusPayload> = client.get("/api/driver/profile/verification-status") {
            withAuth()
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to load verification status")
    }

    suspend fun updateProfile(request: DriverProfileUpdateRequest): Result<Driver> = runCatching {
        val response: ApiResponse<Driver> = client.put("/api/driver/profile") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to update driver profile")
    }

    suspend fun updateVehicle(request: DriverVehicleUpsertRequest): Result<VehicleDetails> = runCatching {
        val response: ApiResponse<VehicleDetails> = client.post("/api/driver/vehicle") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to update vehicle")
    }

    suspend fun submitDocument(request: DriverDocumentSubmissionRequest): Result<Document> = runCatching {
        val response: ApiResponse<Document> = client.post("/api/driver/documents") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to submit document")
    }
}
