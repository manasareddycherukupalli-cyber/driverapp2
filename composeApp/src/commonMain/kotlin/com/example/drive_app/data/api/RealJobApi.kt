package com.example.drive_app.data.api

import com.example.drive_app.data.model.*
import com.example.drive_app.data.network.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RealJobApi : JobApi {
    private val client = HttpClientFactory.client

    override suspend fun getActiveJobs(driverId: String): Result<List<DeliveryJob>> = runCatching {
        val response: ApiResponse<List<DeliveryJob>> = client.get("/api/driver/jobs/active").body()
        response.data ?: emptyList()
    }

    override suspend fun getScheduledJobs(driverId: String): Result<List<DeliveryJob>> = runCatching {
        val response: ApiResponse<List<DeliveryJob>> = client.get("/api/driver/jobs/scheduled").body()
        response.data ?: emptyList()
    }

    override suspend fun getCompletedJobs(driverId: String): Result<List<DeliveryJob>> = runCatching {
        val response: ApiResponse<List<DeliveryJob>> = client.get("/api/driver/jobs/completed").body()
        response.data ?: emptyList()
    }

    override suspend fun getJobDetails(jobId: String): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.get("/api/driver/jobs/$jobId").body()
        response.data ?: throw Exception("Job not found")
    }

    override suspend fun acceptJob(jobId: String, driverId: String): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.post("/api/driver/jobs/$jobId/accept").body()
        response.data ?: throw Exception("Failed to accept job")
    }

    override suspend fun rejectJob(jobId: String, driverId: String): Result<Boolean> = runCatching {
        client.post("/api/driver/jobs/$jobId/reject")
        true
    }

    override suspend fun updateJobStatus(jobId: String, status: JobStatus): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.put("/api/driver/jobs/$jobId/status") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status.name))
        }.body()
        response.data ?: throw Exception("Failed to update status")
    }

    override suspend fun submitProofOfDelivery(jobId: String, proof: ProofOfDelivery): Result<DeliveryJob> = runCatching {
        val response: ApiResponse<DeliveryJob> = client.post("/api/driver/jobs/$jobId/proof") {
            contentType(ContentType.Application.Json)
            setBody(proof)
        }.body()
        response.data ?: throw Exception("Failed to submit proof")
    }

    override suspend fun getIncomingJobRequest(driverId: String): Result<DeliveryJob?> = runCatching {
        val response: ApiResponse<DeliveryJob?> = client.get("/api/driver/jobs/incoming").body()
        response.data
    }
}
