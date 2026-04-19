package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DocumentStatus
import com.company.carryon.data.model.DocumentType
import com.company.carryon.data.model.UiState
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.util.rememberImagePickerLauncher
import com.company.carryon.presentation.util.toImageBitmap
import io.ktor.client.call.body
import io.ktor.client.request.get

private val Blue = Color(0xFF2F80ED)
private val Bg = Color(0xFFF7F8FC)

@Composable
fun PersonalIdentityScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    val strings = LocalStrings.current
    val latestAuthResponse by authViewModel.latestAuthResponse.collectAsState()
    val profileUpdateState by authViewModel.profileUpdateState.collectAsState()
    val documentUploadState by authViewModel.documentUploadState.collectAsState()
    val uploadedDocuments by authViewModel.uploadedDocuments.collectAsState()

    val currentDriver = latestAuthResponse?.driver
    var name by remember(currentDriver?.id, currentDriver?.name) { mutableStateOf(currentDriver?.name.orEmpty()) }
    var email by remember(currentDriver?.id, currentDriver?.email) { mutableStateOf(currentDriver?.email.orEmpty()) }
    var phone by remember(currentDriver?.id, currentDriver?.phone) { mutableStateOf(currentDriver?.phone.orEmpty()) }
    var dl by remember(currentDriver?.id, currentDriver?.driversLicenseNumber) {
        mutableStateOf(currentDriver?.driversLicenseNumber.orEmpty())
    }
    var dob by remember(currentDriver?.id, currentDriver?.dateOfBirth) {
        mutableStateOf(currentDriver?.dateOfBirth.orEmpty())
    }
    var profileError by remember { mutableStateOf<String?>(null) }
    var localProfilePhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var persistedProfilePhotoBytes by remember { mutableStateOf<ByteArray?>(null) }

    val profilePhotoDoc = remember(currentDriver, uploadedDocuments) {
        val merged = linkedMapOf<DocumentType, com.company.carryon.data.model.Document>()
        currentDriver?.documents.orEmpty().forEach { merged[it.type] = it }
        uploadedDocuments.forEach { merged[it.type] = it }
        merged[DocumentType.PROFILE_PHOTO]
    }
    val picker = rememberImagePickerLauncher { bytes ->
        localProfilePhotoBytes = bytes
        authViewModel.uploadDocument(DocumentType.PROFILE_PHOTO, bytes)
    }

    LaunchedEffect(Unit) {
        authViewModel.resetProfileUpdateState()
    }

    LaunchedEffect(profileUpdateState) {
        when (val state = profileUpdateState) {
            is UiState.Success -> {
                val next = authViewModel.determinePostLocationScreen()
                navigator.navigateAndClearStack(next)
            }
            is UiState.Error -> profileError = state.message
            else -> Unit
        }
    }

    LaunchedEffect(profilePhotoDoc?.imageUrl) {
        val imageUrl = profilePhotoDoc?.imageUrl
        if (imageUrl.isNullOrBlank()) {
            persistedProfilePhotoBytes = null
            return@LaunchedEffect
        }
        val bytes = runCatching {
            HttpClientFactory.client.get(imageUrl).body<ByteArray>()
        }.getOrNull()
        persistedProfilePhotoBytes = bytes
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        DriveAppTopBar(
            title = strings.personalIdentity,
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
            showTitle = false
        )

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(strings.personalIdentity, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF1E1F25))
            Text(
                text = "Sign out",
                color = Blue,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    val wentBack = navigator.goBack()
                    if (!wentBack) {
                        navigator.navigateAndClearStack(Screen.Onboarding)
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(strings.step1Of3, color = Blue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFFBFD5F6))) {
            Box(modifier = Modifier.fillMaxWidth(0.33f).height(4.dp).clip(RoundedCornerShape(999.dp)).background(Blue))
        }

        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(206.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF3F4FB)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(strings.profilePhoto, fontWeight = FontWeight.SemiBold, color = Color(0xFF676C78), fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD0E3F8))
                        .clickable { picker.launch() }
                        .drawBehind {
                            val strokeWidth = 3.dp.toPx()
                            drawCircle(
                                color = Blue.copy(alpha = 0.8f),
                                radius = (size.minDimension - strokeWidth) / 2f,
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val previewBytes = localProfilePhotoBytes ?: persistedProfilePhotoBytes
                    val previewBitmap = remember(previewBytes) {
                        previewBytes?.toImageBitmap()
                    }
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap,
                            contentDescription = "Profile photo preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        ProfilePhotoIcon()
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    strings.uploadPhotoInstruction,
                    fontSize = 12.sp,
                    color = Color(0xFF636A7A),
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(6.dp))
                when (profilePhotoDoc?.status) {
                    DocumentStatus.APPROVED -> Text(strings.photoVerified, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    DocumentStatus.PENDING -> Text(strings.photoUnderReview, color = Blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    DocumentStatus.REJECTED -> Text(
                        profilePhotoDoc.rejectionReason?.let { "Photo rejected: $it" } ?: strings.photoRejected,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp
                    )
                    null -> Text(strings.tapToUploadPhoto, color = Color(0xFF636A7A), fontSize = 12.sp)
                }
                if (documentUploadState is UiState.Loading) {
                    Text(strings.uploadingPhoto, color = Blue, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        IdentityField(strings.fullName, name, { name = it }, "e.g. Marcus Aurelius")
        IdentityField(strings.emailAddress, email, { email = it }, "name@carryon.com", KeyboardType.Email, readOnly = true)
        IdentityField(strings.phoneNumber, phone, { phone = it }, "+1 (555) 000-0000", KeyboardType.Phone)
        IdentityField(strings.driverLicenseNumber, dl, { dl = it }, "DL-882910")
        IdentityField(strings.dateOfBirth, dob, { dob = it }, "mm/dd/yyyy")

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEAF2FE))
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = Blue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                strings.backgroundCheckConsent,
                fontSize = 11.sp,
                color = Color(0xFF5E6574),
                lineHeight = 15.sp
            )
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                profileError = null
                if (dl.isBlank()) {
                    profileError = strings.driverLicenseRequired
                    return@Button
                }
                authViewModel.updateProfile(
                    name = name,
                    phone = phone,
                    driversLicenseNumber = dl.trim(),
                    dateOfBirth = dob.trim().takeIf { it.isNotBlank() }
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            enabled = name.isNotBlank() &&
                phone.isNotBlank() &&
                dl.isNotBlank() &&
                profileUpdateState !is UiState.Loading
        ) {
            Text(strings.saveAndContinue, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
        if (profileError != null) {
            Spacer(Modifier.height(8.dp))
            Text(profileError ?: "", color = Color(0xFFD32F2F), fontSize = 12.sp)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ProfilePhotoIcon() {
    Box(modifier = Modifier.size(74.dp), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Outlined.AddAPhoto,
            contentDescription = null,
            tint = Blue,
            modifier = Modifier.size(42.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(Blue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun IdentityField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414755))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF8A8F9B)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFADB3C0),
                unfocusedBorderColor = Color(0xFFADB3C0),
                focusedContainerColor = Color(0xFFF7F8FC),
                unfocusedContainerColor = Color(0xFFF7F8FC)
            )
        )
        Spacer(Modifier.height(3.dp))
    }
}
