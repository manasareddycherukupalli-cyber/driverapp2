package com.example.drive_app.presentation.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.data.model.UiState
import com.example.drive_app.presentation.components.*
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.theme.*

/**
 * EditProfileScreen — Edit driver's personal information.
 */
@Composable
fun EditProfileScreen(navigator: AppNavigator) {
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var name by remember { mutableStateOf(driver?.name ?: "Ahmad bin Hassan") }
    var email by remember { mutableStateOf(driver?.email ?: "ahmad@example.com") }
    var emergencyContact by remember { mutableStateOf(driver?.emergencyContact ?: "") }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) navigator.goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Edit Profile",
            onBackClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AvatarCircle(
                        initials = name.take(2).uppercase(),
                        size = 80.dp
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { /* Photo picker */ }) {
                        Text("Change Photo", color = Orange500, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Orange500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Orange500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Phone (read-only)
            OutlinedTextField(
                value = driver?.phone ?: "+60 12-345 6789",
                onValueChange = {},
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = Orange500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                readOnly = true,
                enabled = false
            )

            // Emergency contact
            OutlinedTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                label = { Text("Emergency Contact") },
                leadingIcon = { Icon(Icons.Filled.ContactPhone, contentDescription = null, tint = Orange500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = "Save Changes",
                onClick = { viewModel.updateProfile(name, email, emergencyContact) },
                enabled = name.isNotBlank() && email.isNotBlank(),
                isLoading = updateState is UiState.Loading
            )

            if (updateState is UiState.Error) {
                Text(
                    text = (updateState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }
}
