package com.company.carryon.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DRIVER_ONBOARDING_AGREEMENT_VERSION
import com.company.carryon.data.model.Document
import com.company.carryon.data.model.DocumentStatus
import com.company.carryon.data.model.DocumentType
import com.company.carryon.data.model.DriverNationality
import com.company.carryon.data.model.DriverOnboardingDraft
import com.company.carryon.data.model.InsuranceCoverageType
import com.company.carryon.data.model.LicenseClass
import com.company.carryon.data.model.MalaysianState
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.ValidationMessage
import com.company.carryon.data.model.VehicleOwnership
import com.company.carryon.data.model.VehicleType
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.util.rememberImagePickerLauncher
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val Bg = Color(0xFFF7F8FC)
private val Blue = Color(0xFF2F80ED)
private val BlueTint = Color(0xFFEAF2FE)
private val TextPrimary = Color(0xFF1E2430)
private val TextMuted = Color(0xFF667085)
private val Success = Color(0xFF2E7D32)
private val Warning = Color(0xFFE18A00)
private val Danger = Color(0xFFD32F2F)

private val stepTitles = listOf(
    "Phone",
    "Nationality",
    "Identity",
    "Personal",
    "License",
    "Vehicle",
    "Vehicle Docs",
    "Vehicle Photos",
    "Insurance",
    "Bank",
    "Tax",
    "Background",
    "Review"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverOnboardingFlowScreen(
    navigator: AppNavigator,
    viewModel: DriverOnboardingViewModel
) {
    val initialLoadState by viewModel.initialLoadState.collectAsState()
    val draft by viewModel.draftState.collectAsState()
    val uploadState by viewModel.documentUploadState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val serverDocuments by viewModel.serverDocuments.collectAsState()

    var pendingType by remember { mutableStateOf<DocumentType?>(null) }
    var pendingExpiry by remember { mutableStateOf<String?>(null) }
    val picker = rememberImagePickerLauncher { bytes ->
        pendingType?.let { type -> viewModel.uploadDocument(type, bytes, pendingExpiry) }
        pendingType = null
        pendingExpiry = null
    }
    val launchUpload: (DocumentType, String?) -> Unit = { type, expiry ->
        pendingType = type
        pendingExpiry = expiry
        picker.launch()
    }

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            navigator.navigateAndClearStack(Screen.VerificationStatus)
        }
    }

    when (initialLoadState) {
        UiState.Loading, UiState.Idle -> Box(
            modifier = Modifier.fillMaxSize().background(Bg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Blue)
        }

        is UiState.Error -> Box(
            modifier = Modifier.fillMaxSize().background(Bg),
            contentAlignment = Alignment.Center
        ) {
            Text((initialLoadState as UiState.Error).message, color = Danger)
        }

        is UiState.Success -> {
            val messages = remember(draft) { viewModel.validationMessagesForCurrentStep() }
            val blockingErrors = messages.filterNot { it.isWarning }
            val currentStep = draft.currentStep

            Scaffold(
                containerColor = Bg,
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Driver verification", fontWeight = FontWeight.Bold)
                                Text(
                                    "Step $currentStep of ${DriverOnboardingViewModel.TOTAL_STEPS}",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    FooterActions(
                        step = currentStep,
                        canGoBack = currentStep > 1,
                        isLoading = submitState is UiState.Loading,
                        hasBlockingErrors = blockingErrors.isNotEmpty(),
                        onBack = viewModel::goBack,
                        onNext = {
                            if (currentStep == DriverOnboardingViewModel.TOTAL_STEPS) {
                                viewModel.submit()
                            } else {
                                viewModel.continueCurrentStep()
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StepRail(
                        currentStep = currentStep,
                        completedSteps = draft.completedSteps,
                        onStepSelected = viewModel::setCurrentStep,
                        canOpenStep = viewModel::canOpenStep
                    )

                    StatusBanner(submitState = submitState, uploadState = uploadState)

                    when (currentStep) {
                        1 -> PhoneStep(
                            draft = draft,
                            messages = messages,
                            onPhoneChanged = { value -> viewModel.updateDraft { it.copy(phone = value) } }
                        )
                        2 -> NationalityStep(
                            draft = draft,
                            messages = messages,
                            onSelected = { nationality -> viewModel.updateDraft { it.copy(nationality = nationality) } }
                        )
                        3 -> IdentityStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            onMyKadChanged = { value -> viewModel.updateDraft { it.copy(mykadNumber = value) } },
                            onPassportChanged = { value -> viewModel.updateDraft { it.copy(passportNumber = value) } },
                            onPassportExpiryChanged = { value -> viewModel.updateDraft { it.copy(passportExpiry = value) } },
                            onPlksChanged = { value -> viewModel.updateDraft { it.copy(plksNumber = value) } },
                            onPlksExpiryChanged = { value -> viewModel.updateDraft { it.copy(plksExpiry = value) } },
                            launchUpload = launchUpload
                        )
                        4 -> PersonalStep(
                            draft = draft,
                            messages = messages,
                            onNameChanged = { value -> viewModel.updateDraft { it.copy(fullName = value) } },
                            onDobChanged = { value -> viewModel.updateDraft { it.copy(dateOfBirth = value) } },
                            onGenderChanged = { value -> viewModel.updateDraft { it.copy(gender = value) } },
                            onAddressLine1Changed = { value -> viewModel.updateDraft { it.copy(addressLine1 = value) } },
                            onAddressLine2Changed = { value -> viewModel.updateDraft { it.copy(addressLine2 = value) } },
                            onCityChanged = { value -> viewModel.updateDraft { it.copy(city = value) } },
                            onPostcodeChanged = { value -> viewModel.updateDraft { it.copy(postcode = value) } },
                            onStateChanged = { value -> viewModel.updateDraft { it.copy(state = value) } },
                            onEmergencyNameChanged = { value -> viewModel.updateDraft { it.copy(emergencyContactName = value) } },
                            onEmergencyRelationChanged = { value -> viewModel.updateDraft { it.copy(emergencyContactRelation = value) } },
                            onEmergencyPhoneChanged = { value -> viewModel.updateDraft { it.copy(emergencyContactPhone = value) } }
                        )
                        5 -> LicenseStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            onClassChanged = { value -> viewModel.updateDraft { it.copy(licenseClass = value) } },
                            onNumberChanged = { value -> viewModel.updateDraft { it.copy(driversLicenseNumber = value) } },
                            onExpiryChanged = { value -> viewModel.updateDraft { it.copy(licenseExpiry = value) } },
                            onHasGdlChanged = { value -> viewModel.updateDraft { it.copy(hasGDL = value) } },
                            onGdlExpiryChanged = { value -> viewModel.updateDraft { it.copy(gdlExpiry = value) } },
                            launchUpload = launchUpload
                        )
                        6 -> VehicleDetailsStep(
                            draft = draft,
                            messages = messages,
                            onVehicleTypeChanged = { value -> viewModel.updateDraft { it.copy(vehicleType = value) } },
                            onMakeChanged = { value -> viewModel.updateDraft { it.copy(vehicleMake = value) } },
                            onModelChanged = { value -> viewModel.updateDraft { it.copy(vehicleModel = value) } },
                            onYearChanged = { value -> viewModel.updateDraft { it.copy(vehicleYear = value) } },
                            onPlateChanged = { value -> viewModel.updateDraft { it.copy(vehiclePlate = value) } },
                            onColorChanged = { value -> viewModel.updateDraft { it.copy(vehicleColor = value) } },
                            onChassisChanged = { value -> viewModel.updateDraft { it.copy(chassisNumber = value) } },
                            onEngineChanged = { value -> viewModel.updateDraft { it.copy(engineNumber = value) } },
                            onOwnershipChanged = { value -> viewModel.updateDraft { it.copy(vehicleOwnership = value) } },
                            onOwnerNameChanged = { value -> viewModel.updateDraft { it.copy(ownerName = value) } }
                        )
                        7 -> VehicleDocumentsStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            onRoadTaxExpiryChanged = { value -> viewModel.updateDraft { it.copy(roadTaxExpiry = value) } },
                            onPuspakomExpiryChanged = { value -> viewModel.updateDraft { it.copy(puspakomExpiry = value) } },
                            onApadNumberChanged = { value -> viewModel.updateDraft { it.copy(apadPermitNumber = value) } },
                            onApadExpiryChanged = { value -> viewModel.updateDraft { it.copy(apadPermitExpiry = value) } },
                            launchUpload = launchUpload
                        )
                        8 -> VehiclePhotosStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            launchUpload = launchUpload
                        )
                        9 -> InsuranceStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            onInsurerChanged = { value -> viewModel.updateDraft { it.copy(insurerName = value) } },
                            onPolicyChanged = { value -> viewModel.updateDraft { it.copy(insurancePolicyNumber = value) } },
                            onCoverageChanged = { value -> viewModel.updateDraft { it.copy(insuranceCoverageType = value) } },
                            onExpiryChanged = { value -> viewModel.updateDraft { it.copy(insuranceExpiry = value) } },
                            onCommercialCoverChanged = { value -> viewModel.updateDraft { it.copy(hasCommercialCover = value) } },
                            launchUpload = launchUpload
                        )
                        10 -> BankStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            onBankChanged = { value -> viewModel.updateDraft { it.copy(bankName = value) } },
                            onAccountNumberChanged = { value -> viewModel.updateDraft { it.copy(bankAccountNumber = value) } },
                            onAccountHolderChanged = { value -> viewModel.updateDraft { it.copy(bankAccountHolder = value) } },
                            onDuitNowChanged = { value -> viewModel.updateDraft { it.copy(duitNowId = value) } },
                            onTngChanged = { value -> viewModel.updateDraft { it.copy(tngEwalletId = value) } },
                            launchUpload = launchUpload
                        )
                        11 -> TaxStep(
                            draft = draft,
                            messages = messages,
                            onLhdnChanged = { value -> viewModel.updateDraft { it.copy(lhdnTaxNumber = value) } },
                            onSstChanged = { value -> viewModel.updateDraft { it.copy(sstNumber = value) } }
                        )
                        12 -> BackgroundStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            onPdpaChanged = { value -> viewModel.updateDraft { it.copy(pdpaConsent = value) } },
                            onBackgroundConsentChanged = { value -> viewModel.updateDraft { it.copy(backgroundCheckConsent = value) } },
                            onNoOffencesChanged = { value -> viewModel.updateDraft { it.copy(noOffencesDeclared = value) } },
                            launchUpload = launchUpload
                        )
                        13 -> ReviewStep(
                            draft = draft,
                            messages = messages,
                            onAgreementChanged = { value -> viewModel.updateDraft { it.copy(agreementAccepted = value) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(
    submitState: UiState<Unit>,
    uploadState: UiState<DocumentType>
) {
    when {
        submitState is UiState.Error -> BannerCard(submitState.message, Danger)
        uploadState is UiState.Error -> BannerCard(uploadState.message, Danger)
        uploadState is UiState.Loading -> BannerCard("Uploading document…", Blue)
        uploadState is UiState.Success -> BannerCard("${uploadState.data.displayName} uploaded.", Success)
    }
}

@Composable
private fun BannerCard(message: String, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.1f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StepRail(
    currentStep: Int,
    completedSteps: Set<Int>,
    onStepSelected: (Int) -> Unit,
    canOpenStep: (Int) -> Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stepTitles.forEachIndexed { index, title ->
            val step = index + 1
            val selected = step == currentStep
            val completed = step in completedSteps
            val enabled = canOpenStep(step)
            Card(
                modifier = Modifier
                    .clickable(enabled = enabled) { onStepSelected(step) }
                    .border(
                        width = 1.dp,
                        color = if (selected) Blue else Color(0xFFD8DEE8),
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        selected -> BlueTint
                        completed -> Success.copy(alpha = 0.12f)
                        else -> Color.White
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = if (completed) Success else if (selected) Blue else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completed) {
                            Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text(step.toString(), color = if (selected) Color.White else TextMuted, fontSize = 11.sp)
                        }
                    }
                    Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun FooterActions(
    step: Int,
    canGoBack: Boolean,
    isLoading: Boolean,
    hasBlockingErrors: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f),
            enabled = canGoBack && !isLoading
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Back")
        }
        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = !hasBlockingErrors && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(if (step == DriverOnboardingViewModel.TOTAL_STEPS) "Submit" else "Save & continue")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PhoneStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    onPhoneChanged: (String) -> Unit
) {
    SectionCard(
        title = "Phone capture",
        description = "Capture the driver phone number. OTP verification is deferred for now."
    ) {
        TextFieldBlock(
            label = "Phone number",
            value = draft.phone,
            onValueChange = onPhoneChanged,
            keyboardType = KeyboardType.Phone,
            placeholder = "+60 12-345 6789",
            error = errorFor(messages, "phone")
        )
    }
}

@Composable
private fun NationalityStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    onSelected: (DriverNationality) -> Unit
) {
    SectionCard(
        title = "Nationality",
        description = "Choose the path that determines the identity documents required next."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectionCard(
                modifier = Modifier.weight(1f),
                title = "Malaysian",
                subtitle = "MyKad front, back, and selfie",
                selected = draft.nationality == DriverNationality.MALAYSIAN,
                onClick = { onSelected(DriverNationality.MALAYSIAN) }
            )
            SelectionCard(
                modifier = Modifier.weight(1f),
                title = "Foreigner",
                subtitle = "Passport and PLKS",
                selected = draft.nationality == DriverNationality.FOREIGNER,
                onClick = { onSelected(DriverNationality.FOREIGNER) }
            )
        }
        ValidationText(error = errorFor(messages, "nationality"), warning = warningFor(messages, "nationality"))
    }
}

@Composable
private fun IdentityStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    onMyKadChanged: (String) -> Unit,
    onPassportChanged: (String) -> Unit,
    onPassportExpiryChanged: (String) -> Unit,
    onPlksChanged: (String) -> Unit,
    onPlksExpiryChanged: (String) -> Unit,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(
        title = "Identity upload",
        description = if (draft.nationality == DriverNationality.FOREIGNER) {
            "Upload passport and PLKS. Selfie is optional but recommended for the driver profile photo."
        } else {
            "Upload MyKad front, MyKad back, and a selfie."
        }
    ) {
        when (draft.nationality) {
            DriverNationality.MALAYSIAN -> {
                TextFieldBlock(
                    label = "MyKad number",
                    value = draft.mykadNumber,
                    onValueChange = onMyKadChanged,
                    placeholder = "YYMMDDPB####",
                    keyboardType = KeyboardType.Number,
                    error = errorFor(messages, "mykadNumber")
                )
                UploadField(
                    label = "MyKad front",
                    documentType = DocumentType.MYKAD_FRONT,
                    localUrl = identityUrl(draft, DocumentType.MYKAD_FRONT),
                    serverDocument = serverDocuments[DocumentType.MYKAD_FRONT],
                    error = errorFor(messages, "mykadFront"),
                    onUpload = { launchUpload(DocumentType.MYKAD_FRONT, null) }
                )
                UploadField(
                    label = "MyKad back",
                    documentType = DocumentType.MYKAD_BACK,
                    localUrl = identityUrl(draft, DocumentType.MYKAD_BACK),
                    serverDocument = serverDocuments[DocumentType.MYKAD_BACK],
                    error = errorFor(messages, "mykadBack"),
                    onUpload = { launchUpload(DocumentType.MYKAD_BACK, null) }
                )
                UploadField(
                    label = "Selfie",
                    documentType = DocumentType.SELFIE,
                    localUrl = identityUrl(draft, DocumentType.SELFIE),
                    serverDocument = serverDocuments[DocumentType.SELFIE],
                    error = errorFor(messages, "selfie"),
                    onUpload = { launchUpload(DocumentType.SELFIE, null) }
                )
            }
            DriverNationality.FOREIGNER -> {
                TextFieldBlock(
                    label = "Passport number",
                    value = draft.passportNumber,
                    onValueChange = onPassportChanged,
                    placeholder = "Passport number",
                    error = errorFor(messages, "passportNumber")
                )
                DateFieldBlock(
                    label = "Passport expiry",
                    value = draft.passportExpiry,
                    onValueSelected = onPassportExpiryChanged,
                    error = errorFor(messages, "passportExpiry"),
                    warning = warningFor(messages, "passportExpiry")
                )
                UploadField(
                    label = "Passport",
                    documentType = DocumentType.PASSPORT,
                    localUrl = identityUrl(draft, DocumentType.PASSPORT),
                    serverDocument = serverDocuments[DocumentType.PASSPORT],
                    error = errorFor(messages, "passport"),
                    onUpload = { launchUpload(DocumentType.PASSPORT, draft.passportExpiry.takeIf { it.isNotBlank() }) }
                )
                TextFieldBlock(
                    label = "PLKS number",
                    value = draft.plksNumber,
                    onValueChange = onPlksChanged,
                    placeholder = "PLKS number",
                    error = errorFor(messages, "plksNumber")
                )
                DateFieldBlock(
                    label = "PLKS expiry",
                    value = draft.plksExpiry,
                    onValueSelected = onPlksExpiryChanged,
                    error = errorFor(messages, "plksExpiry"),
                    warning = warningFor(messages, "plksExpiry")
                )
                UploadField(
                    label = "PLKS / work permit",
                    documentType = DocumentType.WORK_PERMIT_PLKS,
                    localUrl = identityUrl(draft, DocumentType.WORK_PERMIT_PLKS),
                    serverDocument = serverDocuments[DocumentType.WORK_PERMIT_PLKS],
                    error = errorFor(messages, "plks"),
                    onUpload = { launchUpload(DocumentType.WORK_PERMIT_PLKS, draft.plksExpiry.takeIf { it.isNotBlank() }) }
                )
                UploadField(
                    label = "Optional selfie",
                    documentType = DocumentType.SELFIE,
                    localUrl = identityUrl(draft, DocumentType.SELFIE),
                    serverDocument = serverDocuments[DocumentType.SELFIE],
                    error = null,
                    onUpload = { launchUpload(DocumentType.SELFIE, null) }
                )
            }
            null -> Text("Select nationality first.", color = TextMuted)
        }
    }
}

@Composable
private fun PersonalStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    onNameChanged: (String) -> Unit,
    onDobChanged: (String) -> Unit,
    onGenderChanged: (String) -> Unit,
    onAddressLine1Changed: (String) -> Unit,
    onAddressLine2Changed: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onPostcodeChanged: (String) -> Unit,
    onStateChanged: (MalaysianState) -> Unit,
    onEmergencyNameChanged: (String) -> Unit,
    onEmergencyRelationChanged: (String) -> Unit,
    onEmergencyPhoneChanged: (String) -> Unit
) {
    SectionCard(title = "Personal details", description = "Collect legal name, DOB, address, and emergency contact.") {
        TextFieldBlock("Full name as per IC", draft.fullName, onNameChanged, placeholder = "Full legal name", error = errorFor(messages, "fullName"))
        DateFieldBlock("Date of birth", draft.dateOfBirth, onDobChanged, error = errorFor(messages, "dateOfBirth"))
        DropdownBlock(
            label = "Gender",
            value = draft.gender,
            options = DriverOnboardingViewModel.genders,
            onSelected = onGenderChanged,
            error = errorFor(messages, "gender")
        )
        TextFieldBlock("Address line 1", draft.addressLine1, onAddressLine1Changed, placeholder = "Street address", error = errorFor(messages, "addressLine1"))
        TextFieldBlock("Address line 2", draft.addressLine2, onAddressLine2Changed, placeholder = "Apartment, unit, etc. (optional)")
        TextFieldBlock("City", draft.city, onCityChanged, placeholder = "City", error = errorFor(messages, "city"))
        TextFieldBlock("Postcode", draft.postcode, onPostcodeChanged, placeholder = "5-digit postcode", keyboardType = KeyboardType.Number, error = errorFor(messages, "postcode"))
        EnumDropdownBlock(
            label = "State",
            value = draft.state,
            options = DriverOnboardingViewModel.states,
            toLabel = ::enumLabel,
            onSelected = onStateChanged,
            error = errorFor(messages, "state")
        )
        TextFieldBlock("Emergency contact name", draft.emergencyContactName, onEmergencyNameChanged, placeholder = "Emergency contact", error = errorFor(messages, "emergencyContactName"))
        TextFieldBlock("Emergency contact relation", draft.emergencyContactRelation, onEmergencyRelationChanged, placeholder = "Spouse, parent, sibling", error = errorFor(messages, "emergencyContactRelation"))
        TextFieldBlock("Emergency contact phone", draft.emergencyContactPhone, onEmergencyPhoneChanged, placeholder = "+60 12-345 6789", keyboardType = KeyboardType.Phone, error = errorFor(messages, "emergencyContactPhone"))
    }
}

@Composable
private fun LicenseStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    onClassChanged: (LicenseClass) -> Unit,
    onNumberChanged: (String) -> Unit,
    onExpiryChanged: (String) -> Unit,
    onHasGdlChanged: (Boolean) -> Unit,
    onGdlExpiryChanged: (String) -> Unit,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(title = "Driving license", description = "Capture license details and GDL if commercial driving applies.") {
        EnumDropdownBlock(
            label = "License class",
            value = draft.licenseClass,
            options = DriverOnboardingViewModel.licenseClasses,
            toLabel = { it.name },
            onSelected = onClassChanged,
            error = errorFor(messages, "licenseClass")
        )
        TextFieldBlock("License number", draft.driversLicenseNumber, onNumberChanged, placeholder = "License number", error = errorFor(messages, "driversLicenseNumber"))
        DateFieldBlock("License expiry", draft.licenseExpiry, onExpiryChanged, error = errorFor(messages, "licenseExpiry"), warning = warningFor(messages, "licenseExpiry"))
        UploadField(
            label = "License front",
            documentType = DocumentType.DRIVERS_LICENSE,
            localUrl = draft.driversLicenseFrontUrl,
            serverDocument = serverDocuments[DocumentType.DRIVERS_LICENSE],
            error = errorFor(messages, "driversLicenseFront"),
            onUpload = { launchUpload(DocumentType.DRIVERS_LICENSE, draft.licenseExpiry.takeIf { it.isNotBlank() }) }
        )
        UploadField(
            label = "License back",
            documentType = DocumentType.DRIVERS_LICENSE_BACK,
            localUrl = draft.driversLicenseBackUrl,
            serverDocument = serverDocuments[DocumentType.DRIVERS_LICENSE_BACK],
            error = errorFor(messages, "driversLicenseBack"),
            onUpload = { launchUpload(DocumentType.DRIVERS_LICENSE_BACK, draft.licenseExpiry.takeIf { it.isNotBlank() }) }
        )
        CheckboxRow(
            title = "I have a GDL",
            checked = draft.hasGDL,
            onCheckedChange = onHasGdlChanged,
            supporting = "Turn this on if you will operate a commercial vehicle."
        )
        if (draft.hasGDL) {
            DateFieldBlock("GDL expiry", draft.gdlExpiry, onGdlExpiryChanged, error = errorFor(messages, "gdlExpiry"), warning = warningFor(messages, "gdlExpiry"))
            UploadField(
                label = "GDL photo",
                documentType = DocumentType.GDL,
                localUrl = draft.gdlUrl,
                serverDocument = serverDocuments[DocumentType.GDL],
                error = errorFor(messages, "gdlUrl"),
                onUpload = { launchUpload(DocumentType.GDL, draft.gdlExpiry.takeIf { it.isNotBlank() }) }
            )
        }
    }
}

@Composable
private fun VehicleDetailsStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    onVehicleTypeChanged: (VehicleType) -> Unit,
    onMakeChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onYearChanged: (String) -> Unit,
    onPlateChanged: (String) -> Unit,
    onColorChanged: (String) -> Unit,
    onChassisChanged: (String) -> Unit,
    onEngineChanged: (String) -> Unit,
    onOwnershipChanged: (VehicleOwnership) -> Unit,
    onOwnerNameChanged: (String) -> Unit
) {
    SectionCard(title = "Vehicle details", description = "Capture the vehicle that will be used for deliveries.") {
        EnumDropdownBlock(
            label = "Vehicle type",
            value = draft.vehicleType,
            options = DriverOnboardingViewModel.vehicleTypes,
            toLabel = ::vehicleLabel,
            onSelected = onVehicleTypeChanged,
            error = errorFor(messages, "vehicleType")
        )
        TextFieldBlock("Make", draft.vehicleMake, onMakeChanged, placeholder = "Toyota", error = errorFor(messages, "vehicleMake"))
        TextFieldBlock("Model", draft.vehicleModel, onModelChanged, placeholder = "Hiace", error = errorFor(messages, "vehicleModel"))
        TextFieldBlock("Year", draft.vehicleYear, onYearChanged, placeholder = "2022", keyboardType = KeyboardType.Number, error = errorFor(messages, "vehicleYear"))
        TextFieldBlock("License plate", draft.vehiclePlate, onPlateChanged, placeholder = "WXY1234", error = errorFor(messages, "vehiclePlate"))
        TextFieldBlock("Color", draft.vehicleColor, onColorChanged, placeholder = "White", error = errorFor(messages, "vehicleColor"))
        TextFieldBlock("Chassis number", draft.chassisNumber, onChassisChanged, placeholder = "Optional")
        TextFieldBlock("Engine number", draft.engineNumber, onEngineChanged, placeholder = "Optional")
        EnumDropdownBlock(
            label = "Ownership",
            value = draft.vehicleOwnership,
            options = DriverOnboardingViewModel.ownershipTypes,
            toLabel = ::ownershipLabel,
            onSelected = onOwnershipChanged,
            error = errorFor(messages, "vehicleOwnership")
        )
        if (draft.vehicleOwnership == VehicleOwnership.LEASED) {
            TextFieldBlock("Owner name", draft.ownerName, onOwnerNameChanged, placeholder = "Vehicle owner", error = errorFor(messages, "ownerName"))
        }
    }
}

@Composable
private fun VehicleDocumentsStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    onRoadTaxExpiryChanged: (String) -> Unit,
    onPuspakomExpiryChanged: (String) -> Unit,
    onApadNumberChanged: (String) -> Unit,
    onApadExpiryChanged: (String) -> Unit,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(title = "Vehicle documents", description = "Upload geran, road tax, and commercial permits where required.") {
        UploadField(
            label = "Vehicle registration / Geran",
            documentType = DocumentType.VEHICLE_REGISTRATION,
            localUrl = draft.vehicleRegistrationUrl,
            serverDocument = serverDocuments[DocumentType.VEHICLE_REGISTRATION],
            error = errorFor(messages, "vehicleRegistration"),
            onUpload = { launchUpload(DocumentType.VEHICLE_REGISTRATION, null) }
        )
        DateFieldBlock("Road tax expiry", draft.roadTaxExpiry, onRoadTaxExpiryChanged, error = errorFor(messages, "roadTaxExpiry"), warning = warningFor(messages, "roadTaxExpiry"))
        UploadField(
            label = "Road tax",
            documentType = DocumentType.ROAD_TAX,
            localUrl = draft.roadTaxUrl,
            serverDocument = serverDocuments[DocumentType.ROAD_TAX],
            error = errorFor(messages, "roadTax"),
            onUpload = { launchUpload(DocumentType.ROAD_TAX, draft.roadTaxExpiry.takeIf { it.isNotBlank() }) }
        )
        if (draft.vehicleType in setOf(VehicleType.PICKUP, VehicleType.VAN, VehicleType.VAN_7FT, VehicleType.VAN_9FT, VehicleType.LORRY_10FT, VehicleType.LORRY_14FT, VehicleType.LORRY_17FT, VehicleType.TRUCK)) {
            DateFieldBlock("PUSPAKOM expiry", draft.puspakomExpiry, onPuspakomExpiryChanged, error = errorFor(messages, "puspakomExpiry"), warning = warningFor(messages, "puspakomExpiry"))
            UploadField(
                label = "PUSPAKOM",
                documentType = DocumentType.PUSPAKOM,
                localUrl = draft.puspakomUrl,
                serverDocument = serverDocuments[DocumentType.PUSPAKOM],
                error = errorFor(messages, "puspakom"),
                onUpload = { launchUpload(DocumentType.PUSPAKOM, draft.puspakomExpiry.takeIf { it.isNotBlank() }) }
            )
            TextFieldBlock("APAD / LPKP permit number", draft.apadPermitNumber, onApadNumberChanged, placeholder = "Permit number", error = errorFor(messages, "apadPermitNumber"))
            DateFieldBlock("APAD / LPKP permit expiry", draft.apadPermitExpiry, onApadExpiryChanged, error = errorFor(messages, "apadPermitExpiry"), warning = warningFor(messages, "apadPermitExpiry"))
            UploadField(
                label = "APAD / LPKP permit",
                documentType = DocumentType.APAD_PERMIT,
                localUrl = draft.apadPermitUrl,
                serverDocument = serverDocuments[DocumentType.APAD_PERMIT],
                error = errorFor(messages, "apadPermit"),
                onUpload = { launchUpload(DocumentType.APAD_PERMIT, draft.apadPermitExpiry.takeIf { it.isNotBlank() }) }
            )
        }
    }
}

@Composable
private fun VehiclePhotosStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(title = "Vehicle photos", description = "Upload all five vehicle angles required by verification.") {
        UploadField("Front photo", DocumentType.VEHICLE_PHOTO_FRONT, draft.vehicleFrontUrl, serverDocuments[DocumentType.VEHICLE_PHOTO_FRONT], errorFor(messages, "vehicleFront")) { launchUpload(DocumentType.VEHICLE_PHOTO_FRONT, null) }
        UploadField("Back photo", DocumentType.VEHICLE_PHOTO_BACK, draft.vehicleBackUrl, serverDocuments[DocumentType.VEHICLE_PHOTO_BACK], errorFor(messages, "vehicleBack")) { launchUpload(DocumentType.VEHICLE_PHOTO_BACK, null) }
        UploadField("Left photo", DocumentType.VEHICLE_PHOTO_LEFT, draft.vehicleLeftUrl, serverDocuments[DocumentType.VEHICLE_PHOTO_LEFT], errorFor(messages, "vehicleLeft")) { launchUpload(DocumentType.VEHICLE_PHOTO_LEFT, null) }
        UploadField("Right photo", DocumentType.VEHICLE_PHOTO_RIGHT, draft.vehicleRightUrl, serverDocuments[DocumentType.VEHICLE_PHOTO_RIGHT], errorFor(messages, "vehicleRight")) { launchUpload(DocumentType.VEHICLE_PHOTO_RIGHT, null) }
        UploadField("Interior photo", DocumentType.VEHICLE_PHOTO_INTERIOR, draft.vehicleInteriorUrl, serverDocuments[DocumentType.VEHICLE_PHOTO_INTERIOR], errorFor(messages, "vehicleInterior")) { launchUpload(DocumentType.VEHICLE_PHOTO_INTERIOR, null) }
    }
}

@Composable
private fun InsuranceStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    onInsurerChanged: (String) -> Unit,
    onPolicyChanged: (String) -> Unit,
    onCoverageChanged: (InsuranceCoverageType) -> Unit,
    onExpiryChanged: (String) -> Unit,
    onCommercialCoverChanged: (Boolean) -> Unit,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(title = "Insurance", description = "Capture policy details and upload the supporting insurance document.") {
        TextFieldBlock("Insurer", draft.insurerName, onInsurerChanged, placeholder = "Insurer name", error = errorFor(messages, "insurerName"))
        TextFieldBlock("Policy number", draft.insurancePolicyNumber, onPolicyChanged, placeholder = "Policy number", error = errorFor(messages, "insurancePolicyNumber"))
        EnumDropdownBlock(
            label = "Coverage type",
            value = draft.insuranceCoverageType,
            options = DriverOnboardingViewModel.insuranceCoverageTypes,
            toLabel = ::coverageLabel,
            onSelected = onCoverageChanged,
            error = errorFor(messages, "insuranceCoverageType")
        )
        DateFieldBlock("Insurance expiry", draft.insuranceExpiry, onExpiryChanged, error = errorFor(messages, "insuranceExpiry"), warning = warningFor(messages, "insuranceExpiry"))
        CheckboxRow(
            title = "Commercial cover included",
            checked = draft.hasCommercialCover,
            onCheckedChange = onCommercialCoverChanged,
            supporting = "Required for commercial operations."
        )
        UploadField(
            label = "Insurance document",
            documentType = DocumentType.INSURANCE,
            localUrl = draft.insuranceUrl,
            serverDocument = serverDocuments[DocumentType.INSURANCE],
            error = errorFor(messages, "insuranceUrl"),
            onUpload = { launchUpload(DocumentType.INSURANCE, draft.insuranceExpiry.takeIf { it.isNotBlank() }) }
        )
    }
}

@Composable
private fun BankStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    onBankChanged: (String) -> Unit,
    onAccountNumberChanged: (String) -> Unit,
    onAccountHolderChanged: (String) -> Unit,
    onDuitNowChanged: (String) -> Unit,
    onTngChanged: (String) -> Unit,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(title = "Bank and payout", description = "Bank statement is required. DuitNow and TNG are optional payout identifiers.") {
        DropdownBlock(
            label = "Bank",
            value = draft.bankName,
            options = DriverOnboardingViewModel.banks,
            onSelected = onBankChanged,
            error = errorFor(messages, "bankName")
        )
        TextFieldBlock("Account number", draft.bankAccountNumber, onAccountNumberChanged, placeholder = "Bank account number", keyboardType = KeyboardType.Number, error = errorFor(messages, "bankAccountNumber"))
        TextFieldBlock("Account holder", draft.bankAccountHolder, onAccountHolderChanged, placeholder = "Must match MyKad name", error = errorFor(messages, "bankAccountHolder"), warning = warningFor(messages, "bankAccountHolder"))
        UploadField(
            label = "Bank statement",
            documentType = DocumentType.BANK_STATEMENT,
            localUrl = draft.bankStatementUrl,
            serverDocument = serverDocuments[DocumentType.BANK_STATEMENT],
            error = errorFor(messages, "bankStatement"),
            onUpload = { launchUpload(DocumentType.BANK_STATEMENT, null) }
        )
        TextFieldBlock("DuitNow ID (optional)", draft.duitNowId, onDuitNowChanged, placeholder = "Optional")
        TextFieldBlock("TNG eWallet ID (optional)", draft.tngEwalletId, onTngChanged, placeholder = "Optional")
    }
}

@Composable
private fun TaxStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    onLhdnChanged: (String) -> Unit,
    onSstChanged: (String) -> Unit
) {
    SectionCard(title = "Tax", description = "These fields are optional during onboarding and can be completed later.") {
        TextFieldBlock("LHDN tax file number", draft.lhdnTaxNumber, onLhdnChanged, placeholder = "Optional")
        TextFieldBlock("SST number", draft.sstNumber, onSstChanged, placeholder = "Optional")
        ValidationText(error = errorFor(messages, "tax"), warning = warningFor(messages, "tax"))
    }
}

@Composable
private fun BackgroundStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    onPdpaChanged: (Boolean) -> Unit,
    onBackgroundConsentChanged: (Boolean) -> Unit,
    onNoOffencesChanged: (Boolean) -> Unit,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(title = "Background check consent", description = "PDPA consent, background check consent, declaration, and police clearance are all required.") {
        CheckboxRow("I consent to PDPA processing", draft.pdpaConsent, onPdpaChanged, error = errorFor(messages, "pdpaConsent"))
        CheckboxRow("I consent to background checks", draft.backgroundCheckConsent, onBackgroundConsentChanged, error = errorFor(messages, "backgroundCheckConsent"))
        CheckboxRow("I declare I have no disqualifying offences", draft.noOffencesDeclared, onNoOffencesChanged, error = errorFor(messages, "noOffencesDeclared"))
        UploadField(
            label = "Police clearance",
            documentType = DocumentType.POLICE_CLEARANCE,
            localUrl = draft.policeClearanceUrl,
            serverDocument = serverDocuments[DocumentType.POLICE_CLEARANCE],
            error = errorFor(messages, "policeClearance"),
            onUpload = { launchUpload(DocumentType.POLICE_CLEARANCE, null) }
        )
    }
}

@Composable
private fun ReviewStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    onAgreementChanged: (Boolean) -> Unit
) {
    SectionCard(title = "Review and agreement", description = "Review the payload that will be submitted to the backend, then accept the partner agreement.") {
        ReviewCard("Phone", listOf("${draft.phone}"))
        ReviewCard("Identity", listOfNotNull(
            "Nationality: ${draft.nationality?.name ?: "-"}",
            draft.mykadNumber.takeIf { it.isNotBlank() }?.let { "MyKad: $it" },
            draft.passportNumber.takeIf { it.isNotBlank() }?.let { "Passport: $it" },
            draft.plksNumber.takeIf { it.isNotBlank() }?.let { "PLKS: $it" }
        ))
        ReviewCard("Personal", listOf(
            draft.fullName,
            draft.dateOfBirth,
            draft.gender,
            listOf(draft.addressLine1, draft.addressLine2, draft.city, draft.postcode, draft.state?.let(::enumLabel)).filterNotNull().filter { it.isNotBlank() }.joinToString(", "),
            "Emergency: ${draft.emergencyContactName} / ${draft.emergencyContactRelation} / ${draft.emergencyContactPhone}"
        ))
        ReviewCard("License", listOfNotNull(
            draft.licenseClass?.name?.let { "Class: $it" },
            "Number: ${draft.driversLicenseNumber}",
            draft.licenseExpiry.takeIf { it.isNotBlank() }?.let { "Expiry: $it" },
            if (draft.hasGDL) "GDL expiry: ${draft.gdlExpiry}" else null
        ))
        ReviewCard("Vehicle", listOfNotNull(
            draft.vehicleType?.let { "Type: ${vehicleLabel(it)}" },
            "${draft.vehicleMake} ${draft.vehicleModel}".trim().takeIf { it.isNotBlank() },
            draft.vehicleYear.takeIf { it.isNotBlank() }?.let { "Year: $it" },
            draft.vehiclePlate.takeIf { it.isNotBlank() }?.let { "Plate: $it" },
            draft.vehicleOwnership?.let { "Ownership: ${ownershipLabel(it)}" }
        ))
        ReviewCard("Insurance and payout", listOfNotNull(
            draft.insurerName.takeIf { it.isNotBlank() }?.let { "Insurer: $it" },
            draft.insurancePolicyNumber.takeIf { it.isNotBlank() }?.let { "Policy: $it" },
            draft.bankName.takeIf { it.isNotBlank() }?.let { "Bank: $it" },
            draft.bankAccountNumber.takeIf { it.isNotBlank() }?.let { "Account: $it" },
            draft.bankAccountHolder.takeIf { it.isNotBlank() }?.let { "Holder: $it" }
        ))
        ReviewCard("Tax and declarations", listOfNotNull(
            draft.lhdnTaxNumber.takeIf { it.isNotBlank() }?.let { "LHDN: $it" },
            draft.sstNumber.takeIf { it.isNotBlank() }?.let { "SST: $it" },
            "PDPA consent: ${if (draft.pdpaConsent) "Yes" else "No"}",
            "Background check consent: ${if (draft.backgroundCheckConsent) "Yes" else "No"}",
            "No offences declared: ${if (draft.noOffencesDeclared) "Yes" else "No"}"
        ))
        Card(colors = CardDefaults.cardColors(containerColor = BlueTint)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Agreement version: $DRIVER_ONBOARDING_AGREEMENT_VERSION", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                CheckboxRow(
                    title = "I accept the CarryOn partner terms and agreement",
                    checked = draft.agreementAccepted,
                    onCheckedChange = onAgreementChanged,
                    error = errorFor(messages, "agreementAccepted")
                )
                Text("Timestamp is captured server-side on submission.", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ReviewCard(title: String, lines: List<String>) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            lines.filter { it.isNotBlank() }.forEach { line ->
                Text(line, color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(description, color = TextMuted, fontSize = 14.sp, lineHeight = 20.sp)
                content()
            }
        )
    }
}

@Composable
private fun SelectionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick).border(1.dp, if (selected) Blue else Color(0xFFD8DEE8), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = if (selected) BlueTint else Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, color = TextMuted, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun TextFieldBlock(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    warning: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = { Text(placeholder) }
        )
        ValidationText(error = error, warning = warning)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFieldBlock(
    label: String,
    value: String,
    onValueSelected: (String) -> Unit,
    error: String? = null,
    warning: String? = null
) {
    var open by remember { mutableStateOf(false) }
    val state = androidx.compose.material3.rememberDatePickerState()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        OutlinedButton(
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (value.isBlank()) "YYYY-MM-DD" else value,
                modifier = Modifier.fillMaxWidth(),
                color = if (value.isBlank()) TextMuted else TextPrimary
            )
        }
        ValidationText(error = error, warning = warning)
    }

    if (open) {
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) onValueSelected(formatDate(millis))
                        open = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun DropdownBlock(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        ValidationText(error = error, warning = null)
    }
}

@Composable
private fun <T> EnumDropdownBlock(
    label: String,
    value: T?,
    options: List<T>,
    toLabel: (T) -> String,
    onSelected: (T) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        Box {
            OutlinedTextField(
                value = value?.let(toLabel).orEmpty(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(toLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        ValidationText(error = error, warning = null)
    }
}

@Composable
private fun UploadField(
    label: String,
    documentType: DocumentType,
    localUrl: String,
    serverDocument: Document?,
    error: String?,
    onUpload: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                StatusChip(localUrl = localUrl, serverDocument = serverDocument)
            }
            OutlinedButton(onClick = onUpload) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (localUrl.isBlank()) "Choose image" else "Replace image")
            }
            serverDocument?.takeIf { it.status == DocumentStatus.REJECTED && !it.rejectionReason.isNullOrBlank() }?.let {
                Text("Rejected: ${it.rejectionReason}", color = Danger, fontSize = 12.sp)
            }
            ValidationText(error = error, warning = null)
        }
    }
}

@Composable
private fun StatusChip(localUrl: String, serverDocument: Document?) {
    val (label, color) = when {
        serverDocument?.status == DocumentStatus.APPROVED -> "Approved" to Success
        serverDocument?.status == DocumentStatus.REJECTED -> "Rejected" to Danger
        localUrl.isNotBlank() || serverDocument != null -> "Uploaded" to Blue
        else -> "Required" to TextMuted
    }
    Box(
        modifier = Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    supporting: String? = null,
    error: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(title, color = TextPrimary)
        }
        if (supporting != null) Text(supporting, color = TextMuted, fontSize = 12.sp)
        ValidationText(error = error, warning = null)
    }
}

@Composable
private fun ValidationText(error: String?, warning: String?) {
    when {
        error != null -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Danger, modifier = Modifier.size(14.dp))
            Text(error, color = Danger, fontSize = 12.sp)
        }
        warning != null -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(14.dp))
            Text(warning, color = Warning, fontSize = 12.sp)
        }
    }
}

private fun errorFor(messages: List<ValidationMessage>, field: String): String? {
    return messages.firstOrNull { it.field == field && !it.isWarning }?.message
}

private fun warningFor(messages: List<ValidationMessage>, field: String): String? {
    return messages.firstOrNull { it.field == field && it.isWarning }?.message
}

private fun identityUrl(draft: DriverOnboardingDraft, type: DocumentType): String {
    return draft.identityDocuments.firstOrNull { it.type == type }?.imageUrl.orEmpty()
}

private fun enumLabel(state: MalaysianState): String = state.name.replace('_', ' ')
private fun vehicleLabel(type: VehicleType): String = type.displayName
private fun ownershipLabel(ownership: VehicleOwnership): String = ownership.name.replace('_', ' ')
private fun coverageLabel(coverage: InsuranceCoverageType): String = coverage.name.replace('_', ' ')

private fun formatDate(millis: Long): String {
    val localDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
    val month = localDate.monthNumber.toString().padStart(2, '0')
    val day = localDate.dayOfMonth.toString().padStart(2, '0')
    return "${localDate.year}-$month-$day"
}
