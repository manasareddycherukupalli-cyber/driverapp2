package com.example.drive_app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================================
// AUTH & DRIVER MODELS
// ============================================================

@Serializable
data class Driver(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    @SerialName("photo") val profileImageUrl: String? = null,
    val rating: Double = 0.0,
    @SerialName("totalTrips") val totalDeliveries: Int = 0,
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    @SerialName("vehicle") val vehicleDetails: VehicleDetails? = null,
    val documents: List<Document> = emptyList(),
    val emergencyContact: String = "",
    val createdAt: String? = null
)

@Serializable
data class AuthResponse(
    val success: Boolean = false,
    val driver: Driver? = null,
    val isNewDriver: Boolean = false
)

@Serializable
data class OtpRequest(
    val email: String
)

@Serializable
data class OtpVerifyRequest(
    val email: String,
    val otp: String
)

@Serializable
data class SyncRequest(
    val name: String = ""
)

@Serializable
data class RegisterRequest(
    val name: String,
    val phone: String = "",
    val emergencyContact: String = ""
)

// ============================================================
// VEHICLE MODELS
// ============================================================

@Serializable
data class VehicleDetails(
    val id: String = "",
    val type: VehicleType = VehicleType.BIKE,
    val make: String = "",
    val model: String = "",
    val year: Int = 2024,
    val licensePlate: String = "",
    val color: String = ""
)

@Serializable
enum class VehicleType(val displayName: String) {
    BIKE("Motorcycle"),
    CAR("Car"),
    VAN("Van"),
    TRUCK("Truck")
}

// ============================================================
// DOCUMENT MODELS
// ============================================================

@Serializable
data class Document(
    val id: String = "",
    val type: DocumentType = DocumentType.DRIVERS_LICENSE,
    val imageUrl: String = "",
    val status: DocumentStatus = DocumentStatus.PENDING,
    val uploadedAt: String? = null,
    val rejectionReason: String? = null
)

@Serializable
enum class DocumentType(val displayName: String) {
    DRIVERS_LICENSE("Driver's License"),
    VEHICLE_REGISTRATION("Vehicle Registration"),
    INSURANCE("Insurance Certificate"),
    PROFILE_PHOTO("Profile Photo"),
    ID_PROOF("Government ID")
}

@Serializable
enum class DocumentStatus { PENDING, APPROVED, REJECTED }

@Serializable
enum class VerificationStatus { PENDING, IN_REVIEW, APPROVED, REJECTED }

// ============================================================
// DELIVERY JOB MODELS
// ============================================================

@Serializable
data class DeliveryJob(
    val id: String = "",
    val status: JobStatus = JobStatus.PENDING,
    val pickup: LocationInfo = LocationInfo(),
    val dropoff: LocationInfo = LocationInfo(),
    val customerName: String = "",
    val customerPhone: String = "",
    val packageType: String = "",
    val packageSize: PackageSize = PackageSize.SMALL,
    val estimatedEarnings: Double = 0.0,
    val distance: Double = 0.0,
    val estimatedDuration: Int = 0,
    val createdAt: String? = null,
    val scheduledAt: String? = null,
    val acceptedAt: String? = null,
    val pickedUpAt: String? = null,
    val deliveredAt: String? = null,
    val completedAt: String? = null,
    val notes: String? = null,
    val proofOfDelivery: ProofOfDelivery? = null
)

@Serializable
data class LocationInfo(
    val address: String = "",
    val shortAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val instructions: String? = null
)

@Serializable
enum class JobStatus(val displayName: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    HEADING_TO_PICKUP("Heading to Pickup"),
    ARRIVED_AT_PICKUP("Arrived at Pickup"),
    PICKED_UP("Picked Up"),
    IN_TRANSIT("In Transit"),
    ARRIVED_AT_DROP("Arrived at Drop"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

@Serializable
enum class PackageSize(val displayName: String) {
    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large"),
    EXTRA_LARGE("Extra Large")
}

@Serializable
data class ProofOfDelivery(
    val photoUrl: String? = null,
    val signatureUrl: String? = null,
    val otpCode: String? = null,
    val deliveredAt: String? = null,
    val recipientName: String? = null
)

// ============================================================
// EARNINGS & WALLET MODELS
// ============================================================

@Serializable
data class EarningsSummary(
    val todayEarnings: Double = 0.0,
    val weeklyEarnings: Double = 0.0,
    val monthlyEarnings: Double = 0.0,
    val totalDeliveries: Int = 0,
    val todayDeliveries: Int = 0,
    val bonusEarnings: Double = 0.0,
    val tipEarnings: Double = 0.0,
    val onlineHours: Double = 0.0
)

@Serializable
data class Transaction(
    val id: String = "",
    val type: TransactionType = TransactionType.DELIVERY_EARNING,
    val amount: Double = 0.0,
    val description: String = "",
    @SerialName("createdAt") val timestamp: String? = null,
    val jobId: String? = null,
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

@Serializable
enum class TransactionType(val displayName: String) {
    DELIVERY_EARNING("Delivery"),
    BONUS("Bonus"),
    TIP("Tip"),
    WITHDRAWAL("Withdrawal"),
    INCENTIVE("Incentive"),
    ADJUSTMENT("Adjustment")
}

@Serializable
enum class TransactionStatus { PENDING, COMPLETED, FAILED }

@Serializable
data class WalletInfo(
    val balance: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val lifetimeEarnings: Double = 0.0,
    val lastPayout: Double? = null,
    val lastPayoutDate: String? = null,
    val bankAccountLinked: Boolean = false,
    val bankAccountLast4: String? = null
)

// ============================================================
// NOTIFICATION MODELS
// ============================================================

@Serializable
data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.SYSTEM,
    @SerialName("createdAt") val timestamp: String? = null,
    val isRead: Boolean = false,
    val actionData: String? = null
)

@Serializable
enum class NotificationType {
    JOB_REQUEST, JOB_UPDATE, PAYMENT, PROMO, SYSTEM, ALERT
}

// ============================================================
// RATING MODELS
// ============================================================

@Serializable
data class RatingInfo(
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    val fiveStarCount: Int = 0,
    val fourStarCount: Int = 0,
    val threeStarCount: Int = 0,
    val twoStarCount: Int = 0,
    val oneStarCount: Int = 0,
    val recentFeedback: List<FeedbackItem> = emptyList()
)

@Serializable
data class FeedbackItem(
    val id: String = "",
    val customerName: String = "",
    val rating: Int = 5,
    val comment: String? = null,
    val timestamp: String? = null,
    val jobId: String = ""
)

@Serializable
data class CustomerRating(
    val jobId: String,
    val rating: Int,
    val comment: String = ""
)

// ============================================================
// SUPPORT MODELS
// ============================================================

@Serializable
data class SupportTicket(
    val id: String = "",
    val subject: String = "",
    val category: TicketCategory = TicketCategory.GENERAL,
    val description: String = "",
    val status: TicketStatus = TicketStatus.OPEN,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val messages: List<ChatMessage> = emptyList()
)

@Serializable
enum class TicketCategory(val displayName: String) {
    PAYMENT("Payment Issue"),
    DELIVERY("Delivery Problem"),
    APP_BUG("App Bug"),
    ACCOUNT("Account Issue"),
    VEHICLE("Vehicle Problem"),
    GENERAL("General Query")
}

@Serializable
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val message: String = "",
    @SerialName("createdAt") val timestamp: String? = null,
    @SerialName("isStaff") val isFromDriver: Boolean = false
)

@Serializable
enum class TicketStatus(val displayName: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

@Serializable
data class HelpArticle(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = ""
)

// ============================================================
// API RESPONSE WRAPPERS
// ============================================================

@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null
)
