package com.company.carryon.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalShipping
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.vehicle_front_view
import drive_app.composeapp.generated.resources.vehicle_side_view
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.VehicleType
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.Gray50
import com.company.carryon.presentation.theme.Orange100
import com.company.carryon.presentation.theme.Orange500
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val VerifyBlue = Orange500
private val VerifyBg = Gray50
private val MutedBlue = Orange100

@Composable
fun VehicleDetailsScreen(navigator: AppNavigator, viewModel: AuthViewModel) {
    val strings = LocalStrings.current
    val vehicleState by viewModel.vehicleState.collectAsState()
    val authResponse by viewModel.latestAuthResponse.collectAsState()
    val existingVehicle = authResponse?.driver?.vehicleDetails

    var selectedType by remember { mutableStateOf(existingVehicle?.type ?: VehicleType.VAN_7FT) }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2024") }
    var licensePlate by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var selectedCapacity by remember { mutableStateOf("MEDIUM") }

    LaunchedEffect(vehicleState) {
        if (vehicleState is UiState.Success) {
            val next = viewModel.determinePostLocationScreen()
            navigator.navigateAndClearStack(next)
        }
    }

    LaunchedEffect(existingVehicle?.id) {
        if (existingVehicle != null) {
            selectedType = existingVehicle.type
            make = existingVehicle.make
            model = existingVehicle.model
            year = existingVehicle.year.takeIf { it > 0 }?.toString() ?: year
            licensePlate = existingVehicle.licensePlate
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyBg)
    ) {
        DriveAppTopBar(
            title = strings.vehicleDetailsTitle,
            onBackClick = {
                val wentBack = navigator.goBack()
                if (!wentBack) {
                    navigator.navigateAndClearStack(Screen.VerificationStatus)
                }
            },
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
            showTitle = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(2.dp))
            Text(strings.vehicleDetailsTitle, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF242833))
            Text(strings.vehicleDetailsDesc, color = Color(0xFF5E6574), fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(2.dp))

            Text(strings.step2Of3, color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(999.dp)).background(MutedBlue)) {
                Box(modifier = Modifier.fillMaxWidth(0.66f).height(4.dp).clip(RoundedCornerShape(999.dp)).background(VerifyBlue))
            }

            Text("Vehicle type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414755))
            VehicleType.entries.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { type ->
                        VehicleTypeCard(
                            type = type,
                            selected = selectedType == type,
                            modifier = Modifier.weight(1f)
                        ) { selectedType = type }
                    }
                    if (row.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            VerifyInput(strings.vehicleModel, model, { model = it }, "e.g. Ford Transit")
            VerifyInput(strings.vehicleYear, year, { if (it.length <= 4) year = it }, "2024", KeyboardType.Number)
            VerifyInput(strings.licensePlate, licensePlate, { licensePlate = it.uppercase() }, "ABC-1234")
            VerifyInput(strings.vehicleVin, vin, { vin = it }, "17-digit code")

            Text(strings.vehicleCapacity, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414755))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CapacityCard("LIGHT", "Up to 1.5t", selectedCapacity == "LIGHT", Modifier.weight(1f)) { selectedCapacity = "LIGHT" }
                CapacityCard("MEDIUM", "1.5t - 3.5t", selectedCapacity == "MEDIUM", Modifier.weight(1f)) { selectedCapacity = "MEDIUM" }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CapacityCard("HEAVY", "3.5t - 7.5t", selectedCapacity == "HEAVY", Modifier.weight(1f)) { selectedCapacity = "HEAVY" }
                CapacityCard("CUSTOM", "High Spec", selectedCapacity == "CUSTOM", Modifier.weight(1f)) { selectedCapacity = "CUSTOM" }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.visualDocumentation, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414755))
                Text(strings.maxImageSize, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = VerifyBlue)
            }
            DocUploadCard(strings.frontView, Res.drawable.vehicle_front_view)
            DocUploadCard(strings.sideProfile, Res.drawable.vehicle_side_view)

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
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = licensePlate.isNotBlank() && model.isNotBlank()
            ) {
                Text(strings.saveAndContinue, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                strings.vehicleSaveConsent,
                color = Color(0xFF9CA3AF),
                fontSize = 9.sp,
                lineHeight = 12.sp
            )

            if (vehicleState is UiState.Error) {
                Text((vehicleState as UiState.Error).message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VehicleTypeCard(
    type: VehicleType,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(78.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) VerifyBlue else Color.White),
        border = BorderStroke(1.dp, if (selected) VerifyBlue else Color(0x33C1C6D7))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                type.displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (selected) Color.White else Color(0xFF242833)
            )
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
            shape = RoundedCornerShape(10.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF6F7FB),
                unfocusedContainerColor = Color(0xFFF6F7FB),
                focusedBorderColor = Color(0x1A5E6574),
                unfocusedBorderColor = Color(0x1A5E6574)
            )
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
private fun DocUploadCard(label: String, backgroundRes: DrawableResource) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth().height(134.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8F3)),
        border = BorderStroke(1.dp, Color(0x26C1C6D7))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.54f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = VerifyBlue)
                }
                Spacer(Modifier.height(8.dp))
                Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF111827))
                Text(strings.tapToUpload, fontSize = 10.sp, color = Color(0xFF64748B))
            }
        }
    }
}
