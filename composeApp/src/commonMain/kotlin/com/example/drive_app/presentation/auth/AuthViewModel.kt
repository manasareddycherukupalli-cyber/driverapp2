package com.example.drive_app.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drive_app.data.model.*
import com.example.drive_app.data.network.SupabaseConfig
import com.example.drive_app.data.network.getToken
import com.example.drive_app.data.network.saveToken
import com.example.drive_app.di.ServiceLocator
import com.example.drive_app.presentation.navigation.Screen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AuthFlowType { LOGIN, SIGNUP }

class AuthViewModel : ViewModel() {

    private val repository = ServiceLocator.authRepository

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

    // ---- Session State ----
    private val _hasValidSession = MutableStateFlow(false)
    val hasValidSession: StateFlow<Boolean> = _hasValidSession.asStateFlow()

    // ---- Auth Flow Type ----
    var authFlowType by mutableStateOf(AuthFlowType.LOGIN)

    // ---- Form Fields ----
    var driverEmail by mutableStateOf("")
    var otpCode by mutableStateOf("")
    var driverName by mutableStateOf("")
    var driverPhone by mutableStateOf("")
    var emergencyContact by mutableStateOf("")

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
                val session = SupabaseConfig.client.auth.currentSessionOrNull()
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
     */
    fun onSupabaseTokenReceived(token: String) {
        saveToken(token)
        viewModelScope.launch {
            _otpVerifyState.value = UiState.Loading
            repository.syncDriver()
                .onSuccess { response ->
                    _otpVerifyState.value = UiState.Success(response)
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
        viewModelScope.launch {
            try {
                _documentUploadState.value = UiState.Loading

                // 1. Upload image bytes to Supabase Storage
                val sanitizedEmail = driverEmail.replace("@", "_").replace(".", "_").ifEmpty { "unknown" }
                val path = "drivers/$sanitizedEmail/${type.name.lowercase()}.jpg"
                val bucket = SupabaseConfig.client.storage.from("driver-documents")
                bucket.upload(path, imageBytes) { upsert = true }
                val publicUrl = bucket.publicUrl(path)

                // 2. Save document metadata (with real URL) to backend
                val document = Document(type = type, imageUrl = publicUrl)
                repository.uploadDocument(document)
                    .onSuccess { doc ->
                        _documentUploadState.value = UiState.Success(doc)
                        val current = _uploadedDocuments.value.toMutableList()
                        val existingIndex = current.indexOfFirst { it.type == doc.type }
                        if (existingIndex >= 0) current[existingIndex] = doc
                        else current.add(doc)
                        _uploadedDocuments.value = current
                    }
                    .onFailure { _documentUploadState.value = UiState.Error(it.message ?: "Upload failed") }
            } catch (t: Throwable) {
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
                .onSuccess { _vehicleState.value = UiState.Success(it) }
                .onFailure { _vehicleState.value = UiState.Error(it.message ?: "Failed to save vehicle") }
        }
    }

    /** Check current verification status */
    fun checkVerificationStatus() {
        viewModelScope.launch {
            _verificationState.value = UiState.Loading
            repository.getVerificationStatus()
                .onSuccess { _verificationState.value = UiState.Success(it) }
                .onFailure { _verificationState.value = UiState.Error(it.message ?: "Failed to check status") }
        }
    }

    /** Determine which screen the driver should land on based on profile completeness */
    fun determinePostAuthScreen(response: AuthResponse): Screen {
        val driver = response.driver
        if (response.isNewDriver || authFlowType == AuthFlowType.SIGNUP) {
            return Screen.DocumentUpload
        }
        if (driver == null || driver.documents.isEmpty()) {
            return Screen.DocumentUpload
        }
        if (driver.vehicleDetails == null) {
            return Screen.VehicleDetailsInput
        }
        if (driver.verificationStatus != VerificationStatus.APPROVED) {
            return Screen.VerificationStatus
        }
        return Screen.Home
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
                    _sessionSyncState.value = UiState.Success(response)
                }
                .onFailure {
                    _sessionSyncState.value = UiState.Error(it.message ?: "Sync failed")
                }
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
