package com.example.drive_app.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drive_app.data.model.*
import com.example.drive_app.di.ServiceLocator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * AuthViewModel — Handles all authentication and onboarding state.
 * Manages OTP flow, registration, document upload, and vehicle details.
 */
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

    // ---- Form Fields ----
    var phoneNumber by mutableStateOf("")
    var otpCode by mutableStateOf("")
    var driverName by mutableStateOf("")
    var driverEmail by mutableStateOf("")
    var emergencyContact by mutableStateOf("")

    // ---- Uploaded Documents Tracker ----
    private val _uploadedDocuments = MutableStateFlow<List<Document>>(emptyList())
    val uploadedDocuments: StateFlow<List<Document>> = _uploadedDocuments.asStateFlow()

    /** Send OTP to the provided phone number */
    fun sendOtp(phone: String, countryCode: String = "+91") {
        phoneNumber = phone
        viewModelScope.launch {
            _otpSendState.value = UiState.Loading
            repository.sendOtp(phone, countryCode)
                .onSuccess { _otpSendState.value = UiState.Success(true) }
                .onFailure { _otpSendState.value = UiState.Error(it.message ?: "Failed to send OTP") }
        }
    }

    /** Verify OTP code entered by user */
    fun verifyOtp(otp: String) {
        otpCode = otp
        viewModelScope.launch {
            _otpVerifyState.value = UiState.Loading
            repository.verifyOtp(phoneNumber, otp)
                .onSuccess { response ->
                    if (response.success) {
                        _otpVerifyState.value = UiState.Success(response)
                    } else {
                        _otpVerifyState.value = UiState.Error(response.message)
                    }
                }
                .onFailure { _otpVerifyState.value = UiState.Error(it.message ?: "Verification failed") }
        }
    }

    /** Register a new driver */
    fun registerDriver() {
        viewModelScope.launch {
            _registrationState.value = UiState.Loading
            val driver = Driver(
                name = driverName,
                phone = phoneNumber,
                email = driverEmail,
                emergencyContact = emergencyContact
            )
            repository.register(driver)
                .onSuccess { _registrationState.value = UiState.Success(it) }
                .onFailure { _registrationState.value = UiState.Error(it.message ?: "Registration failed") }
        }
    }

    /** Upload a driver document (license, registration, etc.) */
    fun uploadDocument(type: DocumentType) {
        viewModelScope.launch {
            _documentUploadState.value = UiState.Loading
            val document = Document(type = type, imageUrl = "dummy_image_url_${type.name}")
            repository.uploadDocument(document)
                .onSuccess { doc ->
                    _documentUploadState.value = UiState.Success(doc)
                    // Update local documents list
                    val current = _uploadedDocuments.value.toMutableList()
                    val existingIndex = current.indexOfFirst { it.type == doc.type }
                    if (existingIndex >= 0) current[existingIndex] = doc
                    else current.add(doc)
                    _uploadedDocuments.value = current
                }
                .onFailure { _documentUploadState.value = UiState.Error(it.message ?: "Upload failed") }
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

    /** Reset states for re-navigation */
    fun resetOtpState() {
        _otpSendState.value = UiState.Idle
        _otpVerifyState.value = UiState.Idle
    }

    private fun <T> mutableStateOf(value: T): androidx.compose.runtime.MutableState<T> {
        return androidx.compose.runtime.mutableStateOf(value)
    }
}
