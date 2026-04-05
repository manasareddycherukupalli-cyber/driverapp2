package com.company.carryon.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.components.AvatarCircle
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

@Composable
fun SettingsScreen(navigator: AppNavigator) {
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()

    var darkMode by remember { mutableStateOf(false) }
    var useMiles by remember { mutableStateOf(true) }

    val sectionBg = Color(0x33A6D2F3)
    val blue = Color(0xFF2F80ED)
    val textMuted = Color(0xFF414755)
    val driverInitials = remember(driver?.name) { initialsFromName(driver?.name, "CO") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9FF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.goBack() }) {
                Icon(Icons.Filled.Menu, contentDescription = "Back", tint = blue)
            }
            Text("Carry On", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color(0xFF0F172A))
            AvatarCircle(
                initials = driverInitials,
                size = 40.dp
            )
        }

        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarCircle(
                    initials = driverInitials,
                    size = 72.dp
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(driver?.name ?: "Alex Navigator", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("Premium Logistics", color = textMuted, fontSize = 14.sp)
                    Text("Partner", color = textMuted, fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = blue)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val rating = driver?.rating ?: 4.92
                val ratingText = ((rating * 100.0).toInt() / 100.0).toString()
                Text(ratingText, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
                Text("LIFETIME RATING", color = Color(0xCCFFFFFF), fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        SettingsSection("Account Settings")
        SettingsNavRow("Edit Profile", Icons.Filled.Person, sectionBg) { navigator.navigateTo(Screen.EditProfile) }
        SettingsNavRow("Notification Preferences", Icons.Filled.Notifications, sectionBg) { navigator.navigateTo(Screen.NotificationPreferences) }
        SettingsNavRow("Language", Icons.Filled.Language, sectionBg, "English (US)") { navigator.navigateTo(Screen.Language) }

        Spacer(Modifier.height(20.dp))
        SettingsSection("Vehicle Settings")
        SettingsNavRow("Vehicle Details", Icons.Filled.LocalShipping, sectionBg) { navigator.navigateTo(Screen.VehicleInfo) }
        SettingsNavRow("Documents", Icons.Filled.Description, sectionBg, trailingBadge = "1 EXPIRED") {
            navigator.navigateTo(Screen.DocumentsHub)
        }

        Spacer(Modifier.height(20.dp))
        SettingsSection("App Preferences")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = sectionBg)
        ) {
            Column(Modifier.padding(8.dp)) {
                PrefToggleRow("Dark Mode", Icons.Filled.Brightness2, darkMode) { darkMode = it }
                UnitToggleRow(useMiles = useMiles, onMilesSelected = { useMiles = true }, onKmSelected = { useMiles = false })
                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Map, contentDescription = null, tint = Color(0xFF181C23), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Map Provider", color = Color(0xFF181C23), fontWeight = FontWeight.SemiBold)
                        }
                        Text("Google Maps", color = blue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SettingsSection("Support")
        SupportRow("Help Center") { navigator.navigateTo(Screen.HelpCenter) }
        SupportRow("Privacy Policy") { navigator.navigateTo(Screen.PrivacyPolicy) }
        SupportRow("Terms of Service") { navigator.navigateTo(Screen.TermsOfService) }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.logout()
                navigator.navigateAndClearStack(Screen.Login)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = blue)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "CARRY ON DRIVER APP V4.2.0-STABLE",
            color = Color(0x66414755),
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
    }
}

private fun initialsFromName(name: String?, fallback: String): String {
    if (name.isNullOrBlank()) return fallback
    val initials = name
        .trim()
        .split(Regex("\\s+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
    return if (initials.isNotEmpty()) initials else fallback
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        color = Color(0xFF181C23),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
    )
}

@Composable
private fun SettingsNavRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    subtitle: String? = null,
    trailingBadge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0x66FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF2F80ED), modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        if (trailingBadge != null) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(Color(0xFFA6D2F3)).padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(trailingBadge, color = Color(0xFF2F80ED), fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                            }
                        }
                    }
                    if (subtitle != null) {
                        Text(subtitle, color = Color(0xFF414755), fontSize = 12.sp)
                    }
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun PrefToggleRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF181C23), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, color = Color(0xFF181C23), fontWeight = FontWeight.SemiBold)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun UnitToggleRow(useMiles: Boolean, onMilesSelected: () -> Unit, onKmSelected: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Straighten, contentDescription = null, tint = Color(0xFF181C23), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text("Units", color = Color(0xFF181C23), fontWeight = FontWeight.SemiBold)
        }
        Row(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFA6D2F3)).padding(4.dp)
        ) {
            AssistChip(
                onClick = onMilesSelected,
                label = { Text("Miles", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (useMiles) Color.White else Color.Transparent,
                    labelColor = if (useMiles) Color(0xFF2F80ED) else Color(0xFF414755)
                )
            )
            Spacer(Modifier.width(4.dp))
            AssistChip(
                onClick = onKmSelected,
                label = { Text("KM", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (useMiles) Color.Transparent else Color.White,
                    labelColor = if (useMiles) Color(0xFF414755) else Color(0xFF2F80ED)
                )
            )
        }
    }
}

@Composable
private fun SupportRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF414755), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = Color(0xFF414755), fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
