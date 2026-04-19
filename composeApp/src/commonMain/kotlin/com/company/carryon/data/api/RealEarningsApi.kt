package com.company.carryon.data.api

import com.company.carryon.data.model.*
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.data.network.withAuth
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RealEarningsApi : EarningsApi {
    private val client = HttpClientFactory.client

    override suspend fun getEarningsSummary(driverId: String): Result<EarningsSummary> = runCatching {
        val response: ApiResponse<EarningsSummary> = client.get("/api/driver/earnings/summary") {
            withAuth()
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to load earnings summary")
    }

    override suspend fun getTransactionHistory(driverId: String): Result<List<Transaction>> = runCatching {
        val response: ApiResponse<List<Transaction>> = client.get("/api/driver/earnings/transactions") {
            withAuth()
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to load transactions")
    }

    override suspend fun getWalletInfo(driverId: String): Result<WalletInfo> = runCatching {
        val response: ApiResponse<WalletInfo> = client.get("/api/driver/earnings/wallet") {
            withAuth()
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to load wallet")
    }

    override suspend fun requestWithdrawal(driverId: String, amount: Double): Result<Transaction> = runCatching {
        val response: ApiResponse<Transaction> = client.post("/api/driver/earnings/wallet/withdraw") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("amount" to amount))
        }.body()
        response.data ?: throw Exception("Withdrawal failed")
    }
}
