package com.example.drive_app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.data.model.*
import com.example.drive_app.presentation.components.*
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*

/**
 * DocumentUploadScreen — Upload required documents for verification.
 * Shows list of documents with status indicators and upload buttons.
 */
@Composable
fun DocumentUploadScreen(navigator: AppNavigator) {
    val viewModel = remember { AuthViewModel() }
    val uploadState by viewModel.documentUploadState.collectAsState()
    val uploadedDocs by viewModel.uploadedDocuments.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Upload Documents",
            onBackClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Required Documents",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Upload clear photos of the following documents",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Document items
            DocumentType.entries.forEach { docType ->
                val uploadedDoc = uploadedDocs.find { it.type == docType }
                DocumentUploadItem(
                    documentType = docType,
                    status = uploadedDoc?.status,
                    isUploading = uploadState is UiState.Loading,
                    onUpload = { viewModel.uploadDocument(docType) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Continue button
            PrimaryButton(
                text = "Continue to Vehicle Details",
                onClick = { navigator.navigateTo(Screen.VehicleDetailsInput) },
                enabled = uploadedDocs.size >= 2 // Require at least 2 docs
            )

            // Skip for now
            TextButton(
                onClick = { navigator.navigateTo(Screen.VehicleDetailsInput) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DocumentUploadItem(
    documentType: DocumentType,
    status: DocumentStatus?,
    isUploading: Boolean,
    onUpload: () -> Unit
) {
    val icon = when (documentType) {
        DocumentType.DRIVERS_LICENSE -> Icons.Filled.CreditCard
        DocumentType.VEHICLE_REGISTRATION -> Icons.Filled.DirectionsCar
        DocumentType.INSURANCE -> Icons.Filled.Security
        DocumentType.PROFILE_PHOTO -> Icons.Filled.Person
        DocumentType.ID_PROOF -> Icons.Filled.Badge
    }

    val statusColor = when (status) {
        DocumentStatus.APPROVED -> Green500
        DocumentStatus.REJECTED -> Red500
        DocumentStatus.PENDING -> Yellow500
        null -> Gray400
    }

    val statusText = when (status) {
        DocumentStatus.APPROVED -> "Approved ✓"
        DocumentStatus.REJECTED -> "Rejected ✗"
        DocumentStatus.PENDING -> "Under Review"
        null -> "Not Uploaded"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = status == null || status == DocumentStatus.REJECTED) { onUpload() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Orange500.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = documentType.displayName,
                    tint = Orange500,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Document info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = documentType.displayName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }

            // Upload button or status icon
            if (status == null || status == DocumentStatus.REJECTED) {
                FilledTonalButton(
                    onClick = onUpload,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Orange100,
                        contentColor = Orange500
                    )
                ) {
                    Text("Upload", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            } else {
                Icon(
                    imageVector = if (status == DocumentStatus.APPROVED) Icons.Filled.CheckCircle else Icons.Filled.Schedule,
                    contentDescription = statusText,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
