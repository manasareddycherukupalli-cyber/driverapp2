package com.company.carryon.presentation.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.Document
import com.company.carryon.data.model.DocumentStatus
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.VerificationStatus
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val VerifyBlue = Color(0xFF2F80ED)
private val VerifyBg = Color(0xFFF7F9FF)
private val VerifyText = Color(0xFF232323)
private val VerifyMuted = Color(0xFF676A74)
private val SoftBlue = Color(0x33A6D2F3)
private val SoftBlueStrong = Color(0xFFBFD7FF)

@Composable
fun DriverVerificationStatusScreen(
    navigator: AppNavigator,
    viewModel: DriverOnboardingViewModel
) {
    val verificationState by viewModel.verificationState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
        viewModel.startVerificationMonitor()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopVerificationMonitor()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.verificationNavigationEvents.collect { event ->
            when (event) {
                DriverOnboardingViewModel.VerificationNavigationEvent.NavigateToDashboard -> {
                    navigator.navigateAndClearStack(Screen.Home)
                }
            }
        }
    }

    when (val state = verificationState) {
        UiState.Idle, UiState.Loading -> Box(
            modifier = Modifier.fillMaxSize().background(VerifyBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = VerifyBlue)
        }

        is UiState.Error -> Box(
            modifier = Modifier.fillMaxSize().background(VerifyBg).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.message, color = VerifyBlue, textAlign = TextAlign.Center)
                OutlinedButton(onClick = { viewModel.refreshVerificationStatus() }) {
                    Text("Refresh")
                }
            }
        }

        is UiState.Success -> {
            val payload = state.data
            when (payload.verificationStatus) {
                VerificationStatus.APPROVED -> Box(
                    modifier = Modifier.fillMaxSize().background(VerifyBg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VerifyBlue)
                }
                VerificationStatus.REJECTED -> FailedVerificationScreen(
                    navigator = navigator,
                    documents = payload.documents
                )
                VerificationStatus.PENDING,
                VerificationStatus.IN_REVIEW -> PendingVerificationScreen(
                    onRefresh = { viewModel.refreshVerificationStatus() }
                )
            }
        }
    }
}

@Composable
private fun FailedVerificationScreen(
    navigator: AppNavigator,
    documents: List<Document>
) {
    val problemDocs = documents
        .filter { it.status == DocumentStatus.REJECTED || isExpired(it.expiryDate) }
        .ifEmpty { documents.filter { it.status != DocumentStatus.APPROVED } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        VerificationHeader()

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color.White, CircleShape)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(34.dp))
        }

        Text(
            "Verification Failed",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = VerifyText,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp / 2
        )
        Text(
            "We encountered some issues while reviewing your application. Don't worry, you can update your documents to continue the onboarding process.",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = VerifyMuted,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp / 1.3f
        )

        problemDocs.take(2).forEach { doc ->
            val expired = isExpired(doc.expiryDate)
            val chip = if (expired) "EXPIRED" else "ACTION REQUIRED"
            val action = if (expired) "Update File" else "Re-upload"
            val reason = when {
                expired -> "The registration document provided expired on ${doc.expiryDate}."
                !doc.rejectionReason.isNullOrBlank() -> doc.rejectionReason
                else -> "The uploaded image was unclear and needs a better copy."
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Assignment, contentDescription = null, tint = VerifyBlue)
                        Box(
                            modifier = Modifier.background(SoftBlueStrong, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(chip, color = VerifyBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(doc.type.displayName, fontWeight = FontWeight.Bold, color = VerifyText, fontSize = 16.sp)
                    Text(reason ?: "Document needs an updated upload.", color = VerifyMuted, lineHeight = 20.sp / 1.3f)
                    Text("$action ->", color = VerifyBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Need help with documents?", color = VerifyText, fontWeight = FontWeight.Bold)
                Text("Our support team is available 24/7 to guide you through the verification process.", color = VerifyMuted)
                Text("Chat with Support ->", color = VerifyBlue, fontWeight = FontWeight.SemiBold)
            }
        }

        Button(
            onClick = { navigator.navigateAndClearStack(Screen.DriverOnboarding) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue)
        ) {
            Text("Re-upload Documents", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = { navigator.navigateTo(Screen.HelpContactSupport) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = VerifyText)
        ) {
            Text("Contact Support", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PendingVerificationScreen(
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyBg)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
    ) {
        Icon(Icons.Filled.Description, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(48.dp))
        Text("Verification In Progress", color = VerifyText, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(
            "Your documents are submitted and currently under review.",
            color = VerifyMuted,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue)) {
            Text("Refresh status")
        }
    }
}

@Composable
private fun VerificationHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Carry On", color = VerifyBlue, fontWeight = FontWeight.Bold, fontSize = 36.sp / 2)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.NotificationsNone, contentDescription = "Notifications", tint = VerifyMuted)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = VerifyMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun isExpired(date: String?): Boolean {
    if (date.isNullOrBlank()) return false
    val parsed = runCatching { LocalDate.parse(date) }.getOrNull() ?: return false
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return parsed < today
}
