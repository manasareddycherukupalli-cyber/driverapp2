package com.company.carryon.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.time.Instant
import kotlin.time.Duration.Companion.seconds

// ============================================================
// AUTH & DRIVER MODELS
// ============================================================

@Serializable
data class Driver(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val driversLicenseNumber: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val preferredLanguage: String = "",
    val nationality: DriverNationality? = null,
    val mykadNumber: String = "",
    val passportNumber: String = "",
    val passportExpiry: String? = null,
    val plksNumber: String = "",
    val plksExpiry: String? = null,
    val licenseClass: LicenseClass? = null,
    val licenseExpiry: String? = null,
    val hasGDL: Boolean = false,
    val gdlExpiry: String? = null,
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val postcode: String = "",
    val state: MalaysianState? = null,
    val workingStates: List<MalaysianState> = emptyList(),
    val emergencyContactName: String = "",
    val emergencyContactRelation: String = "",
    val emergencyContactPhone: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankAccountHolder: String = "",
    val duitNowId: String = "",
    val tngEwalletId: String = "",
    val lhdnTaxNumber: String = "",
    val sstNumber: String = "",
    val pdpaConsent: Boolean = false,
    val backgroundCheckConsent: Boolean = false,
    val agreementVersion: String = "",
    val noOffencesDeclared: Boolean = false,
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
    val year: Int = 0,
    val licensePlate: String = "",
    val color: String = "",
    val chassisNumber: String = "",
    val engineNumber: String = "",
    val ownership: VehicleOwnership? = null,
    val ownerName: String = "",
    val roadTaxExpiry: String? = null,
    val puspakomExpiry: String? = null,
    val apadPermitNumber: String = "",
    val apadPermitExpiry: String? = null,
    val insurerName: String = "",
    val insurancePolicyNumber: String = "",
    val insuranceCoverageType: InsuranceCoverageType? = null,
    val insuranceExpiry: String? = null,
    val hasCommercialCover: Boolean = false
)

@Serializable
enum class VehicleType(val displayName: String) {
    BIKE("Motorcycle"),
    CAR("Car"),
    PICKUP("Pickup"),
    VAN("Van"),
    VAN_7FT("Van 7ft"),
    VAN_9FT("Van 9ft"),
    LORRY_10FT("Lorry 10ft"),
    LORRY_14FT("Lorry 14ft"),
    LORRY_17FT("Lorry 17ft"),
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
    val expiryDate: String? = null,
    val status: DocumentStatus = DocumentStatus.PENDING,
    val uploadedAt: String? = null,
    val rejectionReason: String? = null
)

@Serializable
enum class DocumentType(val displayName: String) {
    DRIVERS_LICENSE("Driver's License"),
    DRIVERS_LICENSE_BACK("Driver's License (Back)"),
    GDL("GDL"),
    VEHICLE_REGISTRATION("Vehicle Registration"),
    INSURANCE("Insurance Certificate"),
    PROFILE_PHOTO("Profile Photo"),
    ID_PROOF("Government ID"),
    MYKAD_FRONT("MyKad Front"),
    MYKAD_BACK("MyKad Back"),
    SELFIE("Selfie"),
    PASSPORT("Passport"),
    WORK_PERMIT_PLKS("Work Permit / PLKS"),
    ROAD_TAX("Road Tax"),
    PUSPAKOM("PUSPAKOM"),
    APAD_PERMIT("APAD / LPKP Permit"),
    VEHICLE_PHOTO_FRONT("Vehicle Front Photo"),
    VEHICLE_PHOTO_BACK("Vehicle Back Photo"),
    VEHICLE_PHOTO_LEFT("Vehicle Left Photo"),
    VEHICLE_PHOTO_RIGHT("Vehicle Right Photo"),
    VEHICLE_PHOTO_INTERIOR("Vehicle Interior Photo"),
    BANK_STATEMENT("Bank Statement"),
    PROOF_OF_ADDRESS("Proof of Address"),
    POLICE_CLEARANCE("Police Clearance")
}

@Serializable
enum class DocumentStatus { PENDING, APPROVED, REJECTED }

@Serializable
enum class VerificationStatus { PENDING, IN_REVIEW, APPROVED, REJECTED }

@Serializable
enum class DriverNationality {
    MALAYSIAN,
    FOREIGNER
}

@Serializable
enum class LicenseClass {
    B, B1, B2, D, DA, E, E1, E2, GDL
}

@Serializable
enum class MalaysianState {
    JOHOR,
    KEDAH,
    KELANTAN,
    MELAKA,
    NEGERI_SEMBILAN,
    PAHANG,
    PENANG,
    PERAK,
    PERLIS,
    SABAH,
    SARAWAK,
    SELANGOR,
    TERENGGANU,
    KUALA_LUMPUR,
    LABUAN,
    PUTRAJAYA
}

@Serializable
enum class VehicleOwnership {
    OWNED,
    LEASED,
    COMPANY_PROVIDED
}

@Serializable
enum class InsuranceCoverageType {
    COMPREHENSIVE,
    THIRD_PARTY,
    THIRD_PARTY_FIRE_THEFT
}

// ============================================================
// DELIVERY JOB MODELS
// ============================================================

@Serializable
data class DeliveryJob(
    val id: String = "",
    val displayOrderId: String = "",
    val status: JobStatus = JobStatus.PENDING,
    val pickup: LocationInfo = LocationInfo(),
    val dropoff: LocationInfo = LocationInfo(),
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val packageType: String = "",
    val packageSize: PackageSize = PackageSize.SMALL,
    val estimatedEarnings: Double = 0.0,
    val distance: Double = 0.0,
    val estimatedDuration: Int = 0,
    val createdAt: String? = null,
    val expiresAt: String? = null,
    val scheduledAt: String? = null,
    val acceptedAt: String? = null,
    val pickedUpAt: String? = null,
    val deliveredAt: String? = null,
    val completedAt: String? = null,
    val notes: String? = null,
    val proofOfDelivery: ProofOfDelivery? = null
)

@Serializable
data class DemandZonesResponse(
    val centerLatitude: Double = 0.0,
    val centerLongitude: Double = 0.0,
    val radiusKm: Double = 0.0,
    val vehicleType: String? = null,
    val zones: List<DemandZone> = emptyList()
)

@Serializable
data class DemandZone(
    val id: String = "",
    val centerLatitude: Double = 0.0,
    val centerLongitude: Double = 0.0,
    val radiusKm: Double = 0.0,
    val demandCount: Int = 0,
    val onlineDriverCount: Int = 0,
    val score: Double = 0.0,
    val level: DemandZoneLevel = DemandZoneLevel.LOW,
    val guidance: String = ""
)

@Serializable
enum class DemandZoneLevel {
    LOW,
    MEDIUM,
    HIGH
}

val DeliveryJob.offerExpiryInstant: Instant?
    get() = expiresAt.parseIsoInstantOrNull()
        ?: createdAt.parseIsoInstantOrNull()?.plus(60.seconds)

fun DeliveryJob.remainingOfferMillis(nowEpochMillis: Long): Long {
    val expiryEpochMillis = offerExpiryInstant?.toEpochMilliseconds() ?: return 0L
    return (expiryEpochMillis - nowEpochMillis).coerceAtLeast(0L)
}

private fun String?.parseIsoInstantOrNull(): Instant? {
    val raw = this ?: return null
    return runCatching { Instant.parse(raw) }.getOrNull()
}

val DeliveryJob.displayDurationMinutes: Int
    get() {
        if (estimatedDuration > 0) return estimatedDuration
        if (distance <= 0.0) return 0

        // Fallback estimate for missing backend duration (assumes ~30 km/h city speed).
        return ceil((distance / 30.0) * 60.0).toInt().coerceAtLeast(1)
    }

@Serializable
data class LocationInfo(
    val address: String = "",
    val shortAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
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

val ProofOfDelivery.hasConfirmation: Boolean
    get() = !photoUrl.isNullOrBlank() ||
        !signatureUrl.isNullOrBlank() ||
        !otpCode.isNullOrBlank() ||
        !deliveredAt.isNullOrBlank() ||
        !recipientName.isNullOrBlank()

val DeliveryJob.isSettlementEligible: Boolean
    get() = status == JobStatus.DELIVERED &&
        (!deliveredAt.isNullOrBlank() || !completedAt.isNullOrBlank()) &&
        (proofOfDelivery?.hasConfirmation == true)

@Serializable
data class DeliveryOtpInfo(
    val recipientEmail: String = "",
    val otpSentAt: String? = null,
    val otpExpiresAt: String? = null,
    val resendAvailableAt: String? = null,
    val alreadySent: Boolean = false,
    val adminOtp: String? = null
)

@Serializable
enum class DeliveryLifecycleCommand {
    ARRIVE_PICKUP,
    VERIFY_PICKUP_OTP,
    START_DELIVERY,
    ARRIVE_DROP,
    REQUEST_DROP_OTP,
    COMPLETE_DELIVERY,
    CANCEL_BEFORE_PICKUP
}

@Serializable
data class DeliveryLifecycleLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double? = null,
    val capturedAt: String? = null
)

@Serializable
data class DeliveryLifecycleProof(
    val photoUrl: String? = null,
    val recipientName: String? = null
)

@Serializable
data class DeliveryLifecycleCommandPayload(
    val otp: String? = null,
    val forceResend: Boolean = false,
    val proof: DeliveryLifecycleProof? = null,
    val location: DeliveryLifecycleLocation? = null
)

@Serializable
data class DeliveryLocationEvidence(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracyMeters: Double? = null,
    val capturedAt: String? = null,
    val source: String = "",
    val distanceToExpectedMeters: Double? = null
)

@Serializable
data class DeliveryLifecycleResult(
    val job: DeliveryJob = DeliveryJob(),
    val allowedCommands: List<DeliveryLifecycleCommand> = emptyList(),
    val otpInfo: DeliveryOtpInfo? = null,
    val locationEvidence: DeliveryLocationEvidence? = null,
    val message: String = ""
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
    val bankAccountLast4: String? = null,
    val stripeAccountId: String? = null,
    val stripeDetailsSubmitted: Boolean = false,
    val stripePayoutsEnabled: Boolean = false
)

@Serializable
data class PayoutStatus(
    val accountId: String? = null,
    val detailsSubmitted: Boolean = false,
    val payoutsEnabled: Boolean = false
)

@Serializable
data class PayoutOnboardingLink(
    val url: String = "",
    val expiresAt: Long = 0
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
data class BookingChatMessage(
    val id: String = "",
    val bookingId: String = "",
    val senderId: String = "",
    val senderType: String = "DRIVER",
    val message: String = "",
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val createdAt: String = ""
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

// ============================================================
// MAP & LOCATION MODELS
// ============================================================

@Serializable
data class LatLng(val lat: Double = 0.0, val lng: Double = 0.0)

@Serializable
data class MapConfig(val apiKey: String = "", val styleUrl: String = "", val region: String = "")

@Serializable
data class RouteResult(val distance: Double = 0.0, val duration: Int = 0, val geometry: List<LatLng> = emptyList())

@Serializable
data class CalculateRouteRequest(val originLat: Double, val originLng: Double, val destLat: Double, val destLng: Double)
