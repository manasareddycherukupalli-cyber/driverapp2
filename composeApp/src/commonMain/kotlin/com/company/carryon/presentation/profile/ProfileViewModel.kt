package com.company.carryon.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.di.ServiceLocator
import com.company.carryon.data.network.getOnboardingDraft
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ProfileViewModel — Manages driver profile data and updates.
 */
class ProfileViewModel : ViewModel() {

    private val authRepository = ServiceLocator.authRepository
    private val json = Json { ignoreUnknownKeys = true }

    val currentDriver: StateFlow<Driver?> = authRepository.currentDriver
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _draftDocuments = MutableStateFlow<List<Document>>(emptyList())
    val hubDocuments: StateFlow<List<Document>> = combine(currentDriver, _draftDocuments) { driver, draftDocs ->
        val mergedByType = linkedMapOf<DocumentType, Document>()
        driver?.documents.orEmpty().forEach { mergedByType[it.type] = it }
        // Local onboarding draft docs should appear in Documents Hub even before final submit.
        draftDocs.forEach { mergedByType[it.type] = it }
        mergedByType.values.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _updateState = MutableStateFlow<UiState<Driver>>(UiState.Idle)
    val updateState: StateFlow<UiState<Driver>> = _updateState.asStateFlow()

    init {
        refreshDocumentsForHub()
    }

    fun updateProfile(name: String, email: String, emergencyContact: String) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val current = currentDriver.value ?: return@launch
            val updated = current.copy(
                name = name,
                email = email,
                emergencyContact = emergencyContact
            )
            authRepository.updateProfile(updated)
                .onSuccess { _updateState.value = UiState.Success(it) }
                .onFailure { _updateState.value = UiState.Error(it.message ?: "Update failed") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun refreshDocumentsForHub() {
        viewModelScope.launch {
            authRepository.getVerificationStatus()
            _draftDocuments.value = loadLocalDraftDocuments()
        }
    }

    private fun loadLocalDraftDocuments(): List<Document> {
        val driverId = currentDriver.value?.id?.takeIf { it.isNotBlank() } ?: return emptyList()
        val payload = getOnboardingDraft(driverId) ?: return emptyList()
        val draft = runCatching { json.decodeFromString<DriverOnboardingDraft>(payload) }.getOrNull() ?: return emptyList()

        val mergedByType = linkedMapOf<DocumentType, Document>()

        fun add(type: DocumentType, url: String, expiry: String? = null) {
            if (url.isNotBlank()) {
                mergedByType[type] = Document(
                    type = type,
                    imageUrl = url,
                    expiryDate = expiry?.takeIf { it.isNotBlank() },
                    status = DocumentStatus.PENDING
                )
            }
        }

        draft.identityDocuments.forEach { asset ->
            if (asset.imageUrl.isNotBlank()) {
                mergedByType[asset.type] = Document(
                    type = asset.type,
                    imageUrl = asset.imageUrl,
                    expiryDate = asset.expiryDate?.takeIf { it.isNotBlank() },
                    status = DocumentStatus.PENDING
                )
            }
        }

        add(DocumentType.DRIVERS_LICENSE, draft.driversLicenseFrontUrl, draft.licenseExpiry)
        add(DocumentType.DRIVERS_LICENSE_BACK, draft.driversLicenseBackUrl, draft.licenseExpiry)
        add(DocumentType.GDL, draft.gdlUrl, draft.gdlExpiry)
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

        return mergedByType.values.toList()
    }
}
