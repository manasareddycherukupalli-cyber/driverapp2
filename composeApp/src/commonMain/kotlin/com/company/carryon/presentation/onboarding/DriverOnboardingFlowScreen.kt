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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
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
import com.company.carryon.data.model.LicenseClass
import com.company.carryon.data.model.MalaysianState
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.ValidationMessage
import com.company.carryon.data.model.VehicleOwnership
import com.company.carryon.data.model.VehicleType
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.Gray100
import com.company.carryon.presentation.theme.Gray200
import com.company.carryon.presentation.theme.Gray300
import com.company.carryon.presentation.theme.Gray50
import com.company.carryon.presentation.theme.Gray700
import com.company.carryon.presentation.theme.Gray900
import com.company.carryon.presentation.theme.Orange100
import com.company.carryon.presentation.theme.Orange500
import com.company.carryon.presentation.util.rememberImagePickerLauncher
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch

private val Bg = Gray50
private val Blue = Orange500
private val BlueTint = Orange100
private val TextPrimary = Gray900
private val TextMuted = Gray700
private val Success = Orange500
private val WarningColor = Color(0xFF2F80ED)
private val Danger = Color(0xFF2F80ED)
private val FieldBlue = Color(0xFF2F80ED)
private val FieldTextBlack = Color(0xFF000000)

private val stepTitles = listOf(
    "Phone",
    "Nationality",
    "Identity",
    "Personal",
    "License",
    "Vehicle",
    "Vehicle Docs",
    "Vehicle Photos",
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
            val contentScrollState = rememberScrollState()
            val scope = rememberCoroutineScope()

            LaunchedEffect(submitState) {
                when (submitState) {
                    is UiState.Success -> navigator.navigateAndClearStack(Screen.VerificationStatus)
                    is UiState.Error -> scope.launch { contentScrollState.animateScrollTo(0) }
                    else -> Unit
                }
            }

            Scaffold(
                containerColor = Bg,
                topBar = {
                    DriveAppTopBar(
                        title = "Driver verification",
                        onBackClick = {
                            if (currentStep == 1) {
                                navigator.navigateAndClearStack(Screen.Login)
                            } else {
                                viewModel.goBack()
                            }
                        },
                        onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
                        showTitle = false
                    )
                },
                bottomBar = {
                    FooterActions(
                        step = currentStep,
                        canGoBack = true,
                        isLoading = submitState is UiState.Loading,
                        hasBlockingErrors = blockingErrors.isNotEmpty(),
                        submitError = (submitState as? UiState.Error)?.message,
                        onBack = {
                            if (currentStep == 1) {
                                navigator.navigateAndClearStack(Screen.Login)
                            } else {
                                viewModel.goBack()
                            }
                        },
                        onNext = {
                            if (currentStep == DriverOnboardingViewModel.TOTAL_STEPS) {
                                if (submitState is UiState.Error) {
                                    // Testing shortcut: allow opening status screen even when submission is blocked by backend errors.
                                    navigator.navigateAndClearStack(Screen.VerificationStatus)
                                } else {
                                    viewModel.submit()
                                }
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
                        .verticalScroll(contentScrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Driver verification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Step $currentStep of ${DriverOnboardingViewModel.TOTAL_STEPS}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

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
                            onSelected = { viewModel.updateDraft { it.copy(nationality = DriverNationality.MALAYSIAN) } }
                        )
                        3 -> IdentityStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            onMyKadChanged = { value ->
                                viewModel.updateDraft {
                                    it.copy(mykadNumber = value.filter(Char::isDigit).take(12))
                                }
                            },
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
                            onMakeChanged = { value ->
                                viewModel.updateDraft {
                                    val model = it.vehicleModel.takeIf { currentModel ->
                                        currentModel in DriverOnboardingViewModel.vehicleModelsForMake(value)
                                    }.orEmpty()
                                    it.copy(vehicleMake = value, vehicleModel = model)
                                }
                            },
                            onModelChanged = { value -> viewModel.updateDraft { it.copy(vehicleModel = value) } },
                            onYearChanged = { value -> viewModel.updateDraft { it.copy(vehicleYear = value) } },
                            onPlateChanged = { value -> viewModel.updateDraft { it.copy(vehiclePlate = value) } },
                            onColorChanged = { value -> viewModel.updateDraft { it.copy(vehicleColor = value) } },
                            onOwnershipChanged = { value -> viewModel.updateDraft { it.copy(vehicleOwnership = value) } },
                            onOwnerNameChanged = { value -> viewModel.updateDraft { it.copy(ownerName = value) } }
                        )
                        7 -> VehicleDocumentsStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            launchUpload = launchUpload
                        )
                        8 -> VehiclePhotosStep(
                            draft = draft,
                            messages = messages,
                            serverDocuments = serverDocuments,
                            launchUpload = launchUpload
                        )
                        9 -> BackgroundStep(
                            draft = draft,
                            messages = messages,
                            onPdpaChanged = { value -> viewModel.updateDraft { it.copy(pdpaConsent = value) } },
                            onBackgroundConsentChanged = { value -> viewModel.updateDraft { it.copy(backgroundCheckConsent = value) } },
                            onNoOffencesChanged = { value -> viewModel.updateDraft { it.copy(noOffencesDeclared = value) } }
                        )
                        10 -> ReviewStep(
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
                        color = if (selected) Blue else Gray300,
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
                                color = if (completed) Success else if (selected) Blue else Gray200,
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
    submitError: String?,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        submitError?.let { message ->
            Text(message, color = FieldBlue, fontSize = 12.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = canGoBack && !isLoading,
                border = BorderStroke(1.dp, FieldBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FieldBlue)
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
            error = errorFor(messages, "phone"),
            labelColor = Color(0xFF000000),
            borderColor = Blue,
            valueColor = FieldTextBlack
        )
    }
}

@Composable
private fun NationalityStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    onSelected: () -> Unit
) {
    SectionCard(
        title = "Nationality",
        description = "Carry On currently accepts Malaysian drivers only."
    ) {
        SelectionCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Malaysian",
            subtitle = "MyKad front, back, and selfie",
            selected = true,
            onClick = onSelected
        )
        ValidationText(error = errorFor(messages, "nationality"), warning = warningFor(messages, "nationality"))
    }
}

@Composable
private fun IdentityStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    onMyKadChanged: (String) -> Unit,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(
        title = "Identity upload",
        description = "Upload MyKad front, MyKad back, and a selfie."
    ) {
        TextFieldBlock(
            label = "MyKad number",
            value = draft.mykadNumber,
            onValueChange = onMyKadChanged,
            placeholder = "YYMMDDPB####",
            keyboardType = KeyboardType.Number,
            error = errorFor(messages, "mykadNumber"),
            borderColor = Blue,
            valueColor = Blue
        )
        UploadField(
            label = "MyKad front",
            documentType = DocumentType.MYKAD_FRONT,
            localUrl = identityUrl(draft, DocumentType.MYKAD_FRONT),
            serverDocument = serverDocuments[DocumentType.MYKAD_FRONT],
            error = errorFor(messages, "mykadFront"),
            containerColor = Color(0x33A6D2F3),
            onUpload = { launchUpload(DocumentType.MYKAD_FRONT, null) }
        )
        UploadField(
            label = "MyKad back",
            documentType = DocumentType.MYKAD_BACK,
            localUrl = identityUrl(draft, DocumentType.MYKAD_BACK),
            serverDocument = serverDocuments[DocumentType.MYKAD_BACK],
            error = errorFor(messages, "mykadBack"),
            containerColor = Color(0x33A6D2F3),
            onUpload = { launchUpload(DocumentType.MYKAD_BACK, null) }
        )
        UploadField(
            label = "Selfie",
            documentType = DocumentType.SELFIE,
            localUrl = identityUrl(draft, DocumentType.SELFIE),
            serverDocument = serverDocuments[DocumentType.SELFIE],
            error = errorFor(messages, "selfie"),
            containerColor = Color(0x33A6D2F3),
            onUpload = { launchUpload(DocumentType.SELFIE, null) }
        )
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
        TextFieldBlock(
            "Full name as per IC",
            draft.fullName,
            onNameChanged,
            placeholder = "Full legal name",
            error = errorFor(messages, "fullName"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        DateFieldBlock(
            "Date of birth",
            draft.dateOfBirth,
            onDobChanged,
            error = errorFor(messages, "dateOfBirth"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        DropdownBlock(
            label = "Gender",
            value = draft.gender,
            options = DriverOnboardingViewModel.genders,
            onSelected = onGenderChanged,
            error = errorFor(messages, "gender"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        TextFieldBlock(
            "Address line 1",
            draft.addressLine1,
            onAddressLine1Changed,
            placeholder = "Street address",
            error = errorFor(messages, "addressLine1"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        TextFieldBlock(
            "Address line 2",
            draft.addressLine2,
            onAddressLine2Changed,
            placeholder = "Apartment, unit, etc. (optional)",
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        TextFieldBlock(
            "City",
            draft.city,
            onCityChanged,
            placeholder = "City",
            error = errorFor(messages, "city"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        TextFieldBlock(
            "Postcode",
            draft.postcode,
            onPostcodeChanged,
            placeholder = "5-digit postcode",
            keyboardType = KeyboardType.Number,
            error = errorFor(messages, "postcode"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        EnumDropdownBlock(
            label = "State",
            value = draft.state,
            options = DriverOnboardingViewModel.states,
            toLabel = ::enumLabel,
            onSelected = onStateChanged,
            error = errorFor(messages, "state"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        TextFieldBlock(
            "Emergency contact name",
            draft.emergencyContactName,
            onEmergencyNameChanged,
            placeholder = "Emergency contact",
            error = errorFor(messages, "emergencyContactName"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        TextFieldBlock(
            "Emergency contact relation",
            draft.emergencyContactRelation,
            onEmergencyRelationChanged,
            placeholder = "Spouse, parent, sibling",
            error = errorFor(messages, "emergencyContactRelation"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
        TextFieldBlock(
            "Emergency contact phone",
            draft.emergencyContactPhone,
            onEmergencyPhoneChanged,
            placeholder = "+60 12-345 6789",
            keyboardType = KeyboardType.Phone,
            error = errorFor(messages, "emergencyContactPhone"),
            borderColor = FieldBlue,
            valueColor = FieldTextBlack
        )
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
        TextFieldBlock(
            "License number",
            draft.driversLicenseNumber,
            onNumberChanged,
            placeholder = "License number",
            error = errorFor(messages, "driversLicenseNumber"),
            borderColor = FieldBlue
        )
        DateFieldBlock("License expiry", draft.licenseExpiry, onExpiryChanged, error = errorFor(messages, "licenseExpiry"), warning = warningFor(messages, "licenseExpiry"))
        UploadField(
            label = "License front",
            documentType = DocumentType.DRIVERS_LICENSE,
            localUrl = draft.driversLicenseFrontUrl,
            serverDocument = serverDocuments[DocumentType.DRIVERS_LICENSE],
            error = errorFor(messages, "driversLicenseFront"),
            containerColor = Color(0x33A6D2F3),
            onUpload = { launchUpload(DocumentType.DRIVERS_LICENSE, draft.licenseExpiry.takeIf { it.isNotBlank() }) }
        )
        UploadField(
            label = "License back",
            documentType = DocumentType.DRIVERS_LICENSE_BACK,
            localUrl = draft.driversLicenseBackUrl,
            serverDocument = serverDocuments[DocumentType.DRIVERS_LICENSE_BACK],
            error = errorFor(messages, "driversLicenseBack"),
            containerColor = Color(0x33A6D2F3),
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
                containerColor = Color(0x33A6D2F3),
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
    onOwnershipChanged: (VehicleOwnership) -> Unit,
    onOwnerNameChanged: (String) -> Unit
) {
    val modelOptions = DriverOnboardingViewModel.vehicleModelsForMake(draft.vehicleMake)
    SectionCard(title = "Vehicle details", description = "Capture the vehicle that will be used for deliveries.") {
        EnumDropdownBlock(
            label = "Vehicle type",
            value = draft.vehicleType,
            options = DriverOnboardingViewModel.vehicleTypes,
            toLabel = ::vehicleLabel,
            onSelected = onVehicleTypeChanged,
            error = errorFor(messages, "vehicleType")
        )
        DropdownBlock(
            label = "Brand",
            value = draft.vehicleMake,
            options = DriverOnboardingViewModel.vehicleBrands,
            onSelected = onMakeChanged,
            error = errorFor(messages, "vehicleMake")
        )
        DropdownBlock(
            label = "Model",
            value = draft.vehicleModel,
            options = modelOptions,
            onSelected = onModelChanged,
            error = errorFor(messages, "vehicleModel")
        )
        TextFieldBlock("Year", draft.vehicleYear, onYearChanged, placeholder = "2022", keyboardType = KeyboardType.Number, error = errorFor(messages, "vehicleYear"))
        TextFieldBlock("License plate", draft.vehiclePlate, onPlateChanged, placeholder = "WXY1234", error = errorFor(messages, "vehiclePlate"))
        TextFieldBlock("Color", draft.vehicleColor, onColorChanged, placeholder = "White", error = errorFor(messages, "vehicleColor"))
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
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(title = "Vehicle documents", description = "Upload the vehicle registration document.") {
        UploadField(
            label = "Vehicle registration",
            documentType = DocumentType.VEHICLE_REGISTRATION,
            localUrl = draft.vehicleRegistrationUrl,
            serverDocument = serverDocuments[DocumentType.VEHICLE_REGISTRATION],
            error = errorFor(messages, "vehicleRegistration"),
            onUpload = { launchUpload(DocumentType.VEHICLE_REGISTRATION, null) }
        )
    }
}

@Composable
private fun VehiclePhotosStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    serverDocuments: Map<DocumentType, Document>,
    launchUpload: (DocumentType, String?) -> Unit
) {
    SectionCard(title = "Vehicle photos", description = "Upload the front and back of the vehicle.") {
        UploadField("Front photo", DocumentType.VEHICLE_PHOTO_FRONT, draft.vehicleFrontUrl, serverDocuments[DocumentType.VEHICLE_PHOTO_FRONT], errorFor(messages, "vehicleFront")) { launchUpload(DocumentType.VEHICLE_PHOTO_FRONT, null) }
        UploadField("Back photo", DocumentType.VEHICLE_PHOTO_BACK, draft.vehicleBackUrl, serverDocuments[DocumentType.VEHICLE_PHOTO_BACK], errorFor(messages, "vehicleBack")) { launchUpload(DocumentType.VEHICLE_PHOTO_BACK, null) }
    }
}

@Composable
private fun BackgroundStep(
    draft: DriverOnboardingDraft,
    messages: List<ValidationMessage>,
    onPdpaChanged: (Boolean) -> Unit,
    onBackgroundConsentChanged: (Boolean) -> Unit,
    onNoOffencesChanged: (Boolean) -> Unit
) {
    SectionCard(title = "Background check consent", description = "PDPA consent, background check consent, and declaration are required.") {
        CheckboxRow("I consent to PDPA processing", draft.pdpaConsent, onPdpaChanged, error = errorFor(messages, "pdpaConsent"))
        CheckboxRow("I consent to background checks", draft.backgroundCheckConsent, onBackgroundConsentChanged, error = errorFor(messages, "backgroundCheckConsent"))
        CheckboxRow("I declare I have no disqualifying offences", draft.noOffencesDeclared, onNoOffencesChanged, error = errorFor(messages, "noOffencesDeclared"))
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
            draft.mykadNumber.takeIf { it.isNotBlank() }?.let { "MyKad: $it" }
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
        ReviewCard("Payout", listOf("Stripe payout setup happens after approval from the Wallet screen. Drivers cannot go online until Stripe payouts are enabled."))
        ReviewCard("Declarations", listOf(
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
        modifier = modifier.clickable(onClick = onClick).border(1.dp, if (selected) Blue else Gray300, RoundedCornerShape(16.dp)),
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
    warning: String? = null,
    labelColor: Color = TextPrimary,
    borderColor: Color = FieldBlue,
    valueColor: Color? = null,
    errorColor: Color = Danger
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = labelColor, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = valueColor ?: MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = valueColor ?: MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        ValidationText(error = error, warning = warning, errorColor = errorColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFieldBlock(
    label: String,
    value: String,
    onValueSelected: (String) -> Unit,
    error: String? = null,
    warning: String? = null,
    borderColor: Color = FieldBlue,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    errorColor: Color = Danger
) {
    var open by remember { mutableStateOf(false) }
    val state = androidx.compose.material3.rememberDatePickerState()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueSelected,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            placeholder = { Text("YYYY-MM-DD", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                TextButton(onClick = { open = true }) {
                    Text("Pick", color = valueColor)
                }
            },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = valueColor,
                unfocusedTextColor = valueColor,
                cursorColor = borderColor
            )
        )
        ValidationText(error = error, warning = warning, errorColor = errorColor)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownBlock(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    error: String? = null,
    borderColor: Color = FieldBlue,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = valueColor,
                    unfocusedTextColor = valueColor
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdownBlock(
    label: String,
    value: T?,
    options: List<T>,
    toLabel: (T) -> String,
    onSelected: (T) -> Unit,
    error: String? = null,
    borderColor: Color = FieldBlue,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value?.let(toLabel).orEmpty(),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = valueColor,
                    unfocusedTextColor = valueColor
                )
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
    containerColor: Color = Color(0x33A6D2F3),
    errorColor: Color = Danger,
    onUpload: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                StatusChip(localUrl = localUrl, serverDocument = serverDocument)
            }
            OutlinedButton(
                onClick = onUpload,
                border = BorderStroke(1.dp, Blue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue)
            ) {
                Icon(
                    Icons.Filled.CloudUpload,
                    contentDescription = null,
                    tint = Blue
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (localUrl.isBlank()) "Choose image" else "Replace image",
                    color = Blue
                )
            }
            serverDocument?.takeIf { it.status == DocumentStatus.REJECTED && !it.rejectionReason.isNullOrBlank() }?.let {
                Text("Rejected: ${it.rejectionReason}", color = Danger, fontSize = 12.sp)
            }
            ValidationText(error = error, warning = null, errorColor = errorColor)
        }
    }
}

@Composable
private fun StatusChip(localUrl: String, serverDocument: Document?) {
    val (label, color) = when {
        serverDocument?.status == DocumentStatus.APPROVED -> "Approved" to Success
        serverDocument?.status == DocumentStatus.REJECTED -> "Rejected" to Danger
        localUrl.isNotBlank() || serverDocument != null -> "Uploaded" to Blue
        else -> "Required" to Blue
    }
    Box(
        modifier = Modifier.background(
            if (label == "Uploaded" || label == "Required") Color.White else color.copy(alpha = 0.12f),
            RoundedCornerShape(999.dp)
        ).padding(horizontal = 10.dp, vertical = 4.dp)
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
private fun ValidationText(error: String?, warning: String?, errorColor: Color = Danger) {
    when {
        error != null -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = errorColor, modifier = Modifier.size(14.dp))
            Text(error, color = errorColor, fontSize = 12.sp)
        }
        warning != null -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningColor, modifier = Modifier.size(14.dp))
            Text(warning, color = WarningColor, fontSize = 12.sp)
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

private fun formatDate(millis: Long): String {
    val localDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
    val month = localDate.monthNumber.toString().padStart(2, '0')
    val day = localDate.dayOfMonth.toString().padStart(2, '0')
    return "${localDate.year}-$month-$day"
}
