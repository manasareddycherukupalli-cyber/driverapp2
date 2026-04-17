package com.company.carryon.presentation.support

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
import com.company.carryon.data.model.UiState
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.theme.*

/**
 * SosScreen — Emergency SOS button and quick emergency contacts.
 * Triggers emergency alert with location sharing.
 */
@Composable
fun SosScreen(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val viewModel = remember { SupportViewModel() }
    val sosState by viewModel.sosState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = strings.emergencySos,
            onBackClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // SOS Button
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.size(180.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red500,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🆘", fontSize = 48.sp)
                    Text(
                        text = "SOS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = strings.tapInEmergency,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = strings.locationSharedWithEmergency,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            // Status
            if (sosState is UiState.Success) {
                Spacer(Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Green100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = strings.emergencyAlertSent,
                        modifier = Modifier.padding(16.dp),
                        color = Green500,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (sosState is UiState.Loading) {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(color = Red500)
                Spacer(Modifier.height(8.dp))
                Text(strings.sendingEmergencyAlert, color = Red500)
            }

            Spacer(Modifier.height(32.dp))

            // Emergency contacts
            Text(
                text = strings.quickContacts,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            EmergencyContactCard("🚔", "Police", "999")
            Spacer(Modifier.height(8.dp))
            EmergencyContactCard("🚑", "Ambulance", "999")
            Spacer(Modifier.height(8.dp))
            EmergencyContactCard("👤", "Emergency Contact", "+60 12-345 6789")
        }
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Text("🚨", fontSize = 32.sp) },
            title = { Text(strings.triggerEmergencySos, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    strings.sosAlertWarning,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.triggerSos()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500)
                ) {
                    Text(strings.sendSos, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun EmergencyContactCard(emoji: String, name: String, number: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(number, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { /* Open dialer */ }) {
                Icon(Icons.Filled.Phone, contentDescription = "Call", tint = Green500)
            }
        }
    }
}
