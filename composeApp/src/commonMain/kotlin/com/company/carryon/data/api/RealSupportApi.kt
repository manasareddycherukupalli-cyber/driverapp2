package com.company.carryon.data.api

import com.company.carryon.data.model.*
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.data.network.withAuth
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RealSupportApi : SupportApi {
    private val client = HttpClientFactory.client

    override suspend fun getHelpArticles(): Result<List<HelpArticle>> = runCatching {
        val response: ApiResponse<List<HelpArticle>> = client.get("/api/driver/support/articles") {
            withAuth()
        }.body()
        response.data ?: emptyList()
    }

    override suspend fun getTickets(driverId: String): Result<List<SupportTicket>> = runCatching {
        val response: ApiResponse<List<SupportTicket>> = client.get("/api/driver/support/tickets") {
            withAuth()
        }.body()
        response.data ?: emptyList()
    }

    override suspend fun createTicket(ticket: SupportTicket): Result<SupportTicket> = runCatching {
        val response: ApiResponse<SupportTicket> = client.post("/api/driver/support/tickets") {
            withAuth()
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
        val response: ApiResponse<SupportTicket> = client.get("/api/driver/support/tickets/$ticketId") {
            withAuth()
        }.body()
        response.data?.messages ?: emptyList()
    }

    override suspend fun sendMessage(ticketId: String, message: ChatMessage): Result<ChatMessage> = runCatching {
        val response: ApiResponse<ChatMessage> = client.post("/api/driver/support/tickets/$ticketId/reply") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("message" to message.message))
        }.body()
        response.data ?: throw Exception("Failed to send message")
    }

    override suspend fun triggerSos(driverId: String, latitude: Double, longitude: Double): Result<SosResult> = runCatching {
        val response: ApiResponse<SosResult> = client.post("/api/driver/support/sos") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("latitude" to latitude, "longitude" to longitude))
        }.body()
        response.data ?: SosResult()
    }
}
