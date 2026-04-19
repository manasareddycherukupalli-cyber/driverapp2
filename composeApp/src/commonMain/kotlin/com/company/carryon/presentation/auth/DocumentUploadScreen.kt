package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.Document
import com.company.carryon.data.model.DocumentType
import com.company.carryon.data.model.UiState
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.Gray50
import com.company.carryon.presentation.theme.Gray700
import com.company.carryon.presentation.theme.Gray900
import com.company.carryon.presentation.theme.Orange500
import com.company.carryon.presentation.util.rememberImagePickerLauncher
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.checklist_full_corners
import drive_app.composeapp.generated.resources.checklist_no_flash_glare
import org.jetbrains.compose.resources.painterResource

private val VerifyBlue = Orange500
private val VerifyBg = Gray50
private val TextPrimary = Gray900
private val TextMuted = Gray700

@Composable
fun DocumentUploadScreen(navigator: AppNavigator, viewModel: AuthViewModel) {
    val strings = LocalStrings.current
    val latestAuthResponse by viewModel.latestAuthResponse.collectAsState()
    val documentUploadState by viewModel.documentUploadState.collectAsState()
    val uploadedDocs by viewModel.uploadedDocuments.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingType by remember { mutableStateOf<DocumentType?>(null) }
    var awaitingUploadResult by remember { mutableStateOf(false) }

    val picker = rememberImagePickerLauncher { imageBytes ->
        val type = pendingType ?: DocumentType.ID_PROOF
        awaitingUploadResult = true
        viewModel.uploadDocument(type, imageBytes)
    }

    val existingDocs = remember(latestAuthResponse, uploadedDocs) {
        val byType = linkedMapOf<DocumentType, Document>()
        latestAuthResponse?.driver?.documents.orEmpty().forEach { byType[it.type] = it }
        uploadedDocs.forEach { byType[it.type] = it }
        byType.values.toList()
    }

    val idProofDoc = existingDocs.firstOrNull { it.type == DocumentType.ID_PROOF }
    val idProofUploaded = idProofDoc != null

    LaunchedEffect(documentUploadState) {
        when (val state = documentUploadState) {
            is UiState.Success -> {
                errorMessage = null
                if (awaitingUploadResult) {
                    awaitingUploadResult = false
                    val next = viewModel.determinePostLocationScreen()
                    navigator.navigateAndClearStack(next)
                }
            }
            is UiState.Error -> {
                errorMessage = state.message
                awaitingUploadResult = false
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        DriveAppTopBar(
            title = strings.identityVerification,
            onBackClick = {
                val wentBack = navigator.goBack()
                if (!wentBack) {
                    navigator.navigateAndClearStack(Screen.VerificationStatus)
                }
            },
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
            showTitle = false
        )

        Spacer(Modifier.height(26.dp))
        Text(strings.identityVerification, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(strings.step3Of3, color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFFBFD5F6))) {
            Box(modifier = Modifier.fillMaxWidth(1.0f).height(4.dp).clip(RoundedCornerShape(999.dp)).background(VerifyBlue))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            strings.uploadGovernmentId,
            color = TextMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(strings.idProof, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            if (idProofUploaded) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(12.dp))
                    Text(strings.uploadDone, color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text(strings.uploadRequired, fontSize = 11.sp, color = Color(0xFF7D8597), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x26A6D2F3))
                        .border(1.dp, Color(0xFF2F80ED), RoundedCornerShape(10.dp))
                        .clickable {
                            pendingType = DocumentType.ID_PROOF
                            picker.launch()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (idProofUploaded) strings.replaceImage
                            else strings.chooseImage,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(strings.supportsImageFormats, color = TextMuted, fontSize = 10.sp)
                    }
                }

                if (idProofUploaded) {
                    Row(
                        modifier = Modifier.clickable {
                            pendingType = DocumentType.ID_PROOF
                            picker.launch()
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(12.dp))
                        Text(strings.replaceDocument, color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFA6D2F3))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(12.dp))
                        Text(strings.captureInWellLit, color = Color(0xFF516079), fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        when (documentUploadState) {
            UiState.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = VerifyBlue, strokeWidth = 2.dp)
                    Text(strings.uploadingDocument, color = TextMuted, fontSize = 12.sp)
                }
            }
            is UiState.Success -> Text(strings.documentUploaded, color = VerifyBlue, fontSize = 12.sp)
            is UiState.Error -> Text(errorMessage ?: strings.uploadFailed, color = Color(0xFFD32F2F), fontSize = 12.sp)
            else -> Unit
        }

        Spacer(Modifier.height(18.dp))
        Text(strings.verificationChecklist, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        ChecklistRow(
            title = strings.noFlashGlare,
            desc = strings.noFlashGlareDesc,
            iconPainter = painterResource(Res.drawable.checklist_no_flash_glare)
        )
        Spacer(Modifier.height(10.dp))
        ChecklistRow(
            title = strings.fullCorners,
            desc = strings.fullCornersDesc,
            iconPainter = painterResource(Res.drawable.checklist_full_corners)
        )

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                val next = viewModel.determinePostLocationScreen()
                navigator.navigateAndClearStack(next)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue),
            enabled = idProofUploaded && documentUploadState !is UiState.Loading
        ) {
            Text(strings.continueText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            strings.uploadNote,
            color = Color(0xFF818796),
            fontSize = 10.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ChecklistRow(
    title: String,
    desc: String,
    iconPainter: androidx.compose.ui.graphics.painter.Painter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0x332F80ED)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF2A2F3A), fontSize = 13.sp)
            Text(desc, color = Color(0xFF7C8494), fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
