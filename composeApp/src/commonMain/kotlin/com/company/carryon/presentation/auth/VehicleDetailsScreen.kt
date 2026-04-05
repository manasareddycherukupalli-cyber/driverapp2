package com.company.carryon.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.VehicleType
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val VerifyBlue = Color(0xFF2F80ED)
private val VerifyBg = Color(0xFFF9F9FF)
private val MutedBlue = Color(0xFFA6D2F3)

@Composable
fun VehicleDetailsScreen(navigator: AppNavigator, viewModel: AuthViewModel) {
    val vehicleState by viewModel.vehicleState.collectAsState()

    var selectedType by remember { mutableStateOf(VehicleType.VAN) }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2024") }
    var licensePlate by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var selectedCapacity by remember { mutableStateOf("MEDIUM") }

    LaunchedEffect(vehicleState) {
        if (vehicleState is UiState.Success) navigator.navigateTo(Screen.VerificationStatus)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable { navigator.goBack() })
            Text("Carry On", color = VerifyBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.width(18.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("STEP 2 OF 3: VEHICLE DETAILS", color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(999.dp)).background(MutedBlue)) {
                Box(modifier = Modifier.fillMaxWidth(0.66f).height(4.dp).clip(RoundedCornerShape(999.dp)).background(VerifyBlue))
            }

            Text("Vehicle Assets", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text("Provide accurate vehicle identification details for insurance and manifest verification.", color = Color(0xFF414755), fontSize = 14.sp)

            VerifyInput("Vehicle Model", model, { model = it }, "e.g. Ford Transit")
            VerifyInput("Year", year, { if (it.length <= 4) year = it }, "2024", KeyboardType.Number)
            VerifyInput("License Plate Number", licensePlate, { licensePlate = it.uppercase() }, "ABC-1234")
            VerifyInput("VIN (Vehicle Identification Number)", vin, { vin = it }, "17-digit code")

            Text("Vehicle Capacity Class", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414755))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CapacityCard("LIGHT", "Up to 1.5t", selectedCapacity == "LIGHT", Modifier.weight(1f)) { selectedCapacity = "LIGHT" }
                CapacityCard("MEDIUM", "1.5t - 3.5t", selectedCapacity == "MEDIUM", Modifier.weight(1f)) { selectedCapacity = "MEDIUM" }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CapacityCard("HEAVY", "3.5t - 7.5t", selectedCapacity == "HEAVY", Modifier.weight(1f)) { selectedCapacity = "HEAVY" }
                CapacityCard("CUSTOM", "High Spec", selectedCapacity == "CUSTOM", Modifier.weight(1f)) { selectedCapacity = "CUSTOM" }
            }

            Text("Visual Documentation", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414755))
            DocUploadCard("FRONT VIEW")
            DocUploadCard("SIDE PROFILE")

            Button(
                onClick = {
                    viewModel.saveVehicleDetails(
                        type = selectedType,
                        make = if (make.isBlank()) "Carry On" else make,
                        model = if (model.isBlank()) "Transit" else model,
                        year = year.toIntOrNull() ?: 2024,
                        licensePlate = licensePlate,
                        color = selectedCapacity
                    )
                    navigator.navigateTo(Screen.DocumentUpload)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = licensePlate.isNotBlank() && model.isNotBlank()
            ) {
                Text("Save & Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            if (vehicleState is UiState.Error) {
                Text((vehicleState as UiState.Error).message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VerifyInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414755))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(placeholder, color = Color(0x88717786)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun CapacityCard(title: String, subtitle: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(94.dp).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) VerifyBlue else Color.White),
        border = BorderStroke(1.dp, if (selected) VerifyBlue else Color(0x33C1C6D7))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = if (selected) Color.White else VerifyBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = if (selected) Color.White else Color.Black)
            Text(subtitle, fontSize = 9.sp, color = if (selected) Color.White.copy(alpha = 0.85f) else Color(0xFF777777))
        }
    }
}

@Composable
private fun DocUploadCard(label: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(170.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8F3)),
        border = BorderStroke(2.dp, Color(0x4DC1C6D7))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(23.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = VerifyBlue)
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Tap to upload", fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}
