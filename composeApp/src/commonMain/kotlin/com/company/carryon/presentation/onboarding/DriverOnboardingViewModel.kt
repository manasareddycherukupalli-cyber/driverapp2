package com.company.carryon.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.Document
import com.company.carryon.data.model.DocumentType
import com.company.carryon.data.model.Driver
import com.company.carryon.data.model.DriverDocumentSubmissionRequest
import com.company.carryon.data.model.DriverNationality
import com.company.carryon.data.model.DriverOnboardingDraft
import com.company.carryon.data.model.DriverProfileUpdateRequest
import com.company.carryon.data.model.DriverVerificationStatusPayload
import com.company.carryon.data.model.DriverVehicleUpsertRequest
import com.company.carryon.data.model.InsuranceCoverageType
import com.company.carryon.data.model.LicenseClass
import com.company.carryon.data.model.MalaysianState
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.UploadedDocumentAsset
import com.company.carryon.data.model.ValidationMessage
import com.company.carryon.data.model.VehicleDetails
import com.company.carryon.data.model.VehicleOwnership
import com.company.carryon.data.model.VehicleType
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.data.network.clearOnboardingDraft
import com.company.carryon.data.network.getOnboardingDraft
import com.company.carryon.data.network.saveOnboardingDraft
import com.company.carryon.di.ServiceLocator
import com.company.carryon.presentation.auth.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.time.Clock

class DriverOnboardingViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val api = ServiceLocator.driverOnboardingApi
    private val storage = ServiceLocator.driverDocumentsStorage
    private val authRepository = ServiceLocator.authRepository
    private val json = HttpClientFactory.json

    private val _initialLoadState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val initialLoadState: StateFlow<UiState<Unit>> = _initialLoadState.asStateFlow()

    private val _draftState = MutableStateFlow(DriverOnboardingDraft())
    val draftState: StateFlow<DriverOnboardingDraft> = _draftState.asStateFlow()

    private val _documentUploadState = MutableStateFlow<UiState<DocumentType>>(UiState.Idle)
    val documentUploadState: StateFlow<UiState<DocumentType>> = _documentUploadState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    private val _verificationState =
        MutableStateFlow<UiState<DriverVerificationStatusPayload>>(UiState.Idle)
    val verificationState: StateFlow<UiState<DriverVerificationStatusPayload>> = _verificationState.asStateFlow()

    private val _serverDocuments = MutableStateFlow<Map<DocumentType, Document>>(emptyMap())
    val serverDocuments: StateFlow<Map<DocumentType, Document>> = _serverDocuments.asStateFlow()

    private var initializedDriverId: String? = null

    fun initialize(force: Boolean = false) {
        val driverId = currentDriverId() ?: return
        if (!force && initializedDriverId == driverId && _initialLoadState.value !is UiState.Error) return

        initializedDriverId = driverId
        viewModelScope.launch {
            _initialLoadState.value = UiState.Loading

            val localDraft = loadLocalDraft(driverId)
            val profile = api.getProfile().getOrNull() ?: authViewModel.latestAuthResponse.value?.driver
            val vehicle = api.getVehicle().getOrNull()
            val verification = api.getVerificationStatus().getOrNull()

            _verificationState.value = verification?.let { UiState.Success(it) } ?: UiState.Idle
            _serverDocuments.value = verification?.documents.orEmpty().associateBy { it.type }

            val merged = hydrateDraft(localDraft, profile, vehicle, verification)
            val completedSteps = computeCompletedSteps(merged)
            val resumedStep = resolveCurrentStep(merged.currentStep, completedSteps)
            val resolvedDraft = merged.copy(
                completedSteps = completedSteps,
                currentStep = resumedStep
            )
            _draftState.value = resolvedDraft
            persistDraft(resolvedDraft)
            _initialLoadState.value = UiState.Success(Unit)
        }
    }

    fun updateDraft(transform: (DriverOnboardingDraft) -> DriverOnboardingDraft) {
        val updated = transform(_draftState.value)
        val recalculated = updated.copy(
            completedSteps = computeCompletedSteps(updated)
        )
        _draftState.value = recalculated
        persistDraft(recalculated)
    }

    fun setCurrentStep(step: Int) {
        val target = step.coerceIn(1, TOTAL_STEPS)
        if (!canOpenStep(target, _draftState.value.completedSteps)) return
        updateDraft { it.copy(currentStep = target) }
    }

    fun continueCurrentStep(): List<ValidationMessage> {
        val draft = _draftState.value
        val errors = validationMessagesForStep(draft.currentStep, draft).filterNot { it.isWarning }
        if (errors.isNotEmpty()) return errors

        val completed = draft.completedSteps + draft.currentStep
        val nextStep = (draft.currentStep + 1).coerceAtMost(TOTAL_STEPS)
        val updated = draft.copy(
            completedSteps = completed,
            currentStep = nextStep
        )
        _draftState.value = updated
        persistDraft(updated)
        return emptyList()
    }

    fun goBack() {
        val previous = (_draftState.value.currentStep - 1).coerceAtLeast(1)
        updateDraft { it.copy(currentStep = previous) }
    }

    fun validationMessagesForCurrentStep(): List<ValidationMessage> {
        return validationMessagesForStep(_draftState.value.currentStep, _draftState.value)
    }

    fun validationMessagesForStep(step: Int, draft: DriverOnboardingDraft = _draftState.value): List<ValidationMessage> {
        val messages = mutableListOf<ValidationMessage>()
        when (step) {
            1 -> {
                if (!PHONE_REGEX.matches(draft.phone.trim())) {
                    messages += ValidationMessage("phone", "Enter a valid phone number.")
                }
            }
            2 -> {
                if (draft.nationality == null) {
                    messages += ValidationMessage("nationality", "Select your nationality.")
                }
            }
            3 -> {
                when (draft.nationality) {
                    DriverNationality.MALAYSIAN -> {
                        if (!isValidMyKad(draft.mykadNumber)) {
                            messages += ValidationMessage("mykadNumber", "Enter a valid 12-digit MyKad number.")
                        }
                        if (identityUrl(draft, DocumentType.MYKAD_FRONT).isBlank()) {
                            messages += ValidationMessage("mykadFront", "Upload MyKad front.")
                        }
                        if (identityUrl(draft, DocumentType.MYKAD_BACK).isBlank()) {
                            messages += ValidationMessage("mykadBack", "Upload MyKad back.")
                        }
                        if (identityUrl(draft, DocumentType.SELFIE).isBlank()) {
                            messages += ValidationMessage("selfie", "Upload a selfie.")
                        }
                    }
                    DriverNationality.FOREIGNER -> {
                        if (draft.passportNumber.isBlank()) {
                            messages += ValidationMessage("passportNumber", "Enter your passport number.")
                        }
                        if (draft.passportExpiry.isBlank()) {
                            messages += ValidationMessage("passportExpiry", "Select passport expiry date.")
                        } else if (isExpired(draft.passportExpiry)) {
                            messages += ValidationMessage("passportExpiry", "Passport is expired.", isWarning = true)
                        }
                        if (draft.plksNumber.isBlank()) {
                            messages += ValidationMessage("plksNumber", "Enter your PLKS number.")
                        }
                        if (draft.plksExpiry.isBlank()) {
                            messages += ValidationMessage("plksExpiry", "Select PLKS expiry date.")
                        } else if (isExpired(draft.plksExpiry)) {
                            messages += ValidationMessage("plksExpiry", "PLKS is expired.", isWarning = true)
                        }
                        if (identityUrl(draft, DocumentType.PASSPORT).isBlank()) {
                            messages += ValidationMessage("passport", "Upload passport photo.")
                        }
                        if (identityUrl(draft, DocumentType.WORK_PERMIT_PLKS).isBlank()) {
                            messages += ValidationMessage("plks", "Upload PLKS / work permit.")
                        }
                    }
                    null -> messages += ValidationMessage("nationality", "Select your nationality first.")
                }
            }
            4 -> {
                if (draft.fullName.isBlank()) messages += ValidationMessage("fullName", "Enter your full name.")
                if (draft.dateOfBirth.isBlank()) messages += ValidationMessage("dateOfBirth", "Select your date of birth.")
                if (draft.gender.isBlank()) messages += ValidationMessage("gender", "Select your gender.")
                if (draft.addressLine1.isBlank()) messages += ValidationMessage("addressLine1", "Enter address line 1.")
                if (draft.city.isBlank()) messages += ValidationMessage("city", "Enter your city.")
                if (!POSTCODE_REGEX.matches(draft.postcode.trim())) {
                    messages += ValidationMessage("postcode", "Enter a valid 5-digit postcode.")
                }
                if (draft.state == null) messages += ValidationMessage("state", "Select your state.")
                if (draft.emergencyContactName.isBlank()) {
                    messages += ValidationMessage("emergencyContactName", "Enter emergency contact name.")
                }
                if (draft.emergencyContactRelation.isBlank()) {
                    messages += ValidationMessage("emergencyContactRelation", "Enter emergency contact relation.")
                }
                if (!PHONE_REGEX.matches(draft.emergencyContactPhone.trim())) {
                    messages += ValidationMessage("emergencyContactPhone", "Enter a valid emergency contact phone.")
                }
            }
            5 -> {
                if (draft.licenseClass == null) messages += ValidationMessage("licenseClass", "Select license class.")
                if (draft.driversLicenseNumber.isBlank()) {
                    messages += ValidationMessage("driversLicenseNumber", "Enter driver license number.")
                }
                if (draft.licenseExpiry.isBlank()) {
                    messages += ValidationMessage("licenseExpiry", "Select driver license expiry.")
                } else if (isExpired(draft.licenseExpiry)) {
                    messages += ValidationMessage("licenseExpiry", "Driver license is expired.", isWarning = true)
                }
                if (draft.driversLicenseFrontUrl.isBlank()) {
                    messages += ValidationMessage("driversLicenseFront", "Upload license front photo.")
                }
                if (draft.driversLicenseBackUrl.isBlank()) {
                    messages += ValidationMessage("driversLicenseBack", "Upload license back photo.")
                }
                if (draft.hasGDL) {
                    if (draft.gdlExpiry.isBlank()) {
                        messages += ValidationMessage("gdlExpiry", "Select GDL expiry date.")
                    } else if (isExpired(draft.gdlExpiry)) {
                        messages += ValidationMessage("gdlExpiry", "GDL is expired.", isWarning = true)
                    }
                    if (draft.gdlUrl.isBlank()) {
                        messages += ValidationMessage("gdlUrl", "Upload GDL photo.")
                    }
                }
            }
            6 -> {
                if (draft.vehicleType == null) messages += ValidationMessage("vehicleType", "Select vehicle type.")
                if (draft.vehicleMake.isBlank()) messages += ValidationMessage("vehicleMake", "Enter vehicle make.")
                if (draft.vehicleModel.isBlank()) messages += ValidationMessage("vehicleModel", "Enter vehicle model.")
                val year = draft.vehicleYear.toIntOrNull()
                if (year == null || year !in 1980..(currentYear() + 1)) {
                    messages += ValidationMessage("vehicleYear", "Enter a valid vehicle year.")
                }
                if (draft.vehiclePlate.isBlank()) messages += ValidationMessage("vehiclePlate", "Enter license plate.")
                if (draft.vehicleColor.isBlank()) messages += ValidationMessage("vehicleColor", "Enter vehicle color.")
                if (draft.vehicleOwnership == null) messages += ValidationMessage("vehicleOwnership", "Select ownership.")
                if (draft.vehicleOwnership == VehicleOwnership.LEASED && draft.ownerName.isBlank()) {
                    messages += ValidationMessage("ownerName", "Enter owner name for leased vehicle.")
                }
            }
            7 -> {
                if (draft.vehicleRegistrationUrl.isBlank()) {
                    messages += ValidationMessage("vehicleRegistration", "Upload vehicle registration.")
                }
                if (draft.roadTaxUrl.isBlank()) {
                    messages += ValidationMessage("roadTax", "Upload road tax.")
                }
                if (draft.roadTaxExpiry.isBlank()) {
                    messages += ValidationMessage("roadTaxExpiry", "Select road tax expiry.")
                } else if (isExpired(draft.roadTaxExpiry)) {
                    messages += ValidationMessage("roadTaxExpiry", "Road tax is expired.", isWarning = true)
                }
                if (isCommercialVehicle(draft.vehicleType)) {
                    if (draft.puspakomUrl.isBlank()) {
                        messages += ValidationMessage("puspakom", "Upload PUSPAKOM certificate.")
                    }
                    if (draft.puspakomExpiry.isBlank()) {
                        messages += ValidationMessage("puspakomExpiry", "Select PUSPAKOM expiry.")
                    } else if (isExpired(draft.puspakomExpiry)) {
                        messages += ValidationMessage("puspakomExpiry", "PUSPAKOM is expired.", isWarning = true)
                    }
                    if (draft.apadPermitUrl.isBlank()) {
                        messages += ValidationMessage("apadPermit", "Upload APAD / LPKP permit.")
                    }
                    if (draft.apadPermitNumber.isBlank()) {
                        messages += ValidationMessage("apadPermitNumber", "Enter APAD / LPKP permit number.")
                    }
                    if (draft.apadPermitExpiry.isBlank()) {
                        messages += ValidationMessage("apadPermitExpiry", "Select APAD / LPKP permit expiry.")
                    } else if (isExpired(draft.apadPermitExpiry)) {
                        messages += ValidationMessage("apadPermitExpiry", "APAD / LPKP permit is expired.", isWarning = true)
                    }
                }
            }
            8 -> {
                if (draft.vehicleFrontUrl.isBlank()) messages += ValidationMessage("vehicleFront", "Upload front vehicle photo.")
                if (draft.vehicleBackUrl.isBlank()) messages += ValidationMessage("vehicleBack", "Upload back vehicle photo.")
                if (draft.vehicleLeftUrl.isBlank()) messages += ValidationMessage("vehicleLeft", "Upload left vehicle photo.")
                if (draft.vehicleRightUrl.isBlank()) messages += ValidationMessage("vehicleRight", "Upload right vehicle photo.")
                if (draft.vehicleInteriorUrl.isBlank()) messages += ValidationMessage("vehicleInterior", "Upload interior vehicle photo.")
            }
            9 -> {
                if (draft.insurerName.isBlank()) messages += ValidationMessage("insurerName", "Enter insurer name.")
                if (draft.insurancePolicyNumber.isBlank()) messages += ValidationMessage("insurancePolicyNumber", "Enter policy number.")
                if (draft.insuranceCoverageType == null) messages += ValidationMessage("insuranceCoverageType", "Select coverage type.")
                if (draft.insuranceExpiry.isBlank()) {
                    messages += ValidationMessage("insuranceExpiry", "Select insurance expiry.")
                } else if (isExpired(draft.insuranceExpiry)) {
                    messages += ValidationMessage("insuranceExpiry", "Insurance is expired.", isWarning = true)
                }
                if (draft.insuranceUrl.isBlank()) messages += ValidationMessage("insuranceUrl", "Upload insurance document.")
            }
            10 -> {
                if (draft.bankName.isBlank()) messages += ValidationMessage("bankName", "Select bank.")
                if (draft.bankAccountNumber.isBlank()) messages += ValidationMessage("bankAccountNumber", "Enter bank account number.")
                if (draft.bankAccountHolder.isBlank()) messages += ValidationMessage("bankAccountHolder", "Enter account holder name.")
                if (draft.bankStatementUrl.isBlank()) messages += ValidationMessage("bankStatement", "Upload bank statement.")
                if (draft.fullName.isNotBlank() &&
                    draft.bankAccountHolder.isNotBlank() &&
                    !matchesNameSoft(draft.fullName, draft.bankAccountHolder)
                ) {
                    messages += ValidationMessage("bankAccountHolder", "Account holder should match the driver's MyKad name.", isWarning = true)
                }
            }
            11 -> Unit
            12 -> {
                if (!draft.pdpaConsent) messages += ValidationMessage("pdpaConsent", "PDPA consent is required.")
                if (!draft.backgroundCheckConsent) messages += ValidationMessage("backgroundCheckConsent", "Background check consent is required.")
                if (!draft.noOffencesDeclared) messages += ValidationMessage("noOffencesDeclared", "You must declare no disqualifying offences.")
                if (draft.policeClearanceUrl.isBlank()) messages += ValidationMessage("policeClearance", "Upload police clearance.")
            }
            13 -> {
                if (!draft.agreementAccepted) {
                    messages += ValidationMessage("agreementAccepted", "Accept the partner terms to submit.")
                }
            }
        }
        return messages
    }

    fun uploadDocument(type: DocumentType, bytes: ByteArray, expiryDate: String? = null) {
        val driverId = currentDriverId() ?: return
        viewModelScope.launch {
            _documentUploadState.value = UiState.Loading
            storage.uploadDocument(driverId, type, bytes)
                .onSuccess { url ->
                    updateDraft { applyUploadedDocument(it, type, url, expiryDate) }
                    _documentUploadState.value = UiState.Success(type)
                }
                .onFailure {
                    _documentUploadState.value = UiState.Error(it.message ?: "Failed to upload ${type.displayName}.")
                }
        }
    }

    fun refreshVerificationStatus() {
        viewModelScope.launch {
            _verificationState.value = UiState.Loading
            api.getVerificationStatus()
                .onSuccess {
                    _serverDocuments.value = it.documents.associateBy { document -> document.type }
                    _verificationState.value = UiState.Success(it)
                }
                .onFailure {
                    _verificationState.value = UiState.Error(it.message ?: "Failed to load verification status.")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            currentDriverId()?.let { clearOnboardingDraft(it) }
            authRepository.logout()
        }
    }

    fun submit() {
        val draft = _draftState.value
        val blockingErrors = (1..TOTAL_STEPS).associateWith { step ->
            validationMessagesForStep(step, draft).filterNot { it.isWarning }
        }.filterValues { it.isNotEmpty() }

        if (blockingErrors.isNotEmpty()) {
            val firstInvalid = blockingErrors.keys.minOrNull() ?: 1
            updateDraft { it.copy(currentStep = firstInvalid) }
            _submitState.value = UiState.Error(blockingErrors[firstInvalid]?.firstOrNull()?.message ?: "Complete required fields.")
            return
        }

        viewModelScope.launch {
            _submitState.value = UiState.Loading
            val profileRequest = draft.toProfileRequest()
            val vehicleRequest = draft.toVehicleRequest()
            val documentRequests = buildDocumentRequests(draft)

            val result = runCatching {
                api.updateProfile(profileRequest).getOrThrow()
                api.updateVehicle(vehicleRequest).getOrThrow()
                documentRequests.forEach { api.submitDocument(it).getOrThrow() }
                api.getVerificationStatus().getOrThrow()
            }

            result.onSuccess { verification ->
                currentDriverId()?.let { clearOnboardingDraft(it) }
                _serverDocuments.value = verification.documents.associateBy { it.type }
                _verificationState.value = UiState.Success(verification)
                _submitState.value = UiState.Success(Unit)
            }.onFailure {
                _submitState.value = UiState.Error(it.message ?: "Failed to submit onboarding.")
            }
        }
    }

    fun canOpenStep(step: Int, completedSteps: Set<Int> = _draftState.value.completedSteps): Boolean {
        if (step <= 1) return true
        return (1 until step).all { it in completedSteps }
    }

    fun serverDocument(type: DocumentType): Document? = _serverDocuments.value[type]

    private fun currentDriverId(): String? {
        return authViewModel.latestAuthResponse.value?.driver?.id?.takeIf { it.isNotBlank() }
    }

    private fun loadLocalDraft(driverId: String): DriverOnboardingDraft? {
        val payload = getOnboardingDraft(driverId) ?: return null
        return runCatching { json.decodeFromString<DriverOnboardingDraft>(payload) }.getOrNull()
    }

    private fun persistDraft(draft: DriverOnboardingDraft) {
        val driverId = currentDriverId() ?: return
        val payload = draft.copy(lastSavedAtEpochMs = Clock.System.now().toEpochMilliseconds())
        saveOnboardingDraft(driverId, json.encodeToString(payload))
    }

    private fun hydrateDraft(
        localDraft: DriverOnboardingDraft?,
        profile: Driver?,
        vehicle: VehicleDetails?,
        verification: DriverVerificationStatusPayload?
    ): DriverOnboardingDraft {
        val serverDocs = verification?.documents.orEmpty().associateBy { it.type }
        val draft = localDraft ?: DriverOnboardingDraft()

        return draft.copy(
            phone = draft.phone.ifBlank { profile?.phone.orEmpty() },
            nationality = draft.nationality ?: profile?.nationality,
            mykadNumber = draft.mykadNumber.ifBlank { profile?.mykadNumber.orEmpty() },
            passportNumber = draft.passportNumber.ifBlank { profile?.passportNumber.orEmpty() },
            passportExpiry = draft.passportExpiry.ifBlank { profile?.passportExpiry.orEmpty() },
            plksNumber = draft.plksNumber.ifBlank { profile?.plksNumber.orEmpty() },
            plksExpiry = draft.plksExpiry.ifBlank { profile?.plksExpiry.orEmpty() },
            identityDocuments = mergeIdentityDocuments(draft.identityDocuments, serverDocs),
            fullName = draft.fullName.ifBlank { profile?.name.orEmpty() },
            dateOfBirth = draft.dateOfBirth.ifBlank { profile?.dateOfBirth.orEmpty() },
            gender = draft.gender.ifBlank { profile?.gender.orEmpty() },
            addressLine1 = draft.addressLine1.ifBlank { profile?.addressLine1.orEmpty() },
            addressLine2 = draft.addressLine2.ifBlank { profile?.addressLine2.orEmpty() },
            city = draft.city.ifBlank { profile?.city.orEmpty() },
            postcode = draft.postcode.ifBlank { profile?.postcode.orEmpty() },
            state = draft.state ?: profile?.state,
            emergencyContactName = draft.emergencyContactName.ifBlank { profile?.emergencyContactName.orEmpty() },
            emergencyContactRelation = draft.emergencyContactRelation.ifBlank { profile?.emergencyContactRelation.orEmpty() },
            emergencyContactPhone = draft.emergencyContactPhone.ifBlank { profile?.emergencyContactPhone.orEmpty() },
            driversLicenseNumber = draft.driversLicenseNumber.ifBlank {
                profile?.driversLicenseNumber.orEmpty()
            },
            licenseClass = draft.licenseClass ?: profile?.licenseClass,
            licenseExpiry = draft.licenseExpiry.ifBlank { profile?.licenseExpiry.orEmpty() },
            driversLicenseFrontUrl = draft.driversLicenseFrontUrl.ifBlank { serverDocs[DocumentType.DRIVERS_LICENSE]?.imageUrl.orEmpty() },
            driversLicenseBackUrl = draft.driversLicenseBackUrl.ifBlank { serverDocs[DocumentType.DRIVERS_LICENSE_BACK]?.imageUrl.orEmpty() },
            hasGDL = draft.hasGDL || profile?.hasGDL == true,
            gdlExpiry = draft.gdlExpiry.ifBlank { profile?.gdlExpiry.orEmpty() },
            gdlUrl = draft.gdlUrl.ifBlank { serverDocs[DocumentType.GDL]?.imageUrl.orEmpty() },
            vehicleType = draft.vehicleType ?: vehicle?.type,
            vehicleMake = draft.vehicleMake.ifBlank { vehicle?.make.orEmpty() },
            vehicleModel = draft.vehicleModel.ifBlank { vehicle?.model.orEmpty() },
            vehicleYear = draft.vehicleYear.ifBlank { vehicle?.year?.takeIf { it > 0 }?.toString().orEmpty() },
            vehiclePlate = draft.vehiclePlate.ifBlank { vehicle?.licensePlate.orEmpty() },
            vehicleColor = draft.vehicleColor.ifBlank { vehicle?.color.orEmpty() },
            chassisNumber = draft.chassisNumber.ifBlank { vehicle?.chassisNumber.orEmpty() },
            engineNumber = draft.engineNumber.ifBlank { vehicle?.engineNumber.orEmpty() },
            vehicleOwnership = draft.vehicleOwnership ?: vehicle?.ownership,
            ownerName = draft.ownerName.ifBlank { vehicle?.ownerName.orEmpty() },
            vehicleRegistrationUrl = draft.vehicleRegistrationUrl.ifBlank { serverDocs[DocumentType.VEHICLE_REGISTRATION]?.imageUrl.orEmpty() },
            roadTaxUrl = draft.roadTaxUrl.ifBlank { serverDocs[DocumentType.ROAD_TAX]?.imageUrl.orEmpty() },
            roadTaxExpiry = draft.roadTaxExpiry.ifBlank { vehicle?.roadTaxExpiry.orEmpty() },
            puspakomUrl = draft.puspakomUrl.ifBlank { serverDocs[DocumentType.PUSPAKOM]?.imageUrl.orEmpty() },
            puspakomExpiry = draft.puspakomExpiry.ifBlank { vehicle?.puspakomExpiry.orEmpty() },
            apadPermitUrl = draft.apadPermitUrl.ifBlank { serverDocs[DocumentType.APAD_PERMIT]?.imageUrl.orEmpty() },
            apadPermitNumber = draft.apadPermitNumber.ifBlank { vehicle?.apadPermitNumber.orEmpty() },
            apadPermitExpiry = draft.apadPermitExpiry.ifBlank { vehicle?.apadPermitExpiry.orEmpty() },
            vehicleFrontUrl = draft.vehicleFrontUrl.ifBlank { serverDocs[DocumentType.VEHICLE_PHOTO_FRONT]?.imageUrl.orEmpty() },
            vehicleBackUrl = draft.vehicleBackUrl.ifBlank { serverDocs[DocumentType.VEHICLE_PHOTO_BACK]?.imageUrl.orEmpty() },
            vehicleLeftUrl = draft.vehicleLeftUrl.ifBlank { serverDocs[DocumentType.VEHICLE_PHOTO_LEFT]?.imageUrl.orEmpty() },
            vehicleRightUrl = draft.vehicleRightUrl.ifBlank { serverDocs[DocumentType.VEHICLE_PHOTO_RIGHT]?.imageUrl.orEmpty() },
            vehicleInteriorUrl = draft.vehicleInteriorUrl.ifBlank { serverDocs[DocumentType.VEHICLE_PHOTO_INTERIOR]?.imageUrl.orEmpty() },
            insurerName = draft.insurerName.ifBlank { vehicle?.insurerName.orEmpty() },
            insurancePolicyNumber = draft.insurancePolicyNumber.ifBlank { vehicle?.insurancePolicyNumber.orEmpty() },
            insuranceCoverageType = draft.insuranceCoverageType ?: vehicle?.insuranceCoverageType,
            insuranceExpiry = draft.insuranceExpiry.ifBlank { vehicle?.insuranceExpiry.orEmpty() },
            insuranceUrl = draft.insuranceUrl.ifBlank { serverDocs[DocumentType.INSURANCE]?.imageUrl.orEmpty() },
            hasCommercialCover = draft.hasCommercialCover || vehicle?.hasCommercialCover == true,
            bankName = draft.bankName.ifBlank { profile?.bankName.orEmpty() },
            bankAccountNumber = draft.bankAccountNumber.ifBlank { profile?.bankAccountNumber.orEmpty() },
            bankAccountHolder = draft.bankAccountHolder.ifBlank { profile?.bankAccountHolder.orEmpty() },
            bankStatementUrl = draft.bankStatementUrl.ifBlank { serverDocs[DocumentType.BANK_STATEMENT]?.imageUrl.orEmpty() },
            duitNowId = draft.duitNowId.ifBlank { profile?.duitNowId.orEmpty() },
            tngEwalletId = draft.tngEwalletId.ifBlank { profile?.tngEwalletId.orEmpty() },
            lhdnTaxNumber = draft.lhdnTaxNumber.ifBlank { profile?.lhdnTaxNumber.orEmpty() },
            sstNumber = draft.sstNumber.ifBlank { profile?.sstNumber.orEmpty() },
            pdpaConsent = draft.pdpaConsent || profile?.pdpaConsent == true,
            backgroundCheckConsent = draft.backgroundCheckConsent || profile?.backgroundCheckConsent == true,
            noOffencesDeclared = draft.noOffencesDeclared || profile?.noOffencesDeclared == true,
            policeClearanceUrl = draft.policeClearanceUrl.ifBlank { serverDocs[DocumentType.POLICE_CLEARANCE]?.imageUrl.orEmpty() }
        )
    }

    private fun resolveCurrentStep(savedStep: Int, completedSteps: Set<Int>): Int {
        val normalized = savedStep.coerceIn(1, TOTAL_STEPS)
        return if (canOpenStep(normalized, completedSteps)) normalized else firstIncompleteStep(completedSteps)
    }

    private fun firstIncompleteStep(completedSteps: Set<Int>): Int {
        return (1..TOTAL_STEPS).firstOrNull { it !in completedSteps } ?: TOTAL_STEPS
    }

    private fun computeCompletedSteps(draft: DriverOnboardingDraft): Set<Int> {
        return (1 until TOTAL_STEPS).filterTo(linkedSetOf()) { step ->
            validationMessagesForStep(step, draft).none { !it.isWarning }
        }
    }

    private fun applyUploadedDocument(
        draft: DriverOnboardingDraft,
        type: DocumentType,
        url: String,
        expiryDate: String?
    ): DriverOnboardingDraft {
        return when (type) {
            DocumentType.MYKAD_FRONT,
            DocumentType.MYKAD_BACK,
            DocumentType.SELFIE,
            DocumentType.PASSPORT,
            DocumentType.WORK_PERMIT_PLKS -> {
                val byType = draft.identityDocuments.associateBy { it.type }.toMutableMap()
                byType[type] = UploadedDocumentAsset(type, url, expiryDate)
                draft.copy(identityDocuments = byType.values.toList())
            }
            DocumentType.DRIVERS_LICENSE -> draft.copy(driversLicenseFrontUrl = url)
            DocumentType.DRIVERS_LICENSE_BACK -> draft.copy(driversLicenseBackUrl = url)
            DocumentType.GDL -> draft.copy(gdlUrl = url)
            DocumentType.VEHICLE_REGISTRATION -> draft.copy(vehicleRegistrationUrl = url)
            DocumentType.ROAD_TAX -> draft.copy(roadTaxUrl = url, roadTaxExpiry = expiryDate ?: draft.roadTaxExpiry)
            DocumentType.PUSPAKOM -> draft.copy(puspakomUrl = url, puspakomExpiry = expiryDate ?: draft.puspakomExpiry)
            DocumentType.APAD_PERMIT -> draft.copy(apadPermitUrl = url, apadPermitExpiry = expiryDate ?: draft.apadPermitExpiry)
            DocumentType.VEHICLE_PHOTO_FRONT -> draft.copy(vehicleFrontUrl = url)
            DocumentType.VEHICLE_PHOTO_BACK -> draft.copy(vehicleBackUrl = url)
            DocumentType.VEHICLE_PHOTO_LEFT -> draft.copy(vehicleLeftUrl = url)
            DocumentType.VEHICLE_PHOTO_RIGHT -> draft.copy(vehicleRightUrl = url)
            DocumentType.VEHICLE_PHOTO_INTERIOR -> draft.copy(vehicleInteriorUrl = url)
            DocumentType.INSURANCE -> draft.copy(insuranceUrl = url, insuranceExpiry = expiryDate ?: draft.insuranceExpiry)
            DocumentType.BANK_STATEMENT -> draft.copy(bankStatementUrl = url)
            DocumentType.POLICE_CLEARANCE -> draft.copy(policeClearanceUrl = url)
            else -> draft
        }
    }

    private fun mergeIdentityDocuments(
        localDocuments: List<UploadedDocumentAsset>,
        serverDocuments: Map<DocumentType, Document>
    ): List<UploadedDocumentAsset> {
        val merged = localDocuments.associateBy { it.type }.toMutableMap()
        IDENTITY_DOCUMENT_TYPES.forEach { type ->
            if (merged[type]?.imageUrl.isNullOrBlank()) {
                serverDocuments[type]?.let { document ->
                    merged[type] = UploadedDocumentAsset(
                        type = type,
                        imageUrl = document.imageUrl,
                        expiryDate = document.expiryDate
                    )
                }
            }
        }
        return merged.values.toList()
    }

    private fun identityUrl(draft: DriverOnboardingDraft, type: DocumentType): String {
        return draft.identityDocuments.firstOrNull { it.type == type }?.imageUrl.orEmpty()
    }

    private fun buildDocumentRequests(draft: DriverOnboardingDraft): List<DriverDocumentSubmissionRequest> {
        val requests = mutableListOf<DriverDocumentSubmissionRequest>()

        draft.identityDocuments.forEach { asset ->
            if (asset.imageUrl.isNotBlank()) {
                requests += DriverDocumentSubmissionRequest(
                    imageUrl = asset.imageUrl,
                    type = asset.type,
                    expiryDate = asset.expiryDate
                )
            }
        }

        fun add(type: DocumentType, url: String, expiry: String? = null) {
            if (url.isNotBlank()) {
                requests += DriverDocumentSubmissionRequest(
                    imageUrl = url,
                    type = type,
                    expiryDate = expiry?.takeIf { it.isNotBlank() }
                )
            }
        }

        add(DocumentType.DRIVERS_LICENSE, draft.driversLicenseFrontUrl, draft.licenseExpiry)
        add(DocumentType.DRIVERS_LICENSE_BACK, draft.driversLicenseBackUrl, draft.licenseExpiry)
        if (draft.hasGDL) add(DocumentType.GDL, draft.gdlUrl, draft.gdlExpiry)
        add(DocumentType.VEHICLE_REGISTRATION, draft.vehicleRegistrationUrl)
        add(DocumentType.ROAD_TAX, draft.roadTaxUrl, draft.roadTaxExpiry)
        add(DocumentType.PUSPAKOM, draft.puspakomUrl, draft.puspakomExpiry)
        add(DocumentType.APAD_PERMIT, draft.apadPermitUrl, draft.apadPermitExpiry)
        add(DocumentType.VEHICLE_PHOTO_FRONT, draft.vehicleFrontUrl)
        add(DocumentType.VEHICLE_PHOTO_BACK, draft.vehicleBackUrl)
        add(DocumentType.VEHICLE_PHOTO_LEFT, draft.vehicleLeftUrl)
        add(DocumentType.VEHICLE_PHOTO_RIGHT, draft.vehicleRightUrl)
        add(DocumentType.VEHICLE_PHOTO_INTERIOR, draft.vehicleInteriorUrl)
        add(DocumentType.INSURANCE, draft.insuranceUrl, draft.insuranceExpiry)
        add(DocumentType.BANK_STATEMENT, draft.bankStatementUrl)
        add(DocumentType.POLICE_CLEARANCE, draft.policeClearanceUrl)
        return requests.distinctBy { it.type }
    }

    private fun DriverOnboardingDraft.toProfileRequest(): DriverProfileUpdateRequest {
        val selfieUrl = identityDocuments.firstOrNull { it.type == DocumentType.SELFIE }?.imageUrl
        return DriverProfileUpdateRequest(
            name = fullName,
            phone = phone,
            photo = selfieUrl,
            dateOfBirth = dateOfBirth,
            gender = gender,
            preferredLanguage = "en",
            nationality = nationality,
            mykadNumber = mykadNumber.takeIf { it.isNotBlank() },
            passportNumber = passportNumber.takeIf { it.isNotBlank() },
            passportExpiry = passportExpiry.takeIf { it.isNotBlank() },
            plksNumber = plksNumber.takeIf { it.isNotBlank() },
            plksExpiry = plksExpiry.takeIf { it.isNotBlank() },
            driversLicenseNumber = driversLicenseNumber,
            licenseClass = licenseClass,
            licenseExpiry = licenseExpiry.takeIf { it.isNotBlank() },
            hasGDL = hasGDL,
            gdlExpiry = gdlExpiry.takeIf { it.isNotBlank() },
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            postcode = postcode,
            state = state,
            workingStates = listOfNotNull(state),
            emergencyContactName = emergencyContactName,
            emergencyContactRelation = emergencyContactRelation,
            emergencyContactPhone = emergencyContactPhone,
            bankName = bankName,
            bankAccountNumber = bankAccountNumber,
            bankAccountHolder = bankAccountHolder,
            duitNowId = duitNowId.takeIf { it.isNotBlank() },
            tngEwalletId = tngEwalletId.takeIf { it.isNotBlank() },
            lhdnTaxNumber = lhdnTaxNumber.takeIf { it.isNotBlank() },
            sstNumber = sstNumber.takeIf { it.isNotBlank() },
            pdpaConsent = pdpaConsent,
            backgroundCheckConsent = backgroundCheckConsent,
            noOffencesDeclared = noOffencesDeclared
        )
    }

    private fun DriverOnboardingDraft.toVehicleRequest(): DriverVehicleUpsertRequest {
        return DriverVehicleUpsertRequest(
            type = vehicleType ?: VehicleType.BIKE,
            make = vehicleMake,
            model = vehicleModel,
            year = vehicleYear.toIntOrNull() ?: 0,
            licensePlate = vehiclePlate,
            color = vehicleColor,
            chassisNumber = chassisNumber,
            engineNumber = engineNumber,
            ownership = vehicleOwnership,
            ownerName = ownerName.takeIf { it.isNotBlank() },
            roadTaxExpiry = roadTaxExpiry.takeIf { it.isNotBlank() },
            puspakomExpiry = puspakomExpiry.takeIf { it.isNotBlank() },
            apadPermitNumber = apadPermitNumber.takeIf { it.isNotBlank() },
            apadPermitExpiry = apadPermitExpiry.takeIf { it.isNotBlank() },
            insurerName = insurerName.takeIf { it.isNotBlank() },
            insurancePolicyNumber = insurancePolicyNumber.takeIf { it.isNotBlank() },
            insuranceCoverageType = insuranceCoverageType,
            insuranceExpiry = insuranceExpiry.takeIf { it.isNotBlank() },
            hasCommercialCover = hasCommercialCover
        )
    }

    private fun matchesNameSoft(a: String, b: String): Boolean {
        return normalizeName(a) == normalizeName(b)
    }

    private fun normalizeName(value: String): String {
        return value.lowercase().replace(Regex("[^a-z0-9]"), "")
    }

    private fun isValidMyKad(value: String): Boolean {
        return value.filter(Char::isDigit).length == 12
    }

    private fun isCommercialVehicle(type: VehicleType?): Boolean {
        return type in COMMERCIAL_VEHICLE_TYPES
    }

    private fun isExpired(date: String): Boolean {
        val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return false
        return parsed < today()
    }

    private fun today(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    private fun currentYear(): Int = today().year

    companion object {
        const val TOTAL_STEPS = 13

        val states = MalaysianState.entries
        val vehicleTypes = VehicleType.entries
        val licenseClasses = LicenseClass.entries
        val ownershipTypes = VehicleOwnership.entries
        val insuranceCoverageTypes = InsuranceCoverageType.entries
        val nationalities = DriverNationality.entries

        val banks = listOf(
            "Maybank",
            "CIMB",
            "Public Bank",
            "RHB",
            "Hong Leong Bank",
            "AmBank",
            "Bank Islam",
            "Bank Rakyat",
            "OCBC",
            "HSBC",
            "UOB",
            "Affin Bank"
        )

        val genders = listOf("Male", "Female", "Other")

        private val PHONE_REGEX = Regex("^[+0-9][0-9 -]{7,15}$")
        private val POSTCODE_REGEX = Regex("^\\d{5}$")
        private val COMMERCIAL_VEHICLE_TYPES = setOf(
            VehicleType.PICKUP,
            VehicleType.VAN,
            VehicleType.VAN_7FT,
            VehicleType.VAN_9FT,
            VehicleType.LORRY_10FT,
            VehicleType.LORRY_14FT,
            VehicleType.LORRY_17FT,
            VehicleType.TRUCK
        )
        private val IDENTITY_DOCUMENT_TYPES = setOf(
            DocumentType.MYKAD_FRONT,
            DocumentType.MYKAD_BACK,
            DocumentType.SELFIE,
            DocumentType.PASSPORT,
            DocumentType.WORK_PERMIT_PLKS
        )
    }
}
