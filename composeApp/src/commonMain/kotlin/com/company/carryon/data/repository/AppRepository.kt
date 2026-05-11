package com.company.carryon.data.repository

import com.company.carryon.data.api.*
import com.company.carryon.data.model.*
import com.company.carryon.data.network.AuthSessionManager
import com.company.carryon.data.network.clearPushToken
import com.company.carryon.data.network.SupabaseConfig
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// ============================================================
// AUTH REPOSITORY
// ============================================================

interface AuthRepository {
    val currentDriver: Flow<Driver?>
    val isLoggedIn: Flow<Boolean>
    suspend fun syncDriver(): Result<AuthResponse>
    suspend fun register(driver: Driver): Result<AuthResponse>
    suspend fun uploadDocument(document: Document): Result<Document>
    suspend fun updateVehicle(vehicle: VehicleDetails): Result<VehicleDetails>
    suspend fun getVerificationStatus(): Result<Driver>
    suspend fun toggleOnline(isOnline: Boolean): Result<Boolean>
    suspend fun updateFcmToken(fcmToken: String): Result<Boolean>
    suspend fun updateProfile(driver: Driver): Result<Driver>
    suspend fun updateLocation(latitude: Double, longitude: Double): Result<Boolean>
    suspend fun logout()
    fun checkExistingSession()
}

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {
    private val _currentDriver = MutableStateFlow<Driver?>(null)
    override val currentDriver: Flow<Driver?> = _currentDriver.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    override fun checkExistingSession() {
        val token = AuthSessionManager.currentAccessToken()
        if (token != null) {
            _isLoggedIn.value = true
        }
    }

    override suspend fun syncDriver(): Result<AuthResponse> {
        val result = api.verifyOtp(OtpVerifyRequest("", "")) // calls /sync
        result.getOrNull()?.let { response ->
            if (response.success) {
                _currentDriver.value = response.driver
                _isLoggedIn.value = true
            }
        }
        return result
    }

    override suspend fun register(driver: Driver): Result<AuthResponse> {
        val result = api.registerDriver(driver)
        result.getOrNull()?.let { response ->
            if (response.success) {
                _currentDriver.value = response.driver
                _isLoggedIn.value = true
            }
        }
        return result
    }

    override suspend fun uploadDocument(document: Document): Result<Document> {
        // Note: driverId is extracted from JWT token on the backend, so we don't need it here
        val result = api.uploadDocument("", document)
        result.getOrNull()?.let { uploadedDoc ->
            _currentDriver.value = _currentDriver.value?.let { driver ->
                val updatedDocs = driver.documents.toMutableList()
                val existingIndex = updatedDocs.indexOfFirst { it.type == uploadedDoc.type }
                if (existingIndex >= 0) updatedDocs[existingIndex] = uploadedDoc
                else updatedDocs.add(uploadedDoc)
                driver.copy(documents = updatedDocs)
            }
        }
        return result
    }

    override suspend fun updateVehicle(vehicle: VehicleDetails): Result<VehicleDetails> {
        val driverId = _currentDriver.value?.id ?: return Result.failure(Exception("Not logged in"))
        val result = api.updateVehicleDetails(driverId, vehicle)
        result.getOrNull()?.let { updatedVehicle ->
            _currentDriver.value = _currentDriver.value?.copy(vehicleDetails = updatedVehicle)
        }
        return result
    }

    override suspend fun getVerificationStatus(): Result<Driver> {
        val driverId = _currentDriver.value?.id ?: return Result.failure(Exception("Not logged in"))
        val result = api.getVerificationStatus(driverId)
        result.getOrNull()?.let { driver ->
            _currentDriver.value = driver
        }
        return result
    }

    override suspend fun toggleOnline(isOnline: Boolean): Result<Boolean> {
        val driverId = _currentDriver.value?.id ?: return Result.failure(Exception("Not logged in"))
        val result = api.toggleOnlineStatus(driverId, isOnline)
        result.getOrNull()?.let {
            _currentDriver.value = _currentDriver.value?.copy(isOnline = isOnline)
        }
        return result
    }

    override suspend fun updateFcmToken(fcmToken: String): Result<Boolean> {
        val driverId = _currentDriver.value?.id ?: return Result.failure(Exception("Not logged in"))
        return api.updateFcmToken(driverId, fcmToken)
    }

    override suspend fun updateProfile(driver: Driver): Result<Driver> {
        val result = api.updateDriverProfile(driver)
        result.getOrNull()?.let { updated ->
            _currentDriver.value = updated
        }
        return result
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double): Result<Boolean> {
        return api.updateLocation(latitude, longitude)
    }

    override suspend fun logout() {
        val driverId = _currentDriver.value?.id
        if (driverId != null) {
            // Best-effort token de-registration before local auth state is cleared.
            runCatching { api.deletePushToken(driverId) }
        }
        clearPushToken()
        AuthSessionManager.clearAccessToken()
        try {
            SupabaseConfig.client.auth.signOut()
        } catch (_: Exception) {
            // Best-effort: clear local state even if sign-out request fails
        }
        _currentDriver.value = null
        _isLoggedIn.value = false
    }
}

// ============================================================
// JOB REPOSITORY
// ============================================================

interface JobRepository {
    suspend fun getActiveJobs(): Result<List<DeliveryJob>>
    suspend fun getScheduledJobs(): Result<List<DeliveryJob>>
    suspend fun getCompletedJobs(): Result<List<DeliveryJob>>
    suspend fun getJobDetails(jobId: String): Result<DeliveryJob>
    suspend fun acceptJob(jobId: String): Result<DeliveryJob>
    suspend fun rejectJob(jobId: String): Result<Boolean>
    suspend fun updateJobStatus(jobId: String, status: JobStatus): Result<DeliveryJob>
    suspend fun submitProof(jobId: String, proof: ProofOfDelivery): Result<DeliveryJob>
    suspend fun getIncomingRequest(): Result<DeliveryJob?>
    suspend fun getIncomingRequests(): Result<List<DeliveryJob>>
    suspend fun verifyPickupOtp(jobId: String, otp: String): Result<DeliveryJob>
    suspend fun requestDeliveryOtp(jobId: String, forceResend: Boolean = false): Result<DeliveryOtpInfo>
    suspend fun executeLifecycleCommand(
        jobId: String,
        command: DeliveryLifecycleCommand,
        payload: DeliveryLifecycleCommandPayload = DeliveryLifecycleCommandPayload()
    ): Result<DeliveryLifecycleResult>
    suspend fun cancelJob(jobId: String): Result<Boolean>
    suspend fun submitExtraCharge(
        jobId: String,
        type: ExtraChargeType,
        amount: Double,
        proofPath: String,
        note: String = ""
    ): Result<BookingExtraCharge>
}

class JobRepositoryImpl(private val api: JobApi) : JobRepository {
    override suspend fun getActiveJobs() = api.getActiveJobs("")
    override suspend fun getScheduledJobs() = api.getScheduledJobs("")
    override suspend fun getCompletedJobs() = api.getCompletedJobs("")
    override suspend fun getJobDetails(jobId: String) = api.getJobDetails(jobId)
    override suspend fun acceptJob(jobId: String) = api.acceptJob(jobId, "")
    override suspend fun rejectJob(jobId: String) = api.rejectJob(jobId, "")
    override suspend fun updateJobStatus(jobId: String, status: JobStatus) = api.updateJobStatus(jobId, status)
    override suspend fun submitProof(jobId: String, proof: ProofOfDelivery) = api.submitProofOfDelivery(jobId, proof)
    override suspend fun getIncomingRequest() = api.getIncomingJobRequest("")
    override suspend fun getIncomingRequests() = api.getIncomingJobRequests("")
    override suspend fun verifyPickupOtp(jobId: String, otp: String) = api.verifyPickupOtp(jobId, otp)
    override suspend fun requestDeliveryOtp(jobId: String, forceResend: Boolean) = api.requestDeliveryOtp(jobId, forceResend)
    override suspend fun executeLifecycleCommand(
        jobId: String,
        command: DeliveryLifecycleCommand,
        payload: DeliveryLifecycleCommandPayload
    ) = api.executeLifecycleCommand(jobId, command, payload)
    override suspend fun cancelJob(jobId: String) = api.cancelJob(jobId)
    override suspend fun submitExtraCharge(
        jobId: String,
        type: ExtraChargeType,
        amount: Double,
        proofPath: String,
        note: String
    ) = api.submitExtraCharge(jobId, type, amount, proofPath, note)
}

// ============================================================
// EARNINGS REPOSITORY
// ============================================================

interface EarningsRepository {
    suspend fun getEarningsSummary(): Result<EarningsSummary>
    suspend fun getTransactions(): Result<List<Transaction>>
    suspend fun getWalletInfo(): Result<WalletInfo>
    suspend fun requestWithdrawal(amount: Double): Result<Transaction>
    suspend fun getPayoutStatus(): Result<PayoutStatus>
    suspend fun createPayoutOnboardingLink(): Result<PayoutOnboardingLink>
    suspend fun getInvoiceUrl(transactionId: String): Result<InvoiceLink>
}

class EarningsRepositoryImpl(private val api: EarningsApi) : EarningsRepository {
    override suspend fun getEarningsSummary() = api.getEarningsSummary("")
    override suspend fun getTransactions() = api.getTransactionHistory("")
    override suspend fun getWalletInfo() = api.getWalletInfo("")
    override suspend fun requestWithdrawal(amount: Double) = api.requestWithdrawal("", amount)
    override suspend fun getPayoutStatus() = api.getPayoutStatus()
    override suspend fun createPayoutOnboardingLink() = api.createPayoutOnboardingLink()
    override suspend fun getInvoiceUrl(transactionId: String) = api.getInvoiceUrl(transactionId)
}

// ============================================================
// RATINGS REPOSITORY
// ============================================================

interface RatingsRepository {
    suspend fun getRatingInfo(): Result<RatingInfo>
    suspend fun rateCustomer(rating: CustomerRating): Result<Boolean>
}

class RatingsRepositoryImpl(private val api: RatingsApi) : RatingsRepository {
    override suspend fun getRatingInfo() = api.getRatingInfo("")
    override suspend fun rateCustomer(rating: CustomerRating) = api.submitCustomerRating(rating)
}

// ============================================================
// SUPPORT REPOSITORY
// ============================================================

interface SupportRepository {
    suspend fun getHelpArticles(): Result<List<HelpArticle>>
    suspend fun getTickets(): Result<List<SupportTicket>>
    suspend fun createTicket(ticket: SupportTicket): Result<SupportTicket>
    suspend fun getMessages(ticketId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(ticketId: String, message: ChatMessage): Result<ChatMessage>
    suspend fun triggerSos(latitude: Double, longitude: Double): Result<SosResult>
}

class SupportRepositoryImpl(private val api: SupportApi) : SupportRepository {
    override suspend fun getHelpArticles() = api.getHelpArticles()
    override suspend fun getTickets() = api.getTickets("")
    override suspend fun createTicket(ticket: SupportTicket) = api.createTicket(ticket)
    override suspend fun getMessages(ticketId: String) = api.getTicketMessages(ticketId)
    override suspend fun sendMessage(ticketId: String, message: ChatMessage) = api.sendMessage(ticketId, message)
    override suspend fun triggerSos(latitude: Double, longitude: Double) = api.triggerSos("", latitude, longitude)
}

// ============================================================
// NOTIFICATIONS REPOSITORY
// ============================================================

interface NotificationsRepository {
    suspend fun getNotifications(): Result<List<AppNotification>>
    suspend fun markAsRead(notificationId: String): Result<Boolean>
}

class NotificationsRepositoryImpl(private val api: NotificationsApi) : NotificationsRepository {
    override suspend fun getNotifications() = api.getNotifications("")
    override suspend fun markAsRead(notificationId: String) = api.markAsRead(notificationId)
}
