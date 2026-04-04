package com.company.carryon.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

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

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))
    ) {
        SettingsTopBar("Settings") { navigator.goBack() }
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
        ) {
            Text("COMMUNICATION", color = Color(0xFF2F80ED), fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
            Text("Notification\nPreferences", fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 42.sp, color = Color(0xFF181C23))
            Spacer(Modifier.height(8.dp))
            Text("Manage how you receive updates about your deliveries, earnings, and platform news.", color = Color(0xFF414755), fontSize = 16.sp, lineHeight = 24.sp)
            Spacer(Modifier.height(20.dp))

            NotificationCard("Push Notifications", "The master switch for all immediate device-level alerts and sound notifications.", Icons.Filled.Notifications, push) { push = it }
            NotificationCard("Order Updates", "Real-time status changes, pickup assignments, and critical delivery instructions.", Icons.Filled.LocalShipping, orders) { orders = it }
            NotificationCard("Earnings Reports", "Weekly summaries, instant payout confirmations, and monthly tax document alerts.", Icons.Filled.AccountBalanceWallet, earnings) { earnings = it }
            NotificationCard("App Promotions", "New feature announcements, seasonal bonuses, and exclusive partnership offers.", Icons.Filled.Campaign, promos) { promos = it }

            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F80ED))
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Need a break?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("You can temporarily silence all alerts using Focus Mode during your off-hours.", color = Color(0xCCFFFFFF))
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { }, colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF2F80ED))) {
                        Text("Enable Focus Mode", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NotificationCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    Icon(icon, contentDescription = null, tint = Color(0xFF2F80ED))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF2F80ED), lineHeight = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(description, color = Color(0xFF414755), fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
fun LanguageScreen(navigator: AppNavigator) {
    var selected by remember { mutableStateOf("English") }
    val languages = listOf(
        "English" to "United States / UK",
        "Español" to "Spanish",
        "Français" to "French",
        "Deutsch" to "German",
        "اردو" to "Urdu",
        "हिन्दी" to "Hindi",
    )
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        SettingsTopBar("Settings") { navigator.goBack() }
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
        ) {
            Text("Language", fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, lineHeight = 46.sp, color = Color(0xFF181C23))
            Spacer(Modifier.height(8.dp))
            Text("Select your preferred language for the navigation and dashboard interface.", color = Color(0xFF414755), fontSize = 16.sp, lineHeight = 24.sp)
            Spacer(Modifier.height(20.dp))
            languages.forEach { (name, subtitle) ->
                val selectedRow = selected == name
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { selected = name },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedRow) Color.White else Color(0x33A6D2F3)),
                    border = if (selectedRow) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF0058BC)) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(if (selectedRow) Color(0x140070EB) else Color.White, RoundedCornerShape(999.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Language, contentDescription = null, tint = Color(0xFF2F80ED))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                                Text(subtitle, color = Color(0xFF64748B), fontSize = 14.sp)
                            }
                        }
                        Icon(
                            imageVector = if (selectedRow) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selectedRow) Color(0xFF2F80ED) else Color(0xFFB0B7C8)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F80ED))
            ) {
                Column(Modifier.padding(24.dp)) {
                    Icon(Icons.Filled.Public, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text("Global Connectivity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Changes will apply across all your linked logistics terminals and driver manifests instantly.", color = Color(0xE6FFFFFF))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun VehicleInfoScreen(navigator: AppNavigator) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        SettingsTopBar("Vehicle Details") { navigator.goBack() }
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Box(Modifier.height(180.dp).fillMaxWidth().background(Color(0xFFE6ECF5)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = Color(0xFF2F80ED), modifier = Modifier.size(64.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Ford Transit - Cargo XL", fontSize = 42.sp, lineHeight = 42.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF181C23))
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(Color(0xFFA6D2F3), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("KCX 4821-B", color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF2F80ED), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Verified Vehicle", color = Color(0xFF2F80ED), fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleSpecCard("MAX PAYLOAD", "2500 kg", Icons.Filled.Inventory2, Modifier.weight(1f))
                VehicleSpecCard("CARGO VOLUME", "12 m³", Icons.Filled.ViewInAr, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VehicleSpecCard("FUEL TYPE", "Electric", Icons.Filled.Bolt, Modifier.weight(1f))
                VehicleSpecCard("RANGE", "320 km", Icons.Filled.LocationOn, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))) {
                Text("Track Vehicle Location", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA6D2F3), contentColor = Color(0xFF2F80ED))) {
                Text("View Vehicle Manifest", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VehicleSpecCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2F80ED))
            Text(label, color = Color(0xFF414755), fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = Color(0xFF181C23), fontWeight = FontWeight.Bold, fontSize = 28.sp)
        }
    }
}

@Composable
fun DocumentsHubScreen(navigator: AppNavigator) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        SettingsTopBar("Documents") { navigator.goBack() }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(24.dp)) {
                    Text("Compliance Status", fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 40.sp, color = Color(0xFF181C23))
                    Spacer(Modifier.height(8.dp))
                    Text("Your operational documents are 85% compliant. Please renew expiring items to avoid dispatch interruptions.", color = Color(0xFF414755), lineHeight = 22.sp)
                    Spacer(Modifier.height(16.dp))
                    Row {
                        Text("3/4", color = Color(0xFF2F80ED), fontWeight = FontWeight.ExtraBold, fontSize = 42.sp)
                        Spacer(Modifier.width(20.dp))
                        Text("1", color = Color(0xFF2F80ED), fontWeight = FontWeight.ExtraBold, fontSize = 42.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2F80ED)), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Upload New", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                    Text("Required for new vehicle registration or license renewal.", color = Color(0xCCFFFFFF), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { navigator.navigateTo(Screen.DocumentUpload) }, colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF2F80ED))) {
                        Text("Start Upload", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("REQUIRED DOCUMENTATION", color = Color(0xFF414755), fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            DocumentRow("Driver's License", "Expires: Oct 24, 2026", "VERIFIED")
            DocumentRow("Vehicle Insurance", "Expires in 12 Days", "EXPIRING SOON")
            DocumentRow("Registration Certificate", "Expires: Jan 15, 2025", "VERIFIED")
            DocumentRow("Health Insurance", "Expired: 3 Days ago", "EXPIRED")
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DocumentRow(title: String, subtitle: String, status: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3)), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF181C23), fontSize = 16.sp)
                Text(subtitle, color = Color(0xFF414755), fontSize = 13.sp)
            }
            Box(Modifier.background(Color(0xFFA6D2F3), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(status, color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun TermsOfServiceScreen(navigator: AppNavigator) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        SettingsTopBar("Terms of Service") { navigator.goBack() }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Box(Modifier.background(Color(0xFFA6D2F3), RoundedCornerShape(999.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text("LAST UPDATED: OCT 24, 2023", color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(Modifier.height(14.dp))
            Text("Legal Agreement &\nTerms of Service", fontWeight = FontWeight.ExtraBold, fontSize = 42.sp, lineHeight = 44.sp, color = Color(0xFF181C23))
            Spacer(Modifier.height(10.dp))
            Text("Welcome to Cargo Stream. These Terms of Service constitute a legally binding agreement between you and Cargo Stream Logistics Corp.", color = Color(0xFF414755), fontSize = 16.sp, lineHeight = 24.sp)
            Spacer(Modifier.height(16.dp))
            TermsCard("Usage Rules", "Mandatory compliance with safety protocols, route adherence, and professional conduct standards.")
            Spacer(Modifier.height(10.dp))
            TermsCard("Liability", "Detailed limitations regarding cargo damage, delays, and insurance coverage responsibilities.")
            Spacer(Modifier.height(20.dp))
            TermsSection("1. Introduction", "By accessing or using the Cargo Stream platform, you agree to be bound by these terms.")
            TermsSection("2. Usage Rules & Conduct", "Drivers must not use handheld interaction while in motion. Data and status updates must be accurate and timely.")
            TermsSection("3. Limitation of Liability", "Cargo Stream shall not be liable for indirect, incidental, or consequential damages to the maximum extent allowed by law.")
            TermsSection("4. Account Termination", "We may suspend access for violations, repeated safety incidents, or prolonged inactivity.")
            Spacer(Modifier.height(24.dp))
            Button(onClick = { navigator.goBack() }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))) {
                Text("Accept & Continue", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { navigator.goBack() }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Decline")
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun TermsCard(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(20.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF181C23))
            Spacer(Modifier.height(6.dp))
            Text(content, color = Color(0xFF414755), lineHeight = 22.sp)
        }
    }
}

@Composable
private fun TermsSection(title: String, content: String) {
    Spacer(Modifier.height(18.dp))
    Text(title, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF181C23))
    Spacer(Modifier.height(8.dp))
    Text(content, color = Color(0xFF414755), fontSize = 16.sp, lineHeight = 24.sp)
}

@Composable
fun PrivacyPolicyScreen(navigator: AppNavigator) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF9F9FF))) {
        SettingsTopBar("Settings") { navigator.goBack() }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text("LEGAL DOCUMENTATION", color = Color(0xFF0058BC), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Privacy Policy", fontWeight = FontWeight.ExtraBold, fontSize = 42.sp, lineHeight = 42.sp, color = Color(0xFF181C23))
            Spacer(Modifier.height(8.dp))
            Text("Last updated: October 24, 2023. This policy describes how Cargo Stream collects, uses, and protects your information as a professional driver.", color = Color(0xFF414755), lineHeight = 24.sp)
            Spacer(Modifier.height(18.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Data Collection", fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("We collect telemetry, GPS coordinates, and vehicle diagnostics to ensure route efficiency and driver safety.", color = Color(0xFF414755))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip("GPS Tracking")
                        Chip("Biometric Login")
                        Chip("Route Logs")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF2F80ED))) {
                Column(Modifier.padding(20.dp)) {
                    Text("Your Rights", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("You have full control over your personal data including the right to access, rectify, or delete your profile.", color = Color(0xE6FFFFFF))
                    Spacer(Modifier.height(10.dp))
                    Text("Request Data Access →", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("1. Information We Collect", fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Bullet("Personal identifiers: Name, driver's license number, employee ID, and contact information.")
            Bullet("Geolocation data: Real-time tracking while app is active or in background mode during shifts.")
            Bullet("Technical data: IP address, operating system, and crash diagnostics.")
            Spacer(Modifier.height(18.dp))
            Text("2. Sharing Policy", fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text("We do not sell your personal data. We share only with your employer and essential service providers.", color = Color(0xFF414755), lineHeight = 24.sp)
            Spacer(Modifier.height(18.dp))
            Text("3. Security Standards", fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Bullet("AES-256 encryption at rest and in transit.")
            Bullet("Multi-factor authentication requirements.")
            Bullet("24/7 security operations monitoring.")
            Spacer(Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3FE))) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Still have questions?", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Our dedicated privacy officer is here to help you understand your data usage.", textAlign = TextAlign.Center, color = Color(0xFF414755))
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { navigator.navigateTo(Screen.HelpCenter) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))) {
                        Text("Contact Privacy Officer")
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(Modifier.background(Color(0xFFA6D2F3), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(text, color = Color(0xFF2F80ED), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Bullet(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text("•", fontSize = 18.sp, color = Color(0xFF181C23), modifier = Modifier.padding(end = 8.dp))
        Text(text, color = Color(0xFF414755), lineHeight = 24.sp, fontSize = 16.sp)
    }
}

