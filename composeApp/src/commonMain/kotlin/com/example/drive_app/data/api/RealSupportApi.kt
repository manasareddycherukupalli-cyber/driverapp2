package com.example.drive_app.data.api

import com.example.drive_app.data.model.*
import com.example.drive_app.data.network.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RealSupportApi : SupportApi {
    private val client = HttpClientFactory.client

    override suspend fun getHelpArticles(): Result<List<HelpArticle>> = runCatching {
        val response: ApiResponse<List<HelpArticle>> = client.get("/api/driver/support/articles").body()
        response.data ?: emptyList()
    }

    override suspend fun getTickets(driverId: String): Result<List<SupportTicket>> = runCatching {
        val response: ApiResponse<List<SupportTicket>> = client.get("/api/driver/support/tickets").body()
        response.data ?: emptyList()
    }

    override suspend fun createTicket(ticket: SupportTicket): Result<SupportTicket> = runCatching {
        val response: ApiResponse<SupportTicket> = client.post("/api/driver/support/tickets") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "subject" to ticket.subject,
                "category" to ticket.category.name,
                "description" to ticket.description
            ))
        }.body()
        response.data ?: throw Exception("Failed to create ticket")
    }

    override suspend fun getTicketMessages(ticketId: String): Result<List<ChatMessage>> = runCatching {
        val response: ApiResponse<SupportTicket> = client.get("/api/driver/support/tickets/$ticketId").body()
        response.data?.messages ?: emptyList()
    }

    override suspend fun sendMessage(ticketId: String, message: ChatMessage): Result<ChatMessage> = runCatching {
        val response: ApiResponse<ChatMessage> = client.post("/api/driver/support/tickets/$ticketId/reply") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("message" to message.message))
        }.body()
        response.data ?: throw Exception("Failed to send message")
    }

    override suspend fun triggerSos(driverId: String, latitude: Double, longitude: Double): Result<Boolean> = runCatching {
        client.post("/api/driver/support/sos") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("latitude" to latitude, "longitude" to longitude))
        }
        true
    }
}
