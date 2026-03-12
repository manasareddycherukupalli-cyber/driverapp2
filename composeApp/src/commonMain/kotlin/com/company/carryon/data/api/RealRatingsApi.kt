package com.company.carryon.data.api

import com.company.carryon.data.model.*
import com.company.carryon.data.network.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RealRatingsApi : RatingsApi {
    private val client = HttpClientFactory.client

    override suspend fun getRatingInfo(driverId: String): Result<RatingInfo> = runCatching {
        val response: ApiResponse<RatingInfo> = client.get("/api/driver/ratings").body()
        response.data ?: RatingInfo()
    }

    override suspend fun submitCustomerRating(rating: CustomerRating): Result<Boolean> = runCatching {
        client.post("/api/driver/ratings/${rating.jobId}") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("rating" to rating.rating, "comment" to rating.comment))
        }
        true
    }
}
