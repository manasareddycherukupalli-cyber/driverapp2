package com.example.drive_app.data.repository

import com.example.drive_app.data.api.*
import com.example.drive_app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// ============================================================
// AUTH REPOSITORY
// ============================================================

interface AuthRepository {
    val currentDriver: Flow<Driver?>
    val isLoggedIn: Flow<Boolean>
    suspend fun sendOtp(phone: String, countryCode: String): Result<Boolean>
    suspend fun verifyOtp(phone: String, otp: String): Result<AuthResponse>
    suspend fun register(driver: Driver): Result<AuthResponse>
    suspend fun uploadDocument(document: Document): Result<Document>
    suspend fun updateVehicle(vehicle: VehicleDetails): Result<VehicleDetails>
    suspend fun getVerificationStatus(): Result<Driver>
    suspend fun toggleOnline(isOnline: Boolean): Result<Boolean>
    suspend fun updateProfile(driver: Driver): Result<Driver>
    suspend fun logout()
}

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {
    private val _currentDriver = MutableStateFlow<Driver?>(null)
    override val currentDriver: Flow<Driver?> = _currentDriver.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    private var authToken: String? = null

    override suspend fun sendOtp(phone: String, countryCode: String): Result<Boolean> {
        return api.sendOtp(OtpRequest(phone, countryCode))
    }

    override suspend fun verifyOtp(phone: String, otp: String): Result<AuthResponse> {
        val result = api.verifyOtp(OtpVerifyRequest(phone, otp))
        result.getOrNull()?.let { response ->
            if (response.success) {
                authToken = response.token
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
                authToken = response.token
                _currentDriver.value = response.driver
                _isLoggedIn.value = true
            }
        }
        return result
    }

    override suspend fun uploadDocument(document: Document): Result<Document> {
        val driverId = _currentDriver.value?.id ?: return Result.failure(Exception("Not logged in"))
        val result = api.uploadDocument(driverId, document)
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

    override suspend fun updateProfile(driver: Driver): Result<Driver> {
        val result = api.updateDriverProfile(driver)
        result.getOrNull()?.let { updated ->
            _currentDriver.value = updated
        }
        return result
    }

    override suspend fun logout() {
        authToken = null
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
}

class JobRepositoryImpl(private val api: JobApi, private val driverId: String = "DRV_001") : JobRepository {
    override suspend fun getActiveJobs() = api.getActiveJobs(driverId)
    override suspend fun getScheduledJobs() = api.getScheduledJobs(driverId)
    override suspend fun getCompletedJobs() = api.getCompletedJobs(driverId)
    override suspend fun getJobDetails(jobId: String) = api.getJobDetails(jobId)
    override suspend fun acceptJob(jobId: String) = api.acceptJob(jobId, driverId)
    override suspend fun rejectJob(jobId: String) = api.rejectJob(jobId, driverId)
    override suspend fun updateJobStatus(jobId: String, status: JobStatus) = api.updateJobStatus(jobId, status)
    override suspend fun submitProof(jobId: String, proof: ProofOfDelivery) = api.submitProofOfDelivery(jobId, proof)
    override suspend fun getIncomingRequest() = api.getIncomingJobRequest(driverId)
}

// ============================================================
// EARNINGS REPOSITORY
// ============================================================

interface EarningsRepository {
    suspend fun getEarningsSummary(): Result<EarningsSummary>
    suspend fun getTransactions(): Result<List<Transaction>>
    suspend fun getWalletInfo(): Result<WalletInfo>
    suspend fun requestWithdrawal(amount: Double): Result<Transaction>
}

class EarningsRepositoryImpl(private val api: EarningsApi, private val driverId: String = "DRV_001") : EarningsRepository {
    override suspend fun getEarningsSummary() = api.getEarningsSummary(driverId)
    override suspend fun getTransactions() = api.getTransactionHistory(driverId)
    override suspend fun getWalletInfo() = api.getWalletInfo(driverId)
    override suspend fun requestWithdrawal(amount: Double) = api.requestWithdrawal(driverId, amount)
}

// ============================================================
// RATINGS REPOSITORY
// ============================================================

interface RatingsRepository {
    suspend fun getRatingInfo(): Result<RatingInfo>
    suspend fun rateCustomer(rating: CustomerRating): Result<Boolean>
}

class RatingsRepositoryImpl(private val api: RatingsApi, private val driverId: String = "DRV_001") : RatingsRepository {
    override suspend fun getRatingInfo() = api.getRatingInfo(driverId)
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
    suspend fun triggerSos(latitude: Double, longitude: Double): Result<Boolean>
}

class SupportRepositoryImpl(private val api: SupportApi, private val driverId: String = "DRV_001") : SupportRepository {
    override suspend fun getHelpArticles() = api.getHelpArticles()
    override suspend fun getTickets() = api.getTickets(driverId)
    override suspend fun createTicket(ticket: SupportTicket) = api.createTicket(ticket)
    override suspend fun getMessages(ticketId: String) = api.getTicketMessages(ticketId)
    override suspend fun sendMessage(ticketId: String, message: ChatMessage) = api.sendMessage(ticketId, message)
    override suspend fun triggerSos(latitude: Double, longitude: Double) = api.triggerSos(driverId, latitude, longitude)
}

// ============================================================
// NOTIFICATIONS REPOSITORY
// ============================================================

interface NotificationsRepository {
    suspend fun getNotifications(): Result<List<AppNotification>>
    suspend fun markAsRead(notificationId: String): Result<Boolean>
}

class NotificationsRepositoryImpl(private val api: NotificationsApi, private val driverId: String = "DRV_001") : NotificationsRepository {
    override suspend fun getNotifications() = api.getNotifications(driverId)
    override suspend fun markAsRead(notificationId: String) = api.markAsRead(notificationId)
}
