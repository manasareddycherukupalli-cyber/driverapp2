package com.example.drive_app.data.api

import com.example.drive_app.data.model.*
import kotlinx.coroutines.delay

/**
 * Dummy implementation of all API services for development/testing.
 * Simulates network delays and returns mock data.
 * Replace with real Ktor/Retrofit implementations for production.
 */
class DummyAuthApi : AuthApi {

    override suspend fun sendOtp(request: OtpRequest): Result<Boolean> {
        delay(1500) // Simulate network delay
        return Result.success(true)
    }

    override suspend fun verifyOtp(request: OtpVerifyRequest): Result<AuthResponse> {
        delay(1500)
        return if (request.otp == "1234" || request.otp == "123456") {
            Result.success(
                AuthResponse(
                    success = true,
                    token = "dummy_jwt_token_${System.currentTimeMillis()}",
                    driver = createDummyDriver(request.phone),
                    message = "Login successful"
                )
            )
        } else {
            Result.success(AuthResponse(success = false, message = "Invalid OTP"))
        }
    }

    override suspend fun registerDriver(driver: Driver): Result<AuthResponse> {
        delay(2000)
        return Result.success(
            AuthResponse(
                success = true,
                token = "dummy_jwt_token_new",
                driver = driver.copy(id = "DRV_${System.currentTimeMillis()}"),
                message = "Registration successful"
            )
        )
    }

    override suspend fun uploadDocument(driverId: String, document: Document): Result<Document> {
        delay(2000)
        return Result.success(
            document.copy(
                id = "DOC_${System.currentTimeMillis()}",
                status = DocumentStatus.PENDING,
                uploadedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun updateVehicleDetails(driverId: String, vehicle: VehicleDetails): Result<VehicleDetails> {
        delay(1500)
        return Result.success(vehicle.copy(id = "VEH_${System.currentTimeMillis()}"))
    }

    override suspend fun getVerificationStatus(driverId: String): Result<Driver> {
        delay(1000)
        return Result.success(createDummyDriver("+1234567890"))
    }

    override suspend fun getDriverProfile(driverId: String): Result<Driver> {
        delay(1000)
        return Result.success(createDummyDriver("+1234567890"))
    }

    override suspend fun updateDriverProfile(driver: Driver): Result<Driver> {
        delay(1500)
        return Result.success(driver)
    }

    override suspend fun toggleOnlineStatus(driverId: String, isOnline: Boolean): Result<Boolean> {
        delay(500)
        return Result.success(isOnline)
    }

    private fun createDummyDriver(phone: String) = Driver(
        id = "DRV_001",
        name = "Rajesh Kumar",
        phone = phone,
        email = "rajesh@example.com",
        profileImageUrl = null,
        rating = 4.8,
        totalDeliveries = 1247,
        isOnline = false,
        isVerified = true,
        verificationStatus = VerificationStatus.APPROVED,
        vehicleDetails = VehicleDetails(
            id = "VEH_001",
            type = VehicleType.BIKE,
            make = "Honda",
            model = "CB Shine",
            year = 2022,
            licensePlate = "MH-12-AB-1234",
            color = "Black"
        ),
        documents = listOf(
            Document("DOC_001", DocumentType.DRIVERS_LICENSE, "", DocumentStatus.APPROVED, System.currentTimeMillis()),
            Document("DOC_002", DocumentType.VEHICLE_REGISTRATION, "", DocumentStatus.APPROVED, System.currentTimeMillis()),
            Document("DOC_003", DocumentType.INSURANCE, "", DocumentStatus.PENDING, System.currentTimeMillis()),
            Document("DOC_004", DocumentType.PROFILE_PHOTO, "", DocumentStatus.APPROVED, System.currentTimeMillis()),
            Document("DOC_005", DocumentType.ID_PROOF, "", DocumentStatus.APPROVED, System.currentTimeMillis()),
        ),
        emergencyContact = "+919876543210",
        joinedAt = System.currentTimeMillis() - 86400000L * 180 // 6 months ago
    )
}

/**
 * Dummy Job API implementation with realistic mock data.
 */
class DummyJobApi : JobApi {

    private val dummyJobs = listOf(
        DeliveryJob(
            id = "JOB_001",
            status = JobStatus.ACCEPTED,
            pickup = LocationInfo(
                address = "45 MG Road, Indiranagar, Bangalore 560038",
                shortAddress = "MG Road, Indiranagar",
                latitude = 12.9716,
                longitude = 77.5946,
                contactName = "Priya Sharma",
                contactPhone = "+919876543001",
                instructions = "Ring the doorbell, 3rd floor"
            ),
            dropoff = LocationInfo(
                address = "22 Koramangala 4th Block, Bangalore 560034",
                shortAddress = "Koramangala 4th Block",
                latitude = 12.9352,
                longitude = 77.6245,
                contactName = "Amit Patel",
                contactPhone = "+919876543002",
                instructions = "Leave at reception desk"
            ),
            customerName = "Priya Sharma",
            customerPhone = "+919876543001",
            packageType = "Electronics",
            packageSize = PackageSize.MEDIUM,
            estimatedEarnings = 185.0,
            distance = 5.2,
            estimatedDuration = 22,
            createdAt = System.currentTimeMillis() - 600000,
            notes = "Handle with care - fragile item"
        ),
        DeliveryJob(
            id = "JOB_002",
            status = JobStatus.IN_TRANSIT,
            pickup = LocationInfo(
                address = "12 HSR Layout Sector 1, Bangalore 560102",
                shortAddress = "HSR Layout Sector 1",
                latitude = 12.9121,
                longitude = 77.6446,
                contactName = "Vikram Singh",
                contactPhone = "+919876543003"
            ),
            dropoff = LocationInfo(
                address = "78 Whitefield Main Road, Bangalore 560066",
                shortAddress = "Whitefield Main Road",
                latitude = 12.9698,
                longitude = 77.7500,
                contactName = "Meera Reddy",
                contactPhone = "+919876543004"
            ),
            customerName = "Vikram Singh",
            customerPhone = "+919876543003",
            packageType = "Documents",
            packageSize = PackageSize.SMALL,
            estimatedEarnings = 120.0,
            distance = 12.8,
            estimatedDuration = 35,
            createdAt = System.currentTimeMillis() - 1200000
        ),
        DeliveryJob(
            id = "JOB_003",
            status = JobStatus.DELIVERED,
            pickup = LocationInfo(
                address = "90 Brigade Road, Bangalore 560001",
                shortAddress = "Brigade Road",
                latitude = 12.9716,
                longitude = 77.6070
            ),
            dropoff = LocationInfo(
                address = "5 Electronic City Phase 1, Bangalore 560100",
                shortAddress = "Electronic City Phase 1",
                latitude = 12.8440,
                longitude = 77.6568
            ),
            customerName = "Suresh Babu",
            customerPhone = "+919876543005",
            packageType = "Food Package",
            packageSize = PackageSize.SMALL,
            estimatedEarnings = 95.0,
            distance = 3.1,
            estimatedDuration = 15,
            createdAt = System.currentTimeMillis() - 7200000,
            completedAt = System.currentTimeMillis() - 3600000
        ),
        DeliveryJob(
            id = "JOB_004",
            status = JobStatus.DELIVERED,
            pickup = LocationInfo(address = "Jayanagar 4th Block, Bangalore", shortAddress = "Jayanagar 4th Block", latitude = 12.9250, longitude = 77.5938),
            dropoff = LocationInfo(address = "BTM Layout 2nd Stage, Bangalore", shortAddress = "BTM Layout", latitude = 12.9166, longitude = 77.6101),
            customerName = "Deepa Nair",
            customerPhone = "+919876543006",
            packageType = "Clothing",
            packageSize = PackageSize.MEDIUM,
            estimatedEarnings = 75.0,
            distance = 2.5,
            estimatedDuration = 12,
            createdAt = System.currentTimeMillis() - 86400000,
            completedAt = System.currentTimeMillis() - 80000000
        ),
        DeliveryJob(
            id = "JOB_005",
            status = JobStatus.PENDING,
            pickup = LocationInfo(address = "Marathahalli Bridge, Bangalore", shortAddress = "Marathahalli", latitude = 12.9591, longitude = 77.6974),
            dropoff = LocationInfo(address = "Sarjapur Road, Bangalore", shortAddress = "Sarjapur Road", latitude = 12.9107, longitude = 77.6854),
            customerName = "Karthik Rao",
            customerPhone = "+919876543007",
            packageType = "Household Items",
            packageSize = PackageSize.LARGE,
            estimatedEarnings = 250.0,
            distance = 8.5,
            estimatedDuration = 28,
            createdAt = System.currentTimeMillis(),
            scheduledAt = System.currentTimeMillis() + 3600000
        )
    )

    override suspend fun getActiveJobs(driverId: String): Result<List<DeliveryJob>> {
        delay(1000)
        return Result.success(
            dummyJobs.filter { it.status in listOf(JobStatus.ACCEPTED, JobStatus.HEADING_TO_PICKUP, JobStatus.ARRIVED_AT_PICKUP, JobStatus.PICKED_UP, JobStatus.IN_TRANSIT, JobStatus.ARRIVED_AT_DROP) }
        )
    }

    override suspend fun getScheduledJobs(driverId: String): Result<List<DeliveryJob>> {
        delay(1000)
        return Result.success(dummyJobs.filter { it.scheduledAt != null && it.status == JobStatus.PENDING })
    }

    override suspend fun getCompletedJobs(driverId: String): Result<List<DeliveryJob>> {
        delay(1000)
        return Result.success(dummyJobs.filter { it.status == JobStatus.DELIVERED || it.status == JobStatus.CANCELLED })
    }

    override suspend fun getJobDetails(jobId: String): Result<DeliveryJob> {
        delay(800)
        val job = dummyJobs.find { it.id == jobId }
        return if (job != null) Result.success(job) else Result.failure(Exception("Job not found"))
    }

    override suspend fun acceptJob(jobId: String, driverId: String): Result<DeliveryJob> {
        delay(1000)
        val job = dummyJobs.find { it.id == jobId }
        return if (job != null) {
            Result.success(job.copy(status = JobStatus.ACCEPTED, acceptedAt = System.currentTimeMillis()))
        } else {
            Result.failure(Exception("Job not found"))
        }
    }

    override suspend fun rejectJob(jobId: String, driverId: String): Result<Boolean> {
        delay(500)
        return Result.success(true)
    }

    override suspend fun updateJobStatus(jobId: String, status: JobStatus): Result<DeliveryJob> {
        delay(800)
        val job = dummyJobs.find { it.id == jobId }
        return if (job != null) {
            Result.success(job.copy(status = status))
        } else {
            Result.failure(Exception("Job not found"))
        }
    }

    override suspend fun submitProofOfDelivery(jobId: String, proof: ProofOfDelivery): Result<DeliveryJob> {
        delay(2000)
        val job = dummyJobs.find { it.id == jobId }
        return if (job != null) {
            Result.success(job.copy(status = JobStatus.DELIVERED, proofOfDelivery = proof, completedAt = System.currentTimeMillis()))
        } else {
            Result.failure(Exception("Job not found"))
        }
    }

    override suspend fun getIncomingJobRequest(driverId: String): Result<DeliveryJob?> {
        delay(500)
        // Simulate occasional incoming job request
        return Result.success(dummyJobs.find { it.id == "JOB_005" })
    }
}

/**
 * Dummy Earnings API with mock financial data.
 */
class DummyEarningsApi : EarningsApi {

    override suspend fun getEarningsSummary(driverId: String): Result<EarningsSummary> {
        delay(1000)
        return Result.success(
            EarningsSummary(
                todayEarnings = 1850.0,
                weeklyEarnings = 12450.0,
                monthlyEarnings = 48500.0,
                totalDeliveries = 1247,
                todayDeliveries = 12,
                bonusEarnings = 2500.0,
                tipEarnings = 850.0,
                onlineHours = 8.5
            )
        )
    }

    override suspend fun getTransactionHistory(driverId: String): Result<List<Transaction>> {
        delay(1000)
        val now = System.currentTimeMillis()
        return Result.success(
            listOf(
                Transaction("TXN_001", TransactionType.DELIVERY_EARNING, 185.0, "Delivery #JOB_001", now - 3600000, "JOB_001"),
                Transaction("TXN_002", TransactionType.TIP, 30.0, "Tip from Priya Sharma", now - 3500000, "JOB_001"),
                Transaction("TXN_003", TransactionType.DELIVERY_EARNING, 120.0, "Delivery #JOB_002", now - 7200000, "JOB_002"),
                Transaction("TXN_004", TransactionType.BONUS, 500.0, "Weekly bonus - 50 deliveries!", now - 86400000),
                Transaction("TXN_005", TransactionType.DELIVERY_EARNING, 95.0, "Delivery #JOB_003", now - 10800000, "JOB_003"),
                Transaction("TXN_006", TransactionType.WITHDRAWAL, -5000.0, "Bank withdrawal", now - 172800000),
                Transaction("TXN_007", TransactionType.INCENTIVE, 200.0, "Peak hour incentive", now - 172800000),
                Transaction("TXN_008", TransactionType.DELIVERY_EARNING, 75.0, "Delivery #JOB_004", now - 259200000, "JOB_004"),
                Transaction("TXN_009", TransactionType.DELIVERY_EARNING, 310.0, "Delivery #JOB_010", now - 345600000, "JOB_010"),
                Transaction("TXN_010", TransactionType.TIP, 50.0, "Tip from Karthik", now - 345600000, "JOB_010"),
            )
        )
    }

    override suspend fun getWalletInfo(driverId: String): Result<WalletInfo> {
        delay(800)
        return Result.success(
            WalletInfo(
                balance = 8750.0,
                pendingAmount = 1200.0,
                lifetimeEarnings = 248500.0,
                lastPayout = 15000.0,
                lastPayoutDate = System.currentTimeMillis() - 172800000,
                bankAccountLinked = true,
                bankAccountLast4 = "4521"
            )
        )
    }

    override suspend fun requestWithdrawal(driverId: String, amount: Double): Result<Transaction> {
        delay(2000)
        return Result.success(
            Transaction(
                id = "TXN_WD_${System.currentTimeMillis()}",
                type = TransactionType.WITHDRAWAL,
                amount = -amount,
                description = "Bank withdrawal",
                timestamp = System.currentTimeMillis(),
                status = TransactionStatus.PENDING
            )
        )
    }
}

/**
 * Dummy Ratings API with mock rating data.
 */
class DummyRatingsApi : RatingsApi {

    override suspend fun getRatingInfo(driverId: String): Result<RatingInfo> {
        delay(1000)
        val now = System.currentTimeMillis()
        return Result.success(
            RatingInfo(
                averageRating = 4.8,
                totalRatings = 892,
                fiveStarCount = 680,
                fourStarCount = 150,
                threeStarCount = 42,
                twoStarCount = 15,
                oneStarCount = 5,
                recentFeedback = listOf(
                    FeedbackItem("FB_001", "Priya S.", 5, "Very quick delivery, excellent service!", now - 3600000, "JOB_001"),
                    FeedbackItem("FB_002", "Amit P.", 5, "Handled my package with care. Thank you!", now - 7200000, "JOB_002"),
                    FeedbackItem("FB_003", "Suresh B.", 4, "Good delivery, slightly late", now - 86400000, "JOB_003"),
                    FeedbackItem("FB_004", "Deepa N.", 5, "Professional and courteous", now - 172800000, "JOB_004"),
                    FeedbackItem("FB_005", "Rahul M.", 4, null, now - 259200000, "JOB_006"),
                    FeedbackItem("FB_006", "Anita K.", 5, "Best driver! Always on time", now - 345600000, "JOB_007"),
                )
            )
        )
    }

    override suspend fun submitCustomerRating(rating: CustomerRating): Result<Boolean> {
        delay(1000)
        return Result.success(true)
    }
}

/**
 * Dummy Support API with mock help articles and tickets.
 */
class DummySupportApi : SupportApi {

    override suspend fun getHelpArticles(): Result<List<HelpArticle>> {
        delay(800)
        return Result.success(
            listOf(
                HelpArticle("HA_001", "How to accept a delivery job?", "When a new job request arrives, you'll see a popup with pickup and drop-off details...", "Getting Started"),
                HelpArticle("HA_002", "How are earnings calculated?", "Earnings are calculated based on distance, package size, and demand...", "Earnings"),
                HelpArticle("HA_003", "How to upload documents?", "Go to Profile > Documents and tap on the document type you want to upload...", "Account"),
                HelpArticle("HA_004", "What to do if customer is unavailable?", "Wait for 5 minutes at the drop-off location. If the customer doesn't respond...", "Delivery"),
                HelpArticle("HA_005", "How to withdraw earnings?", "Go to Earnings > Wallet > Withdraw. Enter the amount and confirm...", "Payments"),
                HelpArticle("HA_006", "How to handle damaged packages?", "If you notice a package is damaged before pickup, inform the customer...", "Delivery"),
                HelpArticle("HA_007", "Account verification process", "After registration, your documents will be reviewed within 24-48 hours...", "Account"),
                HelpArticle("HA_008", "Safety guidelines", "Always wear a helmet, follow traffic rules, and keep your phone mounted...", "Safety"),
            )
        )
    }

    override suspend fun getTickets(driverId: String): Result<List<SupportTicket>> {
        delay(1000)
        val now = System.currentTimeMillis()
        return Result.success(
            listOf(
                SupportTicket("TKT_001", "Payment not received for JOB_003", TicketCategory.PAYMENT, "I completed delivery JOB_003 but haven't received payment yet.", TicketStatus.IN_PROGRESS, now - 86400000, now - 3600000),
                SupportTicket("TKT_002", "App crashes during navigation", TicketCategory.APP_BUG, "The app crashes when I try to start navigation.", TicketStatus.RESOLVED, now - 604800000, now - 172800000),
            )
        )
    }

    override suspend fun createTicket(ticket: SupportTicket): Result<SupportTicket> {
        delay(1500)
        return Result.success(
            ticket.copy(
                id = "TKT_${System.currentTimeMillis()}",
                status = TicketStatus.OPEN,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getTicketMessages(ticketId: String): Result<List<ChatMessage>> {
        delay(800)
        val now = System.currentTimeMillis()
        return Result.success(
            listOf(
                ChatMessage("MSG_001", "DRV_001", "I completed delivery JOB_003 but haven't received payment yet.", now - 86400000, true),
                ChatMessage("MSG_002", "SUPPORT_001", "Hi Rajesh, we're looking into this. Can you provide the delivery completion screenshot?", now - 82800000, false),
                ChatMessage("MSG_003", "DRV_001", "Sure, here it is. I have the proof of delivery too.", now - 79200000, true),
                ChatMessage("MSG_004", "SUPPORT_001", "Thank you! We've escalated this to our payments team. You should receive the payment within 24 hours.", now - 3600000, false),
            )
        )
    }

    override suspend fun sendMessage(ticketId: String, message: ChatMessage): Result<ChatMessage> {
        delay(500)
        return Result.success(message.copy(id = "MSG_${System.currentTimeMillis()}", timestamp = System.currentTimeMillis()))
    }

    override suspend fun triggerSos(driverId: String, latitude: Double, longitude: Double): Result<Boolean> {
        delay(1000)
        return Result.success(true)
    }
}

/**
 * Dummy Notifications API.
 */
class DummyNotificationsApi : NotificationsApi {

    override suspend fun getNotifications(driverId: String): Result<List<AppNotification>> {
        delay(800)
        val now = System.currentTimeMillis()
        return Result.success(
            listOf(
                AppNotification("NOT_001", "New Job Available!", "A delivery request is available near you. Tap to view.", NotificationType.JOB_REQUEST, now - 300000, false),
                AppNotification("NOT_002", "Payment Received", "₹185 has been added to your wallet for JOB_001.", NotificationType.PAYMENT, now - 3600000, true),
                AppNotification("NOT_003", "Weekly Bonus Unlocked! 🎉", "Congratulations! You've earned a ₹500 bonus for completing 50 deliveries.", NotificationType.PAYMENT, now - 86400000, true),
                AppNotification("NOT_004", "Peak Hours Active", "Earn 1.5x on deliveries between 12 PM - 2 PM today!", NotificationType.PROMO, now - 172800000, true),
                AppNotification("NOT_005", "Document Approved", "Your driver's license has been verified.", NotificationType.SYSTEM, now - 604800000, true),
                AppNotification("NOT_006", "Rate Your Last Customer", "How was your experience with Priya Sharma?", NotificationType.JOB_UPDATE, now - 7200000, false),
            )
        )
    }

    override suspend fun markAsRead(notificationId: String): Result<Boolean> {
        delay(300)
        return Result.success(true)
    }
}

/**
 * System.currentTimeMillis() substitute for KMM.
 * In production, replace with a proper KMM-compatible time utility.
 */
private object System {
    fun currentTimeMillis(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
}
