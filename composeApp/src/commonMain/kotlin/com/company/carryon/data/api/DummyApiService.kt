package com.company.carryon.data.api

import com.company.carryon.data.model.*
import kotlinx.coroutines.delay
private fun now(): String = "2026-03-11T12:00:00Z"

/**
 * Dummy implementation of all API services for development/testing.
 * Not wired in production — ServiceLocator uses Real*Api implementations.
 */
class DummyAuthApi : AuthApi {

    override suspend fun sendOtp(request: OtpRequest): Result<Boolean> {
        delay(1500)
        return Result.success(true)
    }

    override suspend fun verifyOtp(request: OtpVerifyRequest): Result<AuthResponse> {
        delay(1500)
        return if (request.otp == "1234" || request.otp == "123456") {
            Result.success(AuthResponse(success = true, driver = createDummyDriver()))
        } else {
            Result.success(AuthResponse(success = false))
        }
    }

    override suspend fun registerDriver(driver: Driver): Result<AuthResponse> {
        delay(2000)
        return Result.success(AuthResponse(success = true, driver = driver.copy(id = "DRV_NEW")))
    }

    override suspend fun uploadDocument(driverId: String, document: Document): Result<Document> {
        delay(2000)
        return Result.success(document.copy(id = "DOC_NEW", status = DocumentStatus.PENDING, uploadedAt = now()))
    }

    override suspend fun updateVehicleDetails(driverId: String, vehicle: VehicleDetails): Result<VehicleDetails> {
        delay(1500)
        return Result.success(vehicle.copy(id = "VEH_NEW"))
    }

    override suspend fun getVerificationStatus(driverId: String): Result<Driver> {
        delay(1000)
        return Result.success(createDummyDriver())
    }

    override suspend fun getDriverProfile(driverId: String): Result<Driver> {
        delay(1000)
        return Result.success(createDummyDriver())
    }

    override suspend fun updateDriverProfile(driver: Driver): Result<Driver> {
        delay(1500)
        return Result.success(driver)
    }

    override suspend fun toggleOnlineStatus(driverId: String, isOnline: Boolean): Result<Boolean> {
        delay(500)
        return Result.success(isOnline)
    }

    override suspend fun updateFcmToken(driverId: String, fcmToken: String): Result<Boolean> {
        delay(200)
        return Result.success(true)
    }

    private fun createDummyDriver() = Driver(
        id = "DRV_001",
        name = "Ahmad bin Hassan",
        phone = "+60123456789",
        email = "ahmad@example.com",
        rating = 4.8,
        totalDeliveries = 1247,
        isOnline = false,
        isVerified = true,
        verificationStatus = VerificationStatus.APPROVED,
        vehicleDetails = VehicleDetails(id = "VEH_001", type = VehicleType.BIKE, make = "Honda", model = "EX5", year = 2022, licensePlate = "WA 1234 B", color = "Black"),
        documents = listOf(
            Document("DOC_001", DocumentType.DRIVERS_LICENSE, "", DocumentStatus.APPROVED, now()),
            Document("DOC_002", DocumentType.VEHICLE_REGISTRATION, "", DocumentStatus.APPROVED, now()),
        ),
        emergencyContact = "+60198765432",
        createdAt = now()
    )
}

class DummyJobApi : JobApi {
    private val dummyJobs = listOf(
        DeliveryJob(
            id = "JOB_001", status = JobStatus.ACCEPTED,
            pickup = LocationInfo("45 Jalan Bukit Bintang, KL 55100", "Bukit Bintang", 3.1466, 101.7108, "Siti Aminah", "+60123456001"),
            dropoff = LocationInfo("22 Bangsar South, KL 59200", "Bangsar South", 3.1101, 101.6653, "Raj Kumar", "+60123456002"),
            customerName = "Siti Aminah", customerPhone = "+60123456001",
            packageType = "Electronics", packageSize = PackageSize.MEDIUM,
            estimatedEarnings = 18.50, distance = 5.2, estimatedDuration = 22,
            createdAt = now(), notes = "Handle with care"
        ),
        DeliveryJob(
            id = "JOB_002", status = JobStatus.IN_TRANSIT,
            pickup = LocationInfo("12 Petaling Jaya SS2, Selangor", "PJ SS2", 3.1179, 101.6277),
            dropoff = LocationInfo("78 Subang Jaya, Selangor", "Subang Jaya", 3.0565, 101.5851),
            customerName = "Lim Wei", customerPhone = "+60123456003",
            packageType = "Documents", packageSize = PackageSize.SMALL,
            estimatedEarnings = 12.00, distance = 12.8, estimatedDuration = 35, createdAt = now()
        ),
        DeliveryJob(
            id = "JOB_003", status = JobStatus.DELIVERED,
            pickup = LocationInfo("Pavilion KL, Bukit Bintang", "Pavilion KL", 3.1490, 101.7131),
            dropoff = LocationInfo("Mid Valley Megamall, KL", "Mid Valley", 3.1178, 101.6775),
            customerName = "Tan Mei Ling", customerPhone = "+60123456005",
            packageType = "Food Package", packageSize = PackageSize.SMALL,
            estimatedEarnings = 9.50, distance = 3.1, estimatedDuration = 15, createdAt = now(), completedAt = now()
        ),
        DeliveryJob(
            id = "JOB_005", status = JobStatus.PENDING,
            pickup = LocationInfo("Damansara Perdana, Selangor", "Damansara", 3.1570, 101.6158),
            dropoff = LocationInfo("Mont Kiara, KL", "Mont Kiara", 3.1710, 101.6512),
            customerName = "Nurul Huda", customerPhone = "+60123456007",
            packageType = "Household Items", packageSize = PackageSize.LARGE,
            estimatedEarnings = 25.00, distance = 8.5, estimatedDuration = 28, createdAt = now(), scheduledAt = now()
        )
    )

    override suspend fun getActiveJobs(driverId: String): Result<List<DeliveryJob>> {
        delay(1000); return Result.success(dummyJobs.filter { it.status in listOf(JobStatus.ACCEPTED, JobStatus.IN_TRANSIT) })
    }
    override suspend fun getScheduledJobs(driverId: String): Result<List<DeliveryJob>> {
        delay(1000); return Result.success(dummyJobs.filter { it.scheduledAt != null && it.status == JobStatus.PENDING })
    }
    override suspend fun getCompletedJobs(driverId: String): Result<List<DeliveryJob>> {
        delay(1000); return Result.success(dummyJobs.filter { it.status == JobStatus.DELIVERED })
    }
    override suspend fun getJobDetails(jobId: String): Result<DeliveryJob> {
        delay(800); return dummyJobs.find { it.id == jobId }?.let { Result.success(it) } ?: Result.failure(Exception("Job not found"))
    }
    override suspend fun acceptJob(jobId: String, driverId: String): Result<DeliveryJob> {
        delay(1000); return dummyJobs.find { it.id == jobId }?.let { Result.success(it.copy(status = JobStatus.ACCEPTED, acceptedAt = now())) } ?: Result.failure(Exception("Job not found"))
    }
    override suspend fun rejectJob(jobId: String, driverId: String): Result<Boolean> { delay(500); return Result.success(true) }
    override suspend fun updateJobStatus(jobId: String, status: JobStatus): Result<DeliveryJob> {
        delay(800); return dummyJobs.find { it.id == jobId }?.let { Result.success(it.copy(status = status)) } ?: Result.failure(Exception("Job not found"))
    }
    override suspend fun submitProofOfDelivery(jobId: String, proof: ProofOfDelivery): Result<DeliveryJob> {
        delay(2000); return dummyJobs.find { it.id == jobId }?.let { Result.success(it.copy(status = JobStatus.DELIVERED, proofOfDelivery = proof, completedAt = now())) } ?: Result.failure(Exception("Job not found"))
    }
    override suspend fun getIncomingJobRequest(driverId: String): Result<DeliveryJob?> { delay(500); return Result.success(dummyJobs.find { it.id == "JOB_005" }) }
}

class DummyEarningsApi : EarningsApi {
    override suspend fun getEarningsSummary(driverId: String): Result<EarningsSummary> {
        delay(1000); return Result.success(EarningsSummary(todayEarnings = 185.0, weeklyEarnings = 1245.0, monthlyEarnings = 4850.0, totalDeliveries = 1247, todayDeliveries = 12, bonusEarnings = 250.0, tipEarnings = 85.0, onlineHours = 8.5))
    }
    override suspend fun getTransactionHistory(driverId: String): Result<List<Transaction>> {
        delay(1000); return Result.success(listOf(
            Transaction("TXN_001", TransactionType.DELIVERY_EARNING, 18.50, "Delivery #JOB_001", now(), "JOB_001"),
            Transaction("TXN_002", TransactionType.TIP, 3.00, "Tip from Siti Aminah", now(), "JOB_001"),
            Transaction("TXN_003", TransactionType.BONUS, 50.00, "Weekly bonus!", now()),
            Transaction("TXN_004", TransactionType.WITHDRAWAL, -500.00, "Bank withdrawal", now()),
        ))
    }
    override suspend fun getWalletInfo(driverId: String): Result<WalletInfo> {
        delay(800); return Result.success(WalletInfo(balance = 875.0, pendingAmount = 120.0, lifetimeEarnings = 24850.0, lastPayout = 1500.0, lastPayoutDate = now(), bankAccountLinked = true, bankAccountLast4 = "4521"))
    }
    override suspend fun requestWithdrawal(driverId: String, amount: Double): Result<Transaction> {
        delay(2000); return Result.success(Transaction(id = "TXN_WD", type = TransactionType.WITHDRAWAL, amount = -amount, description = "Bank withdrawal", timestamp = now(), status = TransactionStatus.PENDING))
    }
}

class DummyRatingsApi : RatingsApi {
    override suspend fun getRatingInfo(driverId: String): Result<RatingInfo> {
        delay(1000); return Result.success(RatingInfo(averageRating = 4.8, totalRatings = 892, fiveStarCount = 680, fourStarCount = 150, threeStarCount = 42, twoStarCount = 15, oneStarCount = 5, recentFeedback = listOf(
            FeedbackItem("FB_001", "Siti A.", 5, "Very quick delivery!", now(), "JOB_001"),
            FeedbackItem("FB_002", "Raj K.", 5, "Handled my package with care.", now(), "JOB_002"),
            FeedbackItem("FB_003", "Tan M.", 4, "Good delivery, slightly late", now(), "JOB_003"),
        )))
    }
    override suspend fun submitCustomerRating(rating: CustomerRating): Result<Boolean> { delay(1000); return Result.success(true) }
}

class DummySupportApi : SupportApi {
    override suspend fun getHelpArticles(): Result<List<HelpArticle>> {
        delay(800); return Result.success(listOf(
            HelpArticle("HA_001", "How to accept a delivery job?", "When a new job request arrives, you'll see a popup...", "Getting Started"),
            HelpArticle("HA_002", "How are earnings calculated?", "Earnings are based on distance, package size, and demand...", "Earnings"),
            HelpArticle("HA_003", "How to upload documents?", "Go to Profile > Documents...", "Account"),
            HelpArticle("HA_004", "How to withdraw earnings?", "Go to Earnings > Wallet > Withdraw...", "Payments"),
        ))
    }
    override suspend fun getTickets(driverId: String): Result<List<SupportTicket>> {
        delay(1000); return Result.success(listOf(
            SupportTicket("TKT_001", "Payment not received for JOB_003", TicketCategory.PAYMENT, "Haven't received payment.", TicketStatus.IN_PROGRESS, now(), now()),
        ))
    }
    override suspend fun createTicket(ticket: SupportTicket): Result<SupportTicket> {
        delay(1500); return Result.success(ticket.copy(id = "TKT_NEW", status = TicketStatus.OPEN, createdAt = now()))
    }
    override suspend fun getTicketMessages(ticketId: String): Result<List<ChatMessage>> {
        delay(800); return Result.success(listOf(
            ChatMessage("MSG_001", "DRV_001", "I haven't received payment.", now(), false),
            ChatMessage("MSG_002", "SUPPORT", "We're looking into this.", now(), true),
        ))
    }
    override suspend fun sendMessage(ticketId: String, message: ChatMessage): Result<ChatMessage> {
        delay(500); return Result.success(message.copy(id = "MSG_NEW", timestamp = now()))
    }
    override suspend fun triggerSos(driverId: String, latitude: Double, longitude: Double): Result<Boolean> { delay(1000); return Result.success(true) }
}

class DummyNotificationsApi : NotificationsApi {
    override suspend fun getNotifications(driverId: String): Result<List<AppNotification>> {
        delay(800); return Result.success(listOf(
            AppNotification("NOT_001", "New Job Available!", "A delivery request is available near you.", NotificationType.JOB_REQUEST, now(), false),
            AppNotification("NOT_002", "Payment Received", "RM 18.50 has been added to your wallet.", NotificationType.PAYMENT, now(), true),
            AppNotification("NOT_003", "Weekly Bonus Unlocked!", "You've earned RM 50 bonus for 50 deliveries.", NotificationType.PAYMENT, now(), true),
        ))
    }
    override suspend fun markAsRead(notificationId: String): Result<Boolean> { delay(300); return Result.success(true) }
}
