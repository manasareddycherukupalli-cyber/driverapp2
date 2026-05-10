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
        val response: ApiResponse<Transaction> = client.post("/api/driver/payouts/withdraw") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(mapOf("amount" to amount))
        }.body()
        response.data ?: throw Exception("Withdrawal failed")
    }

    override suspend fun getPayoutStatus(): Result<PayoutStatus> = runCatching {
        val response: ApiResponse<PayoutStatus> = client.get("/api/driver/payouts/status") {
            withAuth()
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to load payout status")
    }

    override suspend fun createPayoutOnboardingLink(): Result<PayoutOnboardingLink> = runCatching {
        val response: ApiResponse<PayoutOnboardingLink> = client.post("/api/driver/payouts/onboarding-link") {
            withAuth()
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to start payout setup")
    }

    override suspend fun getInvoiceUrl(transactionId: String): Result<InvoiceLink> = runCatching {
        val response: ApiResponse<InvoiceLink> = client.get("/api/driver/earnings/transactions/$transactionId/invoice") {
            withAuth()
        }.body()
        response.data ?: throw Exception(response.message ?: "Failed to get invoice")
    }
}
