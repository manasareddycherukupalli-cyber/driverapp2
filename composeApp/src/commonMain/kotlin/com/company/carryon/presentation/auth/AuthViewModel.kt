package com.company.carryon.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.company.carryon.data.model.*
import com.company.carryon.data.network.hasLocationPermission
import com.company.carryon.data.network.SupabaseConfig
import com.company.carryon.data.network.getToken
import com.company.carryon.data.network.saveToken
import com.company.carryon.di.ServiceLocator
import com.company.carryon.i18n.currentLanguageOrDefault
import com.company.carryon.presentation.navigation.Screen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AuthFlowType { LOGIN, SIGNUP }

class AuthViewModel : ViewModel() {

    private val repository = ServiceLocator.authRepository

    // Global exception handler for coroutines to prevent unhandled crashes on iOS
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("AuthViewModel coroutine exception: ${throwable.message}")
        throwable.printStackTrace()
    }

    // ---- OTP Flow State ----
    private val _otpSendState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val otpSendState: StateFlow<UiState<Boolean>> = _otpSendState.asStateFlow()

    private val _otpVerifyState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val otpVerifyState: StateFlow<UiState<AuthResponse>> = _otpVerifyState.asStateFlow()

    // ---- Registration State ----
    private val _registrationState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val registrationState: StateFlow<UiState<AuthResponse>> = _registrationState.asStateFlow()

    // ---- Document Upload State ----
    private val _documentUploadState = MutableStateFlow<UiState<Document>>(UiState.Idle)
    val documentUploadState: StateFlow<UiState<Document>> = _documentUploadState.asStateFlow()

    // ---- Vehicle Details State ----
    private val _vehicleState = MutableStateFlow<UiState<VehicleDetails>>(UiState.Idle)
    val vehicleState: StateFlow<UiState<VehicleDetails>> = _vehicleState.asStateFlow()

    // ---- Verification State ----
    private val _verificationState = MutableStateFlow<UiState<Driver>>(UiState.Idle)
    val verificationState: StateFlow<UiState<Driver>> = _verificationState.asStateFlow()
    private val _profileUpdateState = MutableStateFlow<UiState<Driver>>(UiState.Idle)
    val profileUpdateState: StateFlow<UiState<Driver>> = _profileUpdateState.asStateFlow()

    // ---- Session State ----
    private val _hasValidSession = MutableStateFlow(false)
    val hasValidSession: StateFlow<Boolean> = _hasValidSession.asStateFlow()
    private val _latestAuthResponse = MutableStateFlow<AuthResponse?>(null)
    val latestAuthResponse: StateFlow<AuthResponse?> = _latestAuthResponse.asStateFlow()

    // ---- Auth Flow Type ----
    var authFlowType by mutableStateOf(AuthFlowType.LOGIN)

    // ---- Form Fields ----
    var driverEmail by mutableStateOf("")
    var otpCode by mutableStateOf("")
    var driverName by mutableStateOf("")
    var driverPhone by mutableStateOf("")
    var emergencyContact by mutableStateOf("")
    private var lastSubmittedDriversLicenseNumber by mutableStateOf("")
    private var lastSubmittedDateOfBirth by mutableStateOf("")

    // ---- Uploaded Documents Tracker ----
    private val _uploadedDocuments = MutableStateFlow<List<Document>>(emptyList())
    val uploadedDocuments: StateFlow<List<Document>> = _uploadedDocuments.asStateFlow()

    init {
        repository.checkExistingSession()
        checkExistingSupabaseSession()
    }

    private fun checkExistingSupabaseSession() {
        viewModelScope.launch {
            try {
                // First try the current session
                var session = SupabaseConfig.client.auth.currentSessionOrNull()

                // If no current session, try refreshing (handles app restart with expired access token)
                if (session == null) {
                    try {
                        SupabaseConfig.client.auth.refreshCurrentSession()
                        session = SupabaseConfig.client.auth.currentSessionOrNull()
                    } catch (_: Exception) {
                        // Refresh failed — no valid session
                    }
                }

                if (session != null) {
                    saveToken(session.accessToken)
                    _hasValidSession.value = true
                }
            } catch (_: Exception) {
                // No valid session — user needs to log in
            }
        }
    }

    /**
     * Called after Supabase OTP sends successfully.
     * The OTP is sent directly via Supabase SDK on the client side.
     * This just updates the UI state.
     */
    fun onOtpSent(email: String) {
        driverEmail = email
        _otpSendState.value = UiState.Success(true)
    }

    fun onOtpSendError(error: String) {
        _otpSendState.value = UiState.Error(error)
    }

    fun setOtpLoading() {
        _otpSendState.value = UiState.Loading
    }

    /**
     * Called after Supabase OTP verification succeeds.
     * Saves the Supabase token and syncs driver with backend.
     * For SIGNUP flow, also registers the driver details (name, phone) collected on the registration screen.
     */
    fun onSupabaseTokenReceived(token: String) {
        saveToken(token)
        _hasValidSession.value = true
        viewModelScope.launch {
            _otpVerifyState.value = UiState.Loading
            repository.syncDriver()
                .onSuccess { syncResponse ->
                    // If this is a signup flow and we have driver details, register them
                    if (authFlowType == AuthFlowType.SIGNUP && driverName.isNotBlank()) {
                        val driver = Driver(
                            name = driverName,
                            phone = driverPhone,
                            email = driverEmail,
                            emergencyContact = emergencyContact,
                            preferredLanguage = currentLanguageOrDefault()
                        )
                        repository.register(driver)
                            .onSuccess { registerResponse ->
                                _latestAuthResponse.value = registerResponse
                                registerResponse.driver?.let { hydrateIdentityFallbacks(it) }
                                _otpVerifyState.value = UiState.Success(registerResponse)
                            }
                            .onFailure {
                                // Registration of details failed, but sync succeeded —
                                // proceed with the sync response so the user isn't blocked
                                _latestAuthResponse.value = syncResponse
                                syncResponse.driver?.let { hydrateIdentityFallbacks(it) }
                                _otpVerifyState.value = UiState.Success(syncResponse)
                            }
                    } else {
                        _latestAuthResponse.value = syncResponse
                        syncResponse.driver?.let { hydrateIdentityFallbacks(it) }
                        _otpVerifyState.value = UiState.Success(syncResponse)
                    }
                }
                .onFailure {
                    _otpVerifyState.value = UiState.Error(it.message ?: "Sync failed")
                }
        }
    }

    fun onOtpVerifyError(error: String) {
        _otpVerifyState.value = UiState.Error(error)
    }

    /** Register a new driver with details */
    fun registerDriver() {
        viewModelScope.launch {
            _registrationState.value = UiState.Loading
            val driver = Driver(
                name = driverName,
                phone = driverPhone,
                email = driverEmail,
                emergencyContact = emergencyContact
            )
            repository.register(driver)
                .onSuccess { _registrationState.value = UiState.Success(it) }
                .onFailure { _registrationState.value = UiState.Error(it.message ?: "Registration failed") }
        }
    }

    /** Upload a driver document — uploads image to Supabase Storage, then saves metadata to backend */
    fun uploadDocument(type: DocumentType, imageBytes: ByteArray) {
        viewModelScope.launch(exceptionHandler) {
            try {
                _documentUploadState.value = UiState.Loading

                // 1. Upload image bytes to Supabase Storage
                val driverId = _latestAuthResponse.value?.driver?.id
                    ?: throw IllegalStateException("No active driver session")
                val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
                val path = "$driverId/${type.name.lowercase()}_${timestamp}.jpg"

                val bucket = SupabaseConfig.client.storage.from("driver-documents")
                bucket.upload(path, imageBytes) { upsert = true }
                // Store object path, not public URL — backend generates signed URLs
                val objectPath = "driver-documents/$path"

                // 2. Save document metadata (with object path) to backend
                val document = Document(type = type, imageUrl = objectPath)

                repository.uploadDocument(document)
                    .onSuccess { doc ->
                        val current = _uploadedDocuments.value.toMutableList()
                        val existingIndex = current.indexOfFirst { it.type == doc.type }
                        if (existingIndex >= 0) current[existingIndex] = doc
                        else current.add(doc)
                        _uploadedDocuments.value = current
                        _latestAuthResponse.value = _latestAuthResponse.value?.let { response ->
                            val driver = response.driver ?: return@let response
                            val mergedByType = linkedMapOf<DocumentType, Document>()
                            driver.documents.forEach { mergedByType[it.type] = it }
                            current.forEach { mergedByType[it.type] = it }
                            response.copy(driver = driver.copy(documents = mergedByType.values.toList()))
                        }
                        _documentUploadState.value = UiState.Success(doc)
                    }
                    .onFailure { error ->
                        error.printStackTrace()
                        _documentUploadState.value = UiState.Error(error.message ?: "Upload failed")
                    }
            } catch (t: Throwable) {
                t.printStackTrace()
                _documentUploadState.value = UiState.Error(t.message ?: "Upload failed")
            }
        }
    }

    /** Save vehicle details */
    fun saveVehicleDetails(
        type: VehicleType,
        make: String,
        model: String,
        year: Int,
        licensePlate: String,
        color: String
    ) {
        viewModelScope.launch {
            _vehicleState.value = UiState.Loading
            val vehicle = VehicleDetails(
                type = type,
                make = make,
                model = model,
                year = year,
                licensePlate = licensePlate,
                color = color
            )
            repository.updateVehicle(vehicle)
                .onSuccess { savedVehicle ->
                    _latestAuthResponse.value = _latestAuthResponse.value?.let { response ->
                        val driver = response.driver ?: return@let response
                        response.copy(driver = driver.copy(vehicleDetails = savedVehicle))
                    }
                    _vehicleState.value = UiState.Success(savedVehicle)
                }
                .onFailure { _vehicleState.value = UiState.Error(it.message ?: "Failed to save vehicle") }
        }
    }

    /** Check current verification status */
    fun checkVerificationStatus() {
        viewModelScope.launch {
            _verificationState.value = UiState.Loading
            repository.getVerificationStatus()
                .onSuccess { driver ->
                    hydrateIdentityFallbacks(driver)
                    // Backend doesn't return driversLicenseNumber/dateOfBirth in the profile
                    // response — apply the in-memory fallback so the UI shows the correct state.
                    val mergedDriver = driver.copy(
                        driversLicenseNumber = driver.driversLicenseNumber.ifBlank { lastSubmittedDriversLicenseNumber },
                        dateOfBirth = driver.dateOfBirth.ifBlank { lastSubmittedDateOfBirth }
                    )
                    _verificationState.value = UiState.Success(mergedDriver)
                    _latestAuthResponse.value = _latestAuthResponse.value?.copy(driver = mergedDriver)
                }
                .onFailure { _verificationState.value = UiState.Error(it.message ?: "Failed to check status") }
        }
    }

    fun updateProfile(
        name: String,
        phone: String,
        emergencyContact: String = "",
        driversLicenseNumber: String? = null,
        dateOfBirth: String? = null
    ) {
        viewModelScope.launch {
            _profileUpdateState.value = UiState.Loading
            val current = _latestAuthResponse.value?.driver
            if (current == null) {
                _profileUpdateState.value = UiState.Error("No active driver session")
                return@launch
            }

            val updated = current.copy(
                name = name,
                phone = phone,
                emergencyContact = emergencyContact,
                driversLicenseNumber = driversLicenseNumber ?: current.driversLicenseNumber,
                dateOfBirth = dateOfBirth ?: current.dateOfBirth
            )
            lastSubmittedDriversLicenseNumber = updated.driversLicenseNumber
            lastSubmittedDateOfBirth = updated.dateOfBirth
            repository.updateProfile(updated)
                .onSuccess { serverDriver ->
                    val mergedDriver = serverDriver.copy(
                        driversLicenseNumber = serverDriver.driversLicenseNumber.ifBlank { updated.driversLicenseNumber },
                        dateOfBirth = serverDriver.dateOfBirth.ifBlank { updated.dateOfBirth }
                    )

                    if (serverDriver.driversLicenseNumber.isBlank() && mergedDriver.driversLicenseNumber.isNotBlank()) {
                        println("[AuthViewModel] Warning: backend profile response missing driversLicenseNumber; using submitted value for session state.")
                    }
                    if (serverDriver.dateOfBirth.isBlank() && mergedDriver.dateOfBirth.isNotBlank()) {
                        println("[AuthViewModel] Warning: backend profile response missing dateOfBirth; using submitted value for session state.")
                    }

                    _profileUpdateState.value = UiState.Success(mergedDriver)
                    _latestAuthResponse.value = _latestAuthResponse.value?.copy(driver = mergedDriver)
                    driverName = mergedDriver.name
                    driverPhone = mergedDriver.phone
                    driverEmail = mergedDriver.email
                }
                .onFailure { _profileUpdateState.value = UiState.Error(it.message ?: "Profile update failed") }
        }
    }

    fun resetProfileUpdateState() {
        _profileUpdateState.value = UiState.Idle
    }

    /**
     * Route immediately after auth.
     * We always show the location step first so Home is never reached directly after login.
     */
    fun determinePostAuthScreen(response: AuthResponse): Screen {
        _latestAuthResponse.value = response
        response.driver?.let { hydrateIdentityFallbacks(it) }
        return if (hasLocationPermission()) {
            determinePostLocationScreen(response)
        } else {
            Screen.LocationPermission
        }
    }

    /**
     * Route after the location permission step.
     * Home is intentionally gated behind verification screens.
     */
    fun determinePostLocationScreen(response: AuthResponse? = _latestAuthResponse.value): Screen {
        val resolved = response ?: return Screen.Onboarding
        _latestAuthResponse.value = resolved
        return determineNextRequiredScreen(resolved)
    }

    /** Legacy entry point kept for compatibility with existing call sites */
    fun determineProfileCompletionScreen(response: AuthResponse): Screen {
        return determineNextRequiredScreen(response)
    }

    private fun determineNextRequiredScreen(response: AuthResponse): Screen {
        val rawDriver = response.driver ?: return Screen.DriverOnboarding
        val mergedByType = linkedMapOf<DocumentType, Document>()
        rawDriver.documents.forEach { mergedByType[it.type] = it }
        _uploadedDocuments.value.forEach { mergedByType[it.type] = it }
        val driver = rawDriver.copy(
            documents = mergedByType.values.toList(),
            driversLicenseNumber = rawDriver.driversLicenseNumber.ifBlank { lastSubmittedDriversLicenseNumber },
            dateOfBirth = rawDriver.dateOfBirth.ifBlank { lastSubmittedDateOfBirth }
        )

        // Fully approved drivers should not be asked for onboarding details again.
        if (driver.verificationStatus == VerificationStatus.APPROVED) {
            return Screen.Home
        }

        val hasSubmittedOnboardingPackage =
            driver.vehicleDetails != null &&
                driver.driversLicenseNumber.isNotBlank() &&
                driver.documents.size >= 5

        if (hasSubmittedOnboardingPackage) {
            return Screen.VerificationStatus
        }

        return Screen.DriverOnboarding
    }

    private fun isPersonalIdentitySubmitted(driver: Driver): Boolean {
        val hasCoreProfile = driver.name.isNotBlank() &&
            driver.email.isNotBlank() &&
            driver.phone.isNotBlank() &&
            driver.driversLicenseNumber.isNotBlank()
        return hasCoreProfile
    }

    private fun isIdentityVerificationSubmitted(driver: Driver): Boolean {
        val identityDoc = driver.documents.firstOrNull {
            it.type == DocumentType.ID_PROOF || it.type == DocumentType.DRIVERS_LICENSE
        } ?: return false
        return identityDoc.status != DocumentStatus.REJECTED
    }

    // ---- Session Sync State (for SplashScreen) ----
    private val _sessionSyncState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val sessionSyncState: StateFlow<UiState<AuthResponse>> = _sessionSyncState.asStateFlow()

    /** Sync driver for an existing session (called from SplashScreen) */
    fun syncDriverForSession() {
        viewModelScope.launch {
            _sessionSyncState.value = UiState.Loading
            repository.syncDriver()
                .onSuccess { response ->
                    _latestAuthResponse.value = response
                    response.driver?.let { hydrateIdentityFallbacks(it) }
                    _sessionSyncState.value = UiState.Success(response)
                }
                .onFailure {
                    _sessionSyncState.value = UiState.Error(it.message ?: "Sync failed")
                }
        }
    }

    private fun hydrateIdentityFallbacks(driver: Driver) {
        if (driver.driversLicenseNumber.isNotBlank()) {
            lastSubmittedDriversLicenseNumber = driver.driversLicenseNumber
        }
        if (driver.dateOfBirth.isNotBlank()) {
            lastSubmittedDateOfBirth = driver.dateOfBirth
        }
    }

    /** Reset states for re-navigation */
    fun resetOtpState() {
        _otpSendState.value = UiState.Idle
        _otpVerifyState.value = UiState.Idle
    }

    private fun <T> mutableStateOf(value: T): androidx.compose.runtime.MutableState<T> {
        return androidx.compose.runtime.mutableStateOf(value)
    }
}
