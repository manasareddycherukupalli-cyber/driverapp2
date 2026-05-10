package com.company.carryon.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val DRIVER_ONBOARDING_AGREEMENT_VERSION = "driver-partner-v1.0"

@Serializable
data class DriverProfileUpdateRequest(
    val name: String = "",
    val phone: String = "",
    val photo: String? = null,
    val dateOfBirth: String? = null,
    val gender: String = "",
    @SerialName("language") val preferredLanguage: String = "",
    val nationality: DriverNationality? = null,
    val mykadNumber: String? = null,
    val passportNumber: String? = null,
    val passportExpiry: String? = null,
    val plksNumber: String? = null,
    val plksExpiry: String? = null,
    val driversLicenseNumber: String = "",
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
    val duitNowId: String? = null,
    val tngEwalletId: String? = null,
    val lhdnTaxNumber: String? = null,
    val sstNumber: String? = null,
    val pdpaConsent: Boolean = false,
    val backgroundCheckConsent: Boolean = false,
    val agreementVersion: String = DRIVER_ONBOARDING_AGREEMENT_VERSION,
    val noOffencesDeclared: Boolean = false
)

@Serializable
data class DriverVehicleUpsertRequest(
    val type: VehicleType = VehicleType.BIKE,
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val licensePlate: String = "",
    val color: String = "",
    val chassisNumber: String = "",
    val engineNumber: String = "",
    val ownership: VehicleOwnership? = null,
    val ownerName: String? = null,
    val roadTaxExpiry: String? = null,
    val puspakomExpiry: String? = null,
    val apadPermitNumber: String? = null,
    val apadPermitExpiry: String? = null,
    val insurerName: String? = null,
    val insurancePolicyNumber: String? = null,
    val insuranceCoverageType: InsuranceCoverageType? = null,
    val insuranceExpiry: String? = null,
    val hasCommercialCover: Boolean = false
)

@Serializable
data class DriverDocumentSubmissionRequest(
    val imageUrl: String = "",
    val type: DocumentType = DocumentType.DRIVERS_LICENSE,
    val expiryDate: String? = null
)

@Serializable
data class DriverVerificationStatusPayload(
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val isVerified: Boolean = false,
    val documents: List<Document> = emptyList()
)

@Serializable
data class UploadedDocumentAsset(
    val type: DocumentType,
    val imageUrl: String,
    val expiryDate: String? = null
)

@Serializable
data class DriverOnboardingDraft(
    val phone: String = "",
    val nationality: DriverNationality? = null,
    val mykadNumber: String = "",
    val passportNumber: String = "",
    val passportExpiry: String = "",
    val plksNumber: String = "",
    val plksExpiry: String = "",
    val identityDocuments: List<UploadedDocumentAsset> = emptyList(),
    val fullName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val postcode: String = "",
    val state: MalaysianState? = null,
    val emergencyContactName: String = "",
    val emergencyContactRelation: String = "",
    val emergencyContactPhone: String = "",
    val driversLicenseNumber: String = "",
    val licenseClass: LicenseClass? = null,
    val licenseExpiry: String = "",
    val driversLicenseFrontUrl: String = "",
    val driversLicenseBackUrl: String = "",
    val hasGDL: Boolean = false,
    val gdlExpiry: String = "",
    val gdlUrl: String = "",
    val vehicleType: VehicleType? = null,
    val vehicleMake: String = "",
    val vehicleModel: String = "",
    val vehicleYear: String = "",
    val vehiclePlate: String = "",
    val vehicleColor: String = "",
    val chassisNumber: String = "",
    val engineNumber: String = "",
    val vehicleOwnership: VehicleOwnership? = null,
    val ownerName: String = "",
    val vehicleRegistrationUrl: String = "",
    val roadTaxUrl: String = "",
    val roadTaxExpiry: String = "",
    val puspakomUrl: String = "",
    val puspakomExpiry: String = "",
    val apadPermitUrl: String = "",
    val apadPermitNumber: String = "",
    val apadPermitExpiry: String = "",
    val vehicleFrontUrl: String = "",
    val vehicleBackUrl: String = "",
    val vehicleLeftUrl: String = "",
    val vehicleRightUrl: String = "",
    val vehicleInteriorUrl: String = "",
    val insurerName: String = "",
    val insurancePolicyNumber: String = "",
    val insuranceCoverageType: InsuranceCoverageType? = null,
    val insuranceExpiry: String = "",
    val insuranceUrl: String = "",
    val hasCommercialCover: Boolean = false,
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankAccountHolder: String = "",
    val bankStatementUrl: String = "",
    val duitNowId: String = "",
    val tngEwalletId: String = "",
    val lhdnTaxNumber: String = "",
    val sstNumber: String = "",
    val pdpaConsent: Boolean = false,
    val backgroundCheckConsent: Boolean = false,
    val noOffencesDeclared: Boolean = false,
    val policeClearanceUrl: String = "",
    val agreementAccepted: Boolean = false,
    val completedSteps: Set<Int> = emptySet(),
    val currentStep: Int = 1,
    val lastSavedAtEpochMs: Long = 0L
)

@Serializable
data class ValidationMessage(
    val field: String,
    val message: String,
    val isWarning: Boolean = false
)
