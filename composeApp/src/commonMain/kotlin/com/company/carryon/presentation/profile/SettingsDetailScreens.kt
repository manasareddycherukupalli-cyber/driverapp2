package com.company.carryon.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.Document
import com.company.carryon.data.model.DocumentStatus
import com.company.carryon.data.model.DocumentType
import com.company.carryon.data.network.saveLanguage
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.i18n.getLanguageDisplayName
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.DriveAppBottomBar
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.navigation.rememberDriveBottomNavItems
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.notify_app_promotions
import drive_app.composeapp.generated.resources.notify_earnings_reports
import drive_app.composeapp.generated.resources.notify_order_updates
import drive_app.composeapp.generated.resources.notify_push_notifications
import drive_app.composeapp.generated.resources.ic_nearby
import drive_app.composeapp.generated.resources.vehicle_ford_transit_cargo_xl
import drive_app.composeapp.generated.resources.vehicle_spec_cargo_volume
import drive_app.composeapp.generated.resources.vehicle_spec_fuel_type
import drive_app.composeapp.generated.resources.vehicle_spec_max_payload
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
private fun SettingsTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF2F80ED))
        }
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF181C23))
    }
}

@Composable
fun NotificationPreferencesScreen(navigator: AppNavigator) {
    var push by remember { mutableStateOf(true) }
    var orders by remember { mutableStateOf(true) }
    var earnings by remember { mutableStateOf(false) }
    var promos by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0058BC))
            }
            Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF181C23))
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            Text("COMMUNICATION", color = Color(0xFF2F80ED), fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
            Text("Notification Preferences", fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 40.sp, color = Color(0xFF181C23))
            Text(
                "Manage how you receive updates about your\ndeliveries, earnings, and platform news.",
                color = Color(0xFF414755),
                fontSize = 16.sp,
                lineHeight = 26.sp
            )

            NotificationCard(
                "Push Notifications",
                "The master switch for\nall immediate device-\nlevel alerts and sound\nnotifications.",
                Res.drawable.notify_push_notifications,
                push
            ) { push = it }

            NotificationCard(
                "Order Updates",
                "Real-time status\nchanges, pickup\nassignments, and\ncritical delivery\ninstructions.",
                Res.drawable.notify_order_updates,
                orders
            ) { orders = it }

            NotificationCard(
                "Earnings Reports",
                "Weekly summaries,\ninstant payout\nconfirmations, and\nmonthly tax document\nalerts.",
                Res.drawable.notify_earnings_reports,
                earnings
            ) { earnings = it }

            NotificationCard(
                "App Promotions",
                "New feature\nannouncements,\nseasonal bonuses, and\nexclusive partnership\noffers.",
                Res.drawable.notify_app_promotions,
                promos
            ) { promos = it }

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F80ED))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 22.dp, vertical = 24.dp)) {
                        Text("Need a break?", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "You can temporarily silence all alerts\nusing Focus Mode during your off-hours.",
                            color = Color(0xCCFFFFFF),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF2F80ED)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Enable Focus Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 10.dp, bottom = 8.dp)
                            .size(56.dp)
                            .border(6.dp, Color(0x1FFFFFFF), RoundedCornerShape(999.dp))
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        DriveAppBottomBar(navigator = navigator, items = rememberDriveBottomNavItems())
    }
}

@Composable
private fun NotificationCard(
    title: String,
    description: String,
    iconRes: DrawableResource,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33C1C6D7))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(48.dp).background(Color(0x33A6D2F3), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF2F80ED), lineHeight = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(description, color = Color(0xFF414755), fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF2F80ED),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFA6D2F3),
                    uncheckedBorderColor = Color(0xFFA6D2F3)
                )
            )
        }
    }
}

@Composable
fun LanguageScreen(
    navigator: AppNavigator,
    currentLanguage: String = "en",
    onLanguageChanged: (String) -> Unit = {}
) {
    val strings = LocalStrings.current
    // Map of language code → display info (native name, subtitle)
    val languages = listOf(
        Triple("en",  "English",       "English (Malaysia)"),
        Triple("ms",  "Bahasa Melayu", "Malay"),
        Triple("zh",  "中文",           "Mandarin Chinese"),
        Triple("ta",  "தமிழ்",          "Tamil"),
    )
    var selected by remember { mutableStateOf(currentLanguage) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        SettingsTopBar(strings.settingsTitle) { navigator.goBack() }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp)
        ) {
            Text(strings.selectYourLanguage, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 40.sp, color = Color(0xFF181C23))
            Spacer(Modifier.height(4.dp))
            Text(
                "Select your preferred language for the\nnavigation and dashboard interface.",
                color = Color(0xFF414755),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(16.dp))
            languages.forEach { (code, nativeName, subtitle) ->
                val isSelected = selected == code
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable {
                            selected = code
                            saveLanguage(code)
                            onLanguageChanged(code)
                            navigator.goBack()
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3)),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2F80ED)) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(if (isSelected) Color(0x33A6D2F3) else Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Language, contentDescription = null, tint = Color(0xFF2F80ED), modifier = Modifier.size(17.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(nativeName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF414755))
                                Text(subtitle, color = Color(0xFF64748B), fontSize = 14.sp)
                            }
                        }
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFF2F80ED) else Color(0xFFB8C3D7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        DriveAppBottomBar(navigator = navigator, items = rememberDriveBottomNavItems())
    }
}

@Composable
fun VehicleInfoScreen(navigator: AppNavigator) {
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()
    val vehicle = driver?.vehicleDetails
    val vehicleName = remember(vehicle?.make, vehicle?.model) {
        listOfNotNull(
            vehicle?.make?.takeIf { it.isNotBlank() },
            vehicle?.model?.takeIf { it.isNotBlank() }
        ).joinToString(" - ").ifBlank { "Vehicle Not Set" }
    }
    val plate = vehicle?.licensePlate?.takeIf { it.isNotBlank() } ?: "Unavailable"
    val fuelOrColor = vehicle?.color?.takeIf { it.isNotBlank() } ?: "Not provided"
    val vehicleType = vehicle?.type?.displayName ?: "Unknown"
    val year = vehicle?.year?.takeIf { it > 0 }?.toString() ?: "Unknown"
    val docsApproved = driver?.documents.orEmpty().count { it.status == DocumentStatus.APPROVED }
    val docsPending = driver?.documents.orEmpty().count { it.status == DocumentStatus.PENDING }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        SettingsTopBar("Vehicle Details") { navigator.goBack() }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .height(158.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFE8EDF7)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.vehicle_ford_transit_cargo_xl),
                        contentDescription = "Ford Transit Cargo XL",
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color(0xFF4B79E6), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("EN ROUTE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(vehicleName, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF262E3F))
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(Color(0xFFD8E6FF), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(plate, color = Color(0xFF4B79E6), fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                }
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF4B79E6), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (driver?.isVerified == true) "Verified Vehicle" else "Verification Pending",
                    color = Color(0xFF4B79E6),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VehicleSpecCard("VEHICLE TYPE", vehicleType, Res.drawable.vehicle_spec_max_payload, Modifier.weight(1f))
                VehicleSpecCard("MODEL YEAR", year, Res.drawable.vehicle_spec_cargo_volume, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VehicleSpecCard("PRIMARY COLOR", fuelOrColor, Res.drawable.vehicle_spec_fuel_type, Modifier.weight(1f))
                VehicleSpecCard("DOCS APPROVED", docsApproved.toString(), Res.drawable.ic_nearby, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color(0xFF8EA5D4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Driver Profile", fontSize = 10.sp, color = Color(0xFF7B88A2))
                            Text(
                                driver?.name?.ifBlank { "Name unavailable" } ?: "Name unavailable",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2C3852),
                                fontSize = 18.sp,
                                lineHeight = 22.sp
                            )
                            Text("Pending Docs: $docsPending", fontSize = 11.sp, color = Color(0xFF7B88A2))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(32.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Phone, contentDescription = null, tint = Color(0xFF8EA5D4), modifier = Modifier.size(16.dp))
                        }
                        Box(Modifier.size(32.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Message, contentDescription = null, tint = Color(0xFF8EA5D4), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(64.dp)
                            .background(Color(0x0D0058BC), CircleShape)
                    )
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Current Capacity Utilization", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp, color = Color(0xFF181C23))
                        Text(
                            "Live telemetry is not available for this vehicle yet. Once dispatch tracking is enabled, utilization and reserved capacity will appear here.",
                            fontSize = 14.sp,
                            color = Color(0xFF414755),
                            lineHeight = 20.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEAF1FF), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Vehicle: $vehicleName",
                                fontSize = 13.sp,
                                color = Color(0xFF2F80ED),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6))
            ) { Text("Track Vehicle Location", fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { navigator.navigateTo(Screen.DocumentsHub) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC6DAF5), contentColor = Color(0xFF4B79E6))
            ) { Text("View Vehicle Manifest", fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(14.dp))
        }
        DriveAppBottomBar(navigator = navigator, items = rememberDriveBottomNavItems())
    }
}

@Composable
private fun VehicleSpecCard(label: String, value: String, iconRes: DrawableResource, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, color = Color(0xFF78839A), fontSize = 9.sp, letterSpacing = 0.8.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = Color(0xFF242C3E), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
    }
}

@Composable
fun DocumentsHubScreen(navigator: AppNavigator) {
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()
    val requiredTypes = remember {
        listOf(
            DocumentType.DRIVERS_LICENSE,
            DocumentType.VEHICLE_REGISTRATION,
            DocumentType.INSURANCE,
            DocumentType.ID_PROOF
        )
    }
    val docsByType = remember(driver?.documents) { driver?.documents.orEmpty().associateBy { it.type } }
    val approvedCount = requiredTypes.count { docsByType[it]?.status == DocumentStatus.APPROVED }
    val pendingCount = requiredTypes.count { docsByType[it]?.status == DocumentStatus.PENDING }
    val rejectedCount = requiredTypes.count { docsByType[it]?.status == DocumentStatus.REJECTED }
    val actionNeededCount = requiredTypes.size - approvedCount
    val compliancePercent = ((approvedCount * 100f) / requiredTypes.size).toInt()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FB))) {
        SettingsTopBar("Documents") { navigator.goBack() }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Compliance Status", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 34.sp, color = Color(0xFF242C3E))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Your required document compliance is $compliancePercent%. Upload missing items and resolve rejected documents to avoid dispatch interruptions.",
                        color = Color(0xFF6D7890),
                        lineHeight = 18.sp,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$approvedCount/${requiredTypes.size}", color = Color(0xFF4B79E6), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(actionNeededCount.toString(), color = Color(0xFF4B79E6), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("ACTION NEEDED", color = Color(0xFF7B88A2), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF4B79E6)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.height(6.dp))
                    Text("Upload New", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("Required for new vehicle registration or license renewal.", color = Color(0xCCFFFFFF), textAlign = TextAlign.Center, fontSize = 11.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { navigator.navigateTo(Screen.DocumentUpload) }, colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF4B79E6))) {
                        Text("Start Upload", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("REQUIRED DOCUMENTATION", color = Color(0xFF6D7890), fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, fontSize = 10.sp)
            Spacer(Modifier.height(8.dp))
            requiredTypes.forEach { type ->
                DocumentRow(type = type, document = docsByType[type])
            }
            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    Text("History", fontWeight = FontWeight.SemiBold, color = Color(0xFF2C3852))
                    Text(
                        "Total uploaded: ${driver?.documents.orEmpty().size}. Approved: ${driver?.documents.orEmpty().count { it.status == DocumentStatus.APPROVED }}.",
                        fontSize = 11.sp,
                        color = Color(0xFF7B88A2)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Need help?", fontWeight = FontWeight.SemiBold, color = Color(0xFF2C3852))
                    Text("Having trouble scanning your documents? Contact support team for manual verification.", fontSize = 11.sp, color = Color(0xFF7B88A2))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        DriveAppBottomBar(navigator = navigator, items = rememberDriveBottomNavItems())
    }
}

@Composable
private fun DocumentRow(type: DocumentType, document: Document?) {
    val statusLabel = when (document?.status) {
        DocumentStatus.APPROVED -> "VERIFIED"
        DocumentStatus.PENDING -> "PENDING"
        DocumentStatus.REJECTED -> "REJECTED"
        null -> "MISSING"
    }
    val subtitle = when {
        document == null -> "Not uploaded yet"
        document.uploadedAt.isNullOrBlank() -> "Uploaded"
        else -> "Uploaded: ${document.uploadedAt.take(10)}"
    }

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF)), shape = RoundedCornerShape(10.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(type.displayName, fontWeight = FontWeight.Bold, color = Color(0xFF2C3852), fontSize = 14.sp)
                Text(subtitle, color = Color(0xFF6D7890), fontSize = 12.sp)
            }
            val pillColor = when (document?.status) {
                DocumentStatus.APPROVED -> Color(0xFFD2E2FF)
                DocumentStatus.PENDING -> Color(0xFFEAF1FF)
                DocumentStatus.REJECTED -> Color(0xFFFFE2E2)
                null -> Color(0xFFE5E7EB)
            }
            val textColor = when (document?.status) {
                DocumentStatus.APPROVED -> Color(0xFF4B79E6)
                DocumentStatus.PENDING -> Color(0xFF2F80ED)
                DocumentStatus.REJECTED -> Color(0xFFB42318)
                null -> Color(0xFF6B7280)
            }
            Box(Modifier.background(pillColor, RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text(statusLabel, color = textColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun TermsOfServiceScreen(navigator: AppNavigator) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FB))) {
        SettingsTopBar("Terms of Service") { navigator.goBack() }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            Box(Modifier.background(Color(0xFFD4E3FF), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text("LAST UPDATED: OCT 24, 2023", color = Color(0xFF4B79E6), fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text("Legal Agreement &\nTerms of Service", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 35.sp, color = Color(0xFF2C3852))
            Spacer(Modifier.height(8.dp))
            Text("Welcome to Cargo Stream. These Terms of Service constitute a legally binding agreement between you and Cargo Stream Logistics Corp.", color = Color(0xFF6D7890), fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(12.dp))
            TermsCard("Usage Rules", "Mandatory compliance with safety protocols, route adherence, and professional conduct standards.")
            Spacer(Modifier.height(8.dp))
            TermsCard("Liability", "Detailed limitations regarding cargo damage, delays, and insurance coverage responsibilities.")
            Spacer(Modifier.height(14.dp))
            TermsSection("1. Introduction", "By accessing or using the Cargo Stream platform, you agree to be bound by these terms.")
            TermsSection("2. Usage Rules & Conduct", "Drivers must not use handheld interaction while in motion. Data and status updates must be accurate and timely.")
            TermsSection("3. Limitation of Liability", "Cargo Stream shall not be liable for indirect, incidental, or consequential damages to the maximum extent allowed by law.")
            TermsSection("4. Account Termination", "We may suspend access for violations, repeated safety incidents, or prolonged inactivity.")
            Spacer(Modifier.height(18.dp))
            Button(onClick = { navigator.goBack() }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6))) {
                Text("Accept & Continue", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { navigator.goBack() }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(10.dp)) {
                Text("Decline")
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun TermsCard(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F4FA))) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2C3852))
            Spacer(Modifier.height(6.dp))
            Text(content, color = Color(0xFF6D7890), lineHeight = 17.sp, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TermsSection(title: String, content: String) {
    Spacer(Modifier.height(14.dp))
    Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF2C3852))
    Spacer(Modifier.height(6.dp))
    Text(content, color = Color(0xFF6D7890), fontSize = 12.sp, lineHeight = 18.sp)
}

@Composable
fun PrivacyPolicyScreen(navigator: AppNavigator) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FB))) {
        SettingsTopBar("Settings") { navigator.goBack() }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            Text("LEGAL DOCUMENTATION", color = Color(0xFF4B79E6), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Text("Privacy Policy", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 35.sp, color = Color(0xFF2C3852))
            Spacer(Modifier.height(6.dp))
            Text("Last updated: October 24, 2023. This policy describes how Cargo Stream collects, uses, and protects your information as a professional driver.", color = Color(0xFF6D7890), lineHeight = 18.sp, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Data Collection", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("We collect telemetry, GPS coordinates, and vehicle diagnostics to ensure route efficiency and driver safety.", color = Color(0xFF6D7890), fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip("GPS Tracking")
                        Chip("Biometric Login")
                        Chip("Route Logs")
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF4B79E6))) {
                Column(Modifier.padding(14.dp)) {
                    Text("Your Rights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("You have full control over your personal data including the right to access, rectify, or delete your profile.", color = Color(0xE6FFFFFF), fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Request Data Access →", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("1. Information We Collect", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Bullet("Personal identifiers: Name, driver's license number, employee ID, and contact information.")
            Bullet("Geolocation data: Real-time tracking while app is active or in background mode during shifts.")
            Bullet("Technical data: IP address, operating system, and crash diagnostics.")
            Spacer(Modifier.height(12.dp))
            Text("2. Sharing Policy", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text("We do not sell your personal data. We share only with your employer and essential service providers.", color = Color(0xFF6D7890), lineHeight = 18.sp, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Text("3. Security Standards", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Bullet("AES-256 encryption at rest and in transit.")
            Bullet("Multi-factor authentication requirements.")
            Bullet("24/7 security operations monitoring.")
            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3FE))) {
                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Still have questions?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Our dedicated privacy officer is here to help you understand your data usage.", textAlign = TextAlign.Center, color = Color(0xFF6D7890), fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { navigator.navigateTo(Screen.HelpCenter) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6))) {
                        Text("Contact Privacy Officer")
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(Modifier.background(Color(0xFFD8E6FF), RoundedCornerShape(999.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, color = Color(0xFF4B79E6), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Bullet(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text("•", fontSize = 14.sp, color = Color(0xFF2C3852), modifier = Modifier.padding(end = 8.dp))
        Text(text, color = Color(0xFF6D7890), lineHeight = 18.sp, fontSize = 12.sp)
    }
}
