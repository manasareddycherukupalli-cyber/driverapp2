package com.company.carryon.data.api

import com.company.carryon.data.model.*
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.data.network.withAuth
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

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

    override suspend fun verifyPickupOtp(jobId: String, otp: String): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.post("/api/driver/jobs/$jobId/verify-pickup-otp") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("otp" to otp))
        }.body()
        response.data ?: throw Exception("Failed to verify OTP")
    }
}
