package com.company.carryon.presentation.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*

/**
 * ProofOfDeliveryScreen — Upload delivery proof: photo, OTP, signature.
 * Required for completing a delivery.
 */
@Composable
fun ProofOfDeliveryScreen(navigator: AppNavigator) {
    val viewModel = remember { DeliveryViewModel() }
    val proofState by viewModel.proofState.collectAsState()
    val jobId = navigator.selectedJobId ?: ""

    var photoTaken by remember { mutableStateOf(false) }
    var deliveryOtp by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var selectedProofMethod by remember { mutableIntStateOf(0) }

    // Navigate on success
    LaunchedEffect(proofState) {
        if (proofState is UiState.Success) {
            navigator.navigateAndClearStack(Screen.Home)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Proof of Delivery",
            onBackClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Complete Delivery",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Provide proof of delivery to complete this job",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Proof method tabs
            val proofMethods = listOf("📸 Photo", "🔢 OTP", "✍️ Signature")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                proofMethods.forEachIndexed { index, method ->
                    FilterChip(
                        selected = selectedProofMethod == index,
                        onClick = { selectedProofMethod = index },
                        label = { Text(method, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange500,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Proof content based on selected method
            when (selectedProofMethod) {
                0 -> PhotoProofSection(
                    photoTaken = photoTaken,
                    onTakePhoto = { photoTaken = true }
                )
                1 -> OtpProofSection(
                    otp = deliveryOtp,
                    onOtpChange = { deliveryOtp = it }
                )
                2 -> SignatureProofSection()
            }

            // Recipient name
            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                label = { Text("Recipient Name (optional)") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Orange500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Submit button
            PrimaryButton(
                text = "Confirm Delivery ✓",
                onClick = {
                    viewModel.submitProof(
                        jobId = jobId,
                        photoTaken = photoTaken,
                        otpCode = deliveryOtp,
                        recipientName = recipientName
                    )
                },
                enabled = photoTaken || deliveryOtp.length >= 4,
                isLoading = proofState is UiState.Loading
            )

            // Success message
            if (proofState is UiState.Success) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Green100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "✅ Delivery completed successfully!",
                        modifier = Modifier.padding(16.dp),
                        color = Green500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Error
            if (proofState is UiState.Error) {
                Text(
                    text = (proofState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PhotoProofSection(photoTaken: Boolean, onTakePhoto: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (photoTaken) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 40.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Photo Captured", fontSize = 12.sp, color = Green500, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onTakePhoto) {
                    Text("Retake Photo", color = Orange500)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gray200)
                        .clickable { onTakePhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Gray500, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Tap to capture", fontSize = 12.sp, color = Gray500)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Take a photo of the delivered package at the doorstep",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OtpProofSection(otp: String, onOtpChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔢", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Enter delivery OTP",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = "Ask the customer for the 4-digit delivery code",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 4) onOtpChange(it) },
                modifier = Modifier.width(200.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 12.sp
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun SignatureProofSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Signature pad placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color.White)
                    .border(
                        width = 1.dp,
                        color = Gray300,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✍️", fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Signature Pad",
                        fontSize = 14.sp,
                        color = Gray500
                    )
                    Text(
                        text = "(Draw signature here)",
                        fontSize = 12.sp,
                        color = Gray400
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ask the customer to sign above",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
