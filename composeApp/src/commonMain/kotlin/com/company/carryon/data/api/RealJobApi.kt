package com.company.carryon.data.api

import com.company.carryon.data.model.*
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.data.network.withAuth
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
private data class LifecycleCommandRequest(
    val command: String,
    val otp: String? = null,
    val forceResend: Boolean = false,
    val proof: DeliveryLifecycleProof? = null,
    val location: DeliveryLifecycleLocation? = null
)

class RealJobApi : JobApi {
    private val client = HttpClientFactory.client

    override suspend fun getActiveJobs(driverId: String): Result<List<DeliveryJob>> = runCatching {
        val response: ApiResponse<List<DeliveryJob>> = client.get("/api/driver/jobs/active") {
            withAuth()
        }.body()
        response.data ?: emptyList()
    }

    override suspend fun getScheduledJobs(driverId: String): Result<List<DeliveryJob>> = runCatching {
        val response: ApiResponse<List<DeliveryJob>> = client.get("/api/driver/jobs/scheduled") {
            withAuth()
        }.body()
        response.data ?: emptyList()
    }

    override suspend fun getCompletedJobs(driverId: String): Result<List<DeliveryJob>> = runCatching {
        val response: ApiResponse<List<DeliveryJob>> = client.get("/api/driver/jobs/completed") {
            withAuth()
        }.body()
        response.data ?: emptyList()
    }

    override suspend fun getJobDetails(jobId: String): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.get("/api/driver/jobs/$jobId") {
            withAuth()
        }.body()
        response.data ?: throw Exception("Job not found")
    }

    override suspend fun acceptJob(jobId: String, driverId: String): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.post("/api/driver/jobs/$jobId/accept") {
            withAuth()
        }.body()
        response.data ?: throw Exception("Failed to accept job")
    }

    override suspend fun rejectJob(jobId: String, driverId: String): Result<Boolean> = runCatching {
        client.post("/api/driver/jobs/$jobId/reject") {
            withAuth()
        }
        true
    }

    override suspend fun updateJobStatus(jobId: String, status: JobStatus): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.put("/api/driver/jobs/$jobId/status") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status.name))
        }.body()
        response.data ?: throw Exception("Failed to update status")
    }

    override suspend fun submitProofOfDelivery(jobId: String, proof: ProofOfDelivery): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.post("/api/driver/jobs/$jobId/proof") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(proof)
        }.body()
        response.data ?: throw Exception("Failed to submit proof")
    }

    override suspend fun getIncomingJobRequest(driverId: String): Result<DeliveryJob?> = runCatching {
        val response: ApiResponse<DeliveryJob?> = client.get("/api/driver/jobs/incoming") {
            withAuth()
        }.body()
        response.data
    }

    override suspend fun getIncomingJobRequests(driverId: String): Result<List<DeliveryJob>> = runCatching {
        try {
            val response: ApiResponse<List<DeliveryJob>> = client.get("/api/driver/jobs/incoming-list") {
                withAuth()
            }.body()
            response.data ?: emptyList()
        } catch (err: Exception) {
            // Backward compatibility: some deployed backends may not yet expose /incoming-list.
            if ((err.message ?: "").contains("Job not found", ignoreCase = true)) {
                val fallback: ApiResponse<DeliveryJob?> = client.get("/api/driver/jobs/incoming") {
                    withAuth()
                }.body()
                fallback.data?.let { listOf(it) } ?: emptyList()
            } else {
                throw err
            }
        }
    }

    override suspend fun verifyPickupOtp(jobId: String, otp: String): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.post("/api/driver/jobs/$jobId/verify-pickup-otp") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("otp" to otp))
        }.body()
        response.data ?: throw Exception("Failed to verify OTP")
    }

    override suspend fun requestDeliveryOtp(jobId: String, forceResend: Boolean): Result<DeliveryOtpInfo> = runCatching {
        val response: ApiResponse<DeliveryOtpInfo> = client.post("/api/driver/jobs/$jobId/request-delivery-otp") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("forceResend" to forceResend))
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to request delivery OTP")
    }

    override suspend fun executeLifecycleCommand(
        jobId: String,
        command: DeliveryLifecycleCommand,
        payload: DeliveryLifecycleCommandPayload
    ): Result<DeliveryLifecycleResult> = runCatching {
        val response: ApiResponse<DeliveryLifecycleResult> = client.post("/api/driver/jobs/$jobId/lifecycle-command") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(
                LifecycleCommandRequest(
                    command = command.name,
                    otp = payload.otp,
                    forceResend = payload.forceResend,
                    proof = payload.proof,
                    location = payload.location
                )
            )
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to execute lifecycle command")
    }

    override suspend fun cancelJob(jobId: String): Result<Boolean> = runCatching {
        client.post("/api/driver/jobs/$jobId/cancel") {
            withAuth()
        }
        true
    }

    override suspend fun getDemandZones(latitude: Double, longitude: Double, radiusKm: Double): Result<DemandZonesResponse> = runCatching {
        val response: ApiResponse<DemandZonesResponse> = client.get("/api/v1/driver/demand-zones") {
            withAuth()
            parameter("lat", latitude)
            parameter("lng", longitude)
            parameter("radiusKm", radiusKm)
        }.body()
        response.data ?: DemandZonesResponse(
            centerLatitude = latitude,
            centerLongitude = longitude,
            radiusKm = radiusKm
        )
    }
}
