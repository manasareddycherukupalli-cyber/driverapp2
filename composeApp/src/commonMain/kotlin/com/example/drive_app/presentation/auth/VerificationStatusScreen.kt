package com.example.drive_app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.data.model.*
import com.example.drive_app.presentation.components.*
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*

/**
 * VerificationStatusScreen — Shows driver verification progress.
 * Displays document review status, vehicle verification, and overall status.
 */
@Composable
fun VerificationStatusScreen(navigator: AppNavigator, viewModel: AuthViewModel) {
    val verificationState by viewModel.verificationState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkVerificationStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Verification Status",
            onBackClick = { navigator.goBack() }
        )

        when (val state = verificationState) {
            is UiState.Loading -> LoadingScreen("Checking verification status...")
            is UiState.Error -> ErrorState(state.message) { viewModel.checkVerificationStatus() }
            is UiState.Success -> VerificationContent(state.data, navigator)
            is UiState.Idle -> LoadingScreen()
        }
    }
}

@Composable
private fun VerificationContent(driver: Driver, navigator: AppNavigator) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Status icon
        val (emoji, statusTitle, statusDescription, statusColor) = when (driver.verificationStatus) {
            VerificationStatus.APPROVED -> listOf(
                "✅", "Verified!",
                "Your account has been verified. You can start accepting deliveries.",
                Green500
            )
            VerificationStatus.IN_REVIEW -> listOf(
                "⏳", "Under Review",
                "Your documents are being reviewed. This usually takes 24-48 hours.",
                Yellow500
            )
            VerificationStatus.PENDING -> listOf(
                "📋", "Pending Submission",
                "Please upload all required documents to begin verification.",
                Orange500
            )
            VerificationStatus.REJECTED -> listOf(
                "❌", "Verification Failed",
                "Some documents were rejected. Please re-upload them.",
                Red500
            )
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background((statusColor as Color).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji as String, fontSize = 48.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = statusTitle as String,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = statusDescription as String,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(32.dp))

        // Document status list
        Text(
            text = "Document Status",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        driver.documents.forEach { doc ->
            DocumentStatusRow(doc)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.weight(1f))

        // Action button
        if (driver.verificationStatus == VerificationStatus.APPROVED) {
            PrimaryButton(
                text = "Start Driving 🚀",
                onClick = { navigator.navigateAndClearStack(Screen.Home) }
            )
        } else if (driver.verificationStatus == VerificationStatus.REJECTED) {
            PrimaryButton(
                text = "Re-upload Documents",
                onClick = { navigator.navigateTo(Screen.DocumentUpload) }
            )
        } else {
            SecondaryButton(
                text = "Refresh Status",
                onClick = { /* viewModel.checkVerificationStatus() */ }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DocumentStatusRow(document: Document) {
    val statusIcon = when (document.status) {
        DocumentStatus.APPROVED -> Icons.Filled.CheckCircle
        DocumentStatus.REJECTED -> Icons.Filled.Cancel
        DocumentStatus.PENDING -> Icons.Filled.Schedule
    }
    val statusColor = when (document.status) {
        DocumentStatus.APPROVED -> Green500
        DocumentStatus.REJECTED -> Red500
        DocumentStatus.PENDING -> Yellow500
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = document.status.name,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = document.type.displayName,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = document.status.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
    }
}
