package com.company.carryon.data.api

import com.company.carryon.data.model.*
import com.company.carryon.data.network.HttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*

class RealNotificationsApi : NotificationsApi {
    private val client = HttpClientFactory.client

    override suspend fun getNotifications(driverId: String): Result<List<AppNotification>> = runCatching {
        val response: ApiResponse<List<AppNotification>> = client.get("/api/driver/notifications").body()
        response.data ?: emptyList()
    }

    override suspend fun markAsRead(notificationId: String): Result<Boolean> = runCatching {
        client.put("/api/driver/notifications/$notificationId/read")
        true
    }
}
