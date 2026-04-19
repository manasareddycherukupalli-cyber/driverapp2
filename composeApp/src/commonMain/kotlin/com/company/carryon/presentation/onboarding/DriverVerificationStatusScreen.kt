package com.company.carryon.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.carryon.data.model.DocumentStatus
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.VerificationStatus
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val VerifyBlue = Color(0xFF2F80ED)

@Composable
fun DriverVerificationStatusScreen(
    navigator: AppNavigator,
    viewModel: DriverOnboardingViewModel
) {
    val verificationState by viewModel.verificationState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
        viewModel.refreshVerificationStatus()
    }

    when (val state = verificationState) {
        UiState.Idle, UiState.Loading -> Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        is UiState.Error -> Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(state.message, color = VerifyBlue)
        }

        is UiState.Success -> {
            val payload = state.data
            val title = when (payload.verificationStatus) {
                VerificationStatus.APPROVED -> "Verification approved"
                VerificationStatus.REJECTED -> "Verification rejected"
                VerificationStatus.IN_REVIEW -> "Under review"
                VerificationStatus.PENDING -> "Submitted"
            }
            val description = when (payload.verificationStatus) {
                VerificationStatus.APPROVED -> "Your account is verified and ready for dispatch."
                VerificationStatus.REJECTED -> "One or more documents need attention. Fix the rejected items and resubmit."
                VerificationStatus.IN_REVIEW -> "CarryOn is reviewing your onboarding package."
                VerificationStatus.PENDING -> "Your onboarding package has been submitted and is pending review."
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Status: ${payload.verificationStatus.name}", fontWeight = FontWeight.SemiBold)
                        Text("Verified: ${if (payload.isVerified) "Yes" else "No"}")
                    }
                }

                payload.documents.forEach { document ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(document.type.name, fontWeight = FontWeight.SemiBold)
                            Text("Status: ${document.status.name}", color = when (document.status) {
                                DocumentStatus.APPROVED -> MaterialTheme.colorScheme.tertiary
                                DocumentStatus.REJECTED -> VerifyBlue
                                DocumentStatus.PENDING -> MaterialTheme.colorScheme.primary
                            })
                            document.expiryDate?.let { Text("Expiry: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            if (!document.rejectionReason.isNullOrBlank()) {
                                Text("Reason: ${document.rejectionReason}", color = VerifyBlue)
                            }
                        }
                    }
                }

                Button(
                    onClick = { viewModel.refreshVerificationStatus() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Refresh status")
                }

                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        navigator.navigateAndClearStack(Screen.Onboarding)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log Out")
                }

                if (payload.verificationStatus == VerificationStatus.REJECTED) {
                    OutlinedButton(
                        onClick = { navigator.navigateAndClearStack(Screen.DriverOnboarding) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Review and resubmit")
                    }
                }

                if (payload.verificationStatus == VerificationStatus.APPROVED) {
                    Button(
                        onClick = { navigator.navigateAndClearStack(Screen.Home) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Go to home")
                    }
                }
            }
        }
    }
}
