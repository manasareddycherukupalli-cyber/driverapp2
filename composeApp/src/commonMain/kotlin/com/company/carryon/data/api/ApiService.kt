package com.company.carryon.data.api

import com.company.carryon.data.model.*

/**
 * Auth API service interface.
 * Handles OTP-based authentication and driver registration.
 */
interface AuthApi {
    suspend fun sendOtp(request: OtpRequest): Result<Boolean>
    suspend fun verifyOtp(request: OtpVerifyRequest): Result<AuthResponse>
    suspend fun registerDriver(driver: Driver): Result<AuthResponse>
    suspend fun uploadDocument(driverId: String, document: Document): Result<Document>
    suspend fun updateVehicleDetails(driverId: String, vehicle: VehicleDetails): Result<VehicleDetails>
    suspend fun getVerificationStatus(driverId: String): Result<Driver>
    suspend fun getDriverProfile(driverId: String): Result<Driver>
    suspend fun updateDriverProfile(driver: Driver): Result<Driver>
    suspend fun toggleOnlineStatus(driverId: String, isOnline: Boolean): Result<Boolean>
    suspend fun updateFcmToken(driverId: String, fcmToken: String): Result<Boolean>
}

/**
 * Job API service interface.
 * Handles all delivery job operations.
 */
interface JobApi {
    suspend fun getActiveJobs(driverId: String): Result<List<DeliveryJob>>
    suspend fun getScheduledJobs(driverId: String): Result<List<DeliveryJob>>
    suspend fun getCompletedJobs(driverId: String): Result<List<DeliveryJob>>
    suspend fun getJobDetails(jobId: String): Result<DeliveryJob>
    suspend fun acceptJob(jobId: String, driverId: String): Result<DeliveryJob>
    suspend fun rejectJob(jobId: String, driverId: String): Result<Boolean>
    suspend fun updateJobStatus(jobId: String, status: JobStatus): Result<DeliveryJob>
    suspend fun submitProofOfDelivery(jobId: String, proof: ProofOfDelivery): Result<DeliveryJob>
    suspend fun getIncomingJobRequest(driverId: String): Result<DeliveryJob?>
    suspend fun verifyPickupOtp(jobId: String, otp: String): Result<DeliveryJob>
}

/**
 * Earnings API service interface.
 * Handles earnings data and wallet operations.
 */
interface EarningsApi {
    suspend fun getEarningsSummary(driverId: String): Result<EarningsSummary>
    suspend fun getTransactionHistory(driverId: String): Result<List<Transaction>>
    suspend fun getWalletInfo(driverId: String): Result<WalletInfo>
    suspend fun requestWithdrawal(driverId: String, amount: Double): Result<Transaction>
}

/**
 * Ratings API service interface.
 */
interface RatingsApi {
    suspend fun getRatingInfo(driverId: String): Result<RatingInfo>
    suspend fun submitCustomerRating(rating: CustomerRating): Result<Boolean>
}

/**
 * Support API service interface.
 * Handles help center, tickets, and chat.
 */
interface SupportApi {
    suspend fun getHelpArticles(): Result<List<HelpArticle>>
    suspend fun getTickets(driverId: String): Result<List<SupportTicket>>
    suspend fun createTicket(ticket: SupportTicket): Result<SupportTicket>
    suspend fun getTicketMessages(ticketId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(ticketId: String, message: ChatMessage): Result<ChatMessage>
    suspend fun triggerSos(driverId: String, latitude: Double, longitude: Double): Result<Boolean>
}

/**
 * Notifications API service interface.
 */
interface NotificationsApi {
    suspend fun getNotifications(driverId: String): Result<List<AppNotification>>
    suspend fun markAsRead(notificationId: String): Result<Boolean>
}
