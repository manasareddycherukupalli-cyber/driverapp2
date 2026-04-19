package com.company.carryon.data.network

import com.company.carryon.data.model.ApiResponse
import com.company.carryon.data.model.BookingChatMessage
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
private data class SendBookingChatMessageRequest(
    val message: String,
    val imageUrl: String? = null
)

object BookingChatApi {
    private val client get() = HttpClientFactory.client

    suspend fun getMessages(bookingId: String): Result<ApiResponse<List<BookingChatMessage>>> = runCatching {
        client.get("/api/chat/$bookingId") {
            withAuth()
        }.body()
    }

    suspend fun sendMessage(
        bookingId: String,
        message: String,
        imageUrl: String? = null
    ): Result<ApiResponse<BookingChatMessage>> = runCatching {
        client.post("/api/chat/$bookingId") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(SendBookingChatMessageRequest(message, imageUrl))
        }.body()
    }

    suspend fun getQuickMessages(bookingId: String): Result<ApiResponse<List<String>>> = runCatching {
        client.get("/api/chat/$bookingId/quick-messages") {
            withAuth()
        }.body()
    }
}
