package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import drive_app.composeapp.generated.resources.*
import com.company.carryon.data.model.Driver
import com.company.carryon.data.model.Document
import com.company.carryon.data.model.DocumentStatus
import com.company.carryon.data.model.DocumentType
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.VerificationStatus
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.DriveAppBottomBar
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.navigation.rememberDriveBottomNavItems
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val VerifyBlue = Color(0xFF2F80ED)
private val VerifyBg = Color(0xFFF3F4F9)
private val SoftBlue = Color(0xFFAECFF3)
private val SoftBlue20 = Color(0x2EAECFF3)
private val SoftBlue30 = Color(0x40AFC7E7)
private val PureBlack = Color(0xFF000000)

@Composable
fun VerificationStatusScreen(navigator: AppNavigator, viewModel: AuthViewModel) {
    val latestAuthResponse by viewModel.latestAuthResponse.collectAsState()
    val verificationState by viewModel.verificationState.collectAsState()

    if (latestAuthResponse == null) {
        LaunchedEffect(Unit) {
            navigator.navigateAndClearStack(Screen.Onboarding)
        }
        return
    }

    LaunchedEffect(Unit) { viewModel.checkVerificationStatus() }

    when (val state = verificationState) {
        is UiState.Loading, UiState.Idle -> {
            val strings = LocalStrings.current
            Box(modifier = Modifier.fillMaxSize().background(VerifyBg), contentAlignment = Alignment.Center) {
                Text(strings.checkingStatus, color = PureBlack)
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().background(VerifyBg), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            val driver = state.data
            if (driver.verificationStatus == VerificationStatus.APPROVED) {
                ApprovedFlow(navigator)
            } else {
                VerificationSummary(driver = driver, navigator = navigator)
            }
        }
    }
}

@Composable
private fun VerificationSummary(driver: Driver, navigator: AppNavigator) {
    val strings = LocalStrings.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = VerifyBlue, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Precision Navigator", fontWeight = FontWeight.Bold, color = PureBlack, fontSize = 18.sp)
                }
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(22.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(SoftBlue, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.verify_safety_icon),
                            contentDescription = null,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(strings.safetyProtocol, color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(strings.identityVerification, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = PureBlack)
                Text(
                    strings.completeStepsPrompt,
                    color = PureBlack.copy(alpha = 0.65f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                val personalDone = isPersonalIdentityComplete(driver)
                val vehicleDone = driver.vehicleDetails != null

                val personalStatus = resolvePersonalIdentityStep(driver)
                StatusCard(
                    iconRes = Res.drawable.verify_personal_identity,
                    title = strings.personalIdentity,
                    desc = personalStatus.description,
                    pill = personalStatus.pill,
                    pillIconRes = personalStatus.pillIcon,
                    progress = personalStatus.progress,
                    primary = personalStatus.primaryAction,
                    onPrimary = { navigator.navigateTo(Screen.PersonalIdentity) },
                    onCardClick = { navigator.navigateTo(Screen.PersonalIdentity) }
                )
                val vehicleStatus = resolveVehicleDetailsStep(
                    hasVehicleInfo = driver.vehicleDetails != null,
                    documents = driver.documents
                )
                StatusCard(
                    iconRes = Res.drawable.verify_vehicle_details,
                    title = strings.vehicleDetails,
                    desc = vehicleStatus.description,
                    pill = vehicleStatus.pill,
                    pillIconRes = vehicleStatus.pillIcon,
                    progress = vehicleStatus.progress,
                    onCardClick = {
                        if (personalDone) navigator.navigateTo(Screen.VehicleDetailsInput)
                        else navigator.navigateTo(Screen.PersonalIdentity)
                    }
                )
                val identityStatus = resolveIdentityVerificationStep(driver.documents)
                StatusCard(
                    iconRes = Res.drawable.verify_identity_check,
                    title = strings.identityVerification,
                    desc = identityStatus.description,
                    pill = identityStatus.pill,
                    pillIconRes = identityStatus.pillIcon,
                    progress = identityStatus.progress,
                    primary = identityStatus.primaryAction,
                    onPrimary = {
                        when {
                            !personalDone -> navigator.navigateTo(Screen.PersonalIdentity)
                            !vehicleDone -> navigator.navigateTo(Screen.VehicleDetailsInput)
                            else -> navigator.navigateTo(Screen.DocumentUpload)
                        }
                    },
                    onCardClick = {
                        when {
                            !personalDone -> navigator.navigateTo(Screen.PersonalIdentity)
                            !vehicleDone -> navigator.navigateTo(Screen.VehicleDetailsInput)
                            else -> navigator.navigateTo(Screen.DocumentUpload)
                        }
                    }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftBlue30)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(strings.needAssistance, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PureBlack)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            strings.supportAvailable247,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            color = PureBlack.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.verify_support_icon),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(strings.contactSupportDesk, color = VerifyBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            DriveAppBottomBar(navigator = navigator, items = rememberDriveBottomNavItems())
        }
    }
}

private data class VerificationStepUi(
    val pill: String,
    val description: String,
    val progress: Float,
    val pillIcon: DrawableResource? = null,
    val primaryAction: String? = null
)

private fun resolveStepFromDocuments(
    hasBaseInfo: Boolean,
    documents: List<Document>
): VerificationStepUi {
    if (!hasBaseInfo) {
        return VerificationStepUi(
            pill = "Not Started",
            description = "Step has not been submitted yet.",
            progress = 0f,
            primaryAction = "Start Verification"
        )
    }
    if (documents.isEmpty()) {
        return VerificationStepUi(
            pill = "Not Started",
            description = "Required document is missing. Upload to continue verification.",
            progress = 0.2f,
            primaryAction = "Upload Document"
        )
    }

    val hasRejected = documents.any { it.status == DocumentStatus.REJECTED }
    val hasPending = documents.any { it.status == DocumentStatus.PENDING }
    val allApproved = documents.all { it.status == DocumentStatus.APPROVED }
    val rejectionReason = documents.firstOrNull { it.status == DocumentStatus.REJECTED }?.rejectionReason

    return when {
        hasRejected -> VerificationStepUi(
            pill = "Rejected",
            description = rejectionReason?.let { "Document rejected: $it" }
                ?: "Document was rejected. Upload a clearer copy to continue.",
            progress = 0.33f,
            primaryAction = "Re-upload"
        )
        hasPending -> VerificationStepUi(
            pill = "Under Review",
            description = "Submitted and waiting for admin approval.",
            progress = 0.66f,
            pillIcon = Res.drawable.verify_badge_under_review
        )
        allApproved -> VerificationStepUi(
            pill = "Verified",
            description = "Approved by admin.",
            progress = 1f,
            pillIcon = Res.drawable.verify_badge_verified
        )
        else -> VerificationStepUi(
            pill = "Submitted",
            description = "Submitted and waiting for admin review.",
            progress = 0.66f,
            pillIcon = Res.drawable.verify_badge_under_review
        )
    }
}

private fun resolvePersonalIdentityStep(driver: Driver): VerificationStepUi {
    val hasCoreProfile = driver.name.isNotBlank() &&
        driver.email.isNotBlank() &&
        driver.phone.isNotBlank() &&
        driver.driversLicenseNumber.isNotBlank()
    val profilePhoto = driver.documents.firstOrNull { it.type == DocumentType.PROFILE_PHOTO }

    if (!hasCoreProfile) {
        return VerificationStepUi(
            pill = "Not Started",
            description = "Complete your name, email, phone, and driver's license details.",
            progress = 0f,
            primaryAction = "Complete Profile"
        )
    }
    if (profilePhoto == null) {
        return VerificationStepUi(
            pill = "Submitted",
            description = "Profile data saved. Profile photo is optional and can be added later.",
            progress = 1f,
            pillIcon = Res.drawable.verify_badge_verified
        )
    }
    return when (profilePhoto.status) {
        DocumentStatus.APPROVED -> VerificationStepUi(
            pill = "Verified",
            description = "Profile and photo approved by admin.",
            progress = 1f,
            pillIcon = Res.drawable.verify_badge_verified
        )
        DocumentStatus.PENDING -> VerificationStepUi(
            pill = "Under Review",
            description = "Profile photo submitted and waiting for admin approval.",
            progress = 0.75f,
            pillIcon = Res.drawable.verify_badge_under_review
        )
        DocumentStatus.REJECTED -> VerificationStepUi(
            pill = "Submitted",
            description = "Profile data is complete. Profile photo is optional and can be re-uploaded anytime.",
            progress = 1f,
            pillIcon = Res.drawable.verify_badge_verified
        )
    }
}

private fun isPersonalIdentityComplete(driver: Driver): Boolean {
    val hasCoreProfile = driver.name.isNotBlank() &&
        driver.email.isNotBlank() &&
        driver.phone.isNotBlank() &&
        driver.driversLicenseNumber.isNotBlank()
    return hasCoreProfile
}

private fun resolveVehicleDetailsStep(
    hasVehicleInfo: Boolean,
    documents: List<Document>
): VerificationStepUi {
    if (!hasVehicleInfo) {
        return VerificationStepUi(
            pill = "Not Started",
            description = "Save your vehicle details first.",
            progress = 0f,
            primaryAction = "Add Vehicle"
        )
    }

    val byType = documents.associateBy { it.type }
    val registration = byType[DocumentType.VEHICLE_REGISTRATION]
    val insurance = byType[DocumentType.INSURANCE]
    if (registration == null || insurance == null) {
        return VerificationStepUi(
            pill = "Submitted",
            description = "Vehicle details saved. Upload registration and insurance documents.",
            progress = 0.4f,
            pillIcon = Res.drawable.verify_badge_under_review
        )
    }

    val requiredDocs = listOf(registration, insurance)
    return resolveStepFromDocuments(hasBaseInfo = true, documents = requiredDocs)
}

private fun resolveIdentityVerificationStep(
    documents: List<Document>
): VerificationStepUi {
    val identityDocs = documents.filter {
        it.type == DocumentType.ID_PROOF || it.type == DocumentType.DRIVERS_LICENSE
    }
    if (identityDocs.isEmpty()) {
        return VerificationStepUi(
            pill = "Not Started",
            description = "Upload a government ID document to begin identity verification.",
            progress = 0f,
            primaryAction = "Start Verification"
        )
    }

    if (identityDocs.any { it.status == DocumentStatus.APPROVED }) {
        return VerificationStepUi(
            pill = "Verified",
            description = "Identity document approved by admin.",
            progress = 1f,
            pillIcon = Res.drawable.verify_badge_verified
        )
    }
    if (identityDocs.any { it.status == DocumentStatus.PENDING }) {
        return VerificationStepUi(
            pill = "Under Review",
            description = "Identity document submitted and waiting for admin approval.",
            progress = 0.66f,
            pillIcon = Res.drawable.verify_badge_under_review
        )
    }

    val rejected = identityDocs.firstOrNull { it.status == DocumentStatus.REJECTED }
    return VerificationStepUi(
        pill = "Rejected",
        description = rejected?.rejectionReason?.let { "Document rejected: $it" }
            ?: "Identity document rejected. Upload a clearer image.",
        progress = 0.33f,
        primaryAction = "Re-upload"
    )
}

@Composable
private fun StatusCard(
    iconRes: DrawableResource,
    title: String,
    desc: String,
    pill: String,
    pillIconRes: DrawableResource? = null,
    progress: Float,
    primary: String? = null,
    onPrimary: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null
) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
            .then(
                if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(SoftBlue20, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Box(modifier = Modifier.background(SoftBlue, RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pillIconRes != null) {
                            Image(
                                painter = painterResource(pillIconRes),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        } else {
                            Icon(
                                Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = VerifyBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(pill, color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PureBlack)
            Text(desc, color = PureBlack.copy(alpha = 0.65f), fontSize = 12.sp, lineHeight = 16.sp)
            if (primary != null && onPrimary != null) {
                Button(
                    onClick = onPrimary,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue)
                ) {
                    Text(primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(SoftBlue20, RoundedCornerShape(999.dp))) {
                if (progress > 0f) {
                    Box(modifier = Modifier.fillMaxWidth(progress).height(3.dp).background(VerifyBlue, RoundedCornerShape(999.dp)))
                }
            }
        }
    }
}

@Composable
private fun ApprovedFlow(navigator: AppNavigator) {
    val strings = LocalStrings.current
    var readyStep by remember { mutableStateOf(false) }

    if (!readyStep) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VerifyBg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(50.dp))
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(70.dp))
            Text(strings.verificationSuccessful, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
            Text(strings.congratulationsActive, textAlign = TextAlign.Center, color = Color(0xFF414755))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3)), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(strings.nextStep, color = VerifyBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(strings.completeFirstDelivery, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = { readyStep = true },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue),
                shape = RoundedCornerShape(12.dp)
            ) { Text(strings.goToDashboard) }
        }
    } else {
        var accepted by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VerifyBg)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                repeat(3) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerifyBlue)
                    if (it < 2) Box(modifier = Modifier.width(56.dp).height(2.dp).background(VerifyBlue).align(Alignment.CenterVertically))
                }
            }
            Text(strings.step3Of3, color = Color(0x99000000), fontSize = 12.sp)
            Text(strings.youAreReadyToDrive, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp)

            Box(modifier = Modifier.fillMaxWidth().height(240.dp).background(Color(0xFFD4D9E6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(72.dp))
            }

            Text(strings.accountNowActivated, fontSize = 17.sp)
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                Text(strings.iAcceptThe)
                Text(strings.termsAndConditions, color = VerifyBlue, modifier = Modifier.clickable { accepted = true })
            }

            Button(
                onClick = { navigator.navigateAndClearStack(Screen.Home) },
                enabled = accepted,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue),
                shape = RoundedCornerShape(12.dp)
            ) { Text(strings.continueText) }
        }
    }
}
