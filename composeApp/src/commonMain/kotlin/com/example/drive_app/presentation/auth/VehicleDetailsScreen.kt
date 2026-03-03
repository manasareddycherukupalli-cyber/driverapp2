package com.example.drive_app.presentation.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.data.model.*
import com.example.drive_app.presentation.components.*
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*

/**
 * VehicleDetailsScreen — Input vehicle information.
 * Vehicle type selection chips, make, model, license plate, etc.
 */
@Composable
fun VehicleDetailsScreen(navigator: AppNavigator) {
    val viewModel = remember { AuthViewModel() }
    val vehicleState by viewModel.vehicleState.collectAsState()

    var selectedType by remember { mutableStateOf(VehicleType.BIKE) }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    // Navigate on success
    LaunchedEffect(vehicleState) {
        if (vehicleState is UiState.Success) {
            navigator.navigateTo(Screen.VerificationStatus)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Vehicle Details",
            onBackClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your Vehicle",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Enter your vehicle information",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            // Vehicle Type Selection
            Text(
                text = "Vehicle Type",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehicleType.entries.forEach { type ->
                    VehicleTypeChip(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = { selectedType = type },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Vehicle Make
            OutlinedTextField(
                value = make,
                onValueChange = { make = it },
                label = { Text("Make (e.g., Honda)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Vehicle Model
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model (e.g., CB Shine)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Year and Color row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { if (it.length <= 4) year = it },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // License Plate
            OutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it.uppercase() },
                label = { Text("License Plate Number") },
                leadingIcon = { Icon(Icons.Filled.DirectionsCar, contentDescription = null, tint = Orange500) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Save Button
            PrimaryButton(
                text = "Save & Continue",
                onClick = {
                    viewModel.saveVehicleDetails(
                        type = selectedType,
                        make = make,
                        model = model,
                        year = year.toIntOrNull() ?: 2024,
                        licensePlate = licensePlate,
                        color = color
                    )
                },
                enabled = make.isNotBlank() && model.isNotBlank() && licensePlate.isNotBlank(),
                isLoading = vehicleState is UiState.Loading
            )

            // Error
            if (vehicleState is UiState.Error) {
                Text(
                    text = (vehicleState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VehicleTypeChip(
    type: VehicleType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emoji = when (type) {
        VehicleType.BIKE -> "🏍️"
        VehicleType.CAR -> "🚗"
        VehicleType.VAN -> "🚐"
        VehicleType.TRUCK -> "🚛"
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Orange500 else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = type.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
