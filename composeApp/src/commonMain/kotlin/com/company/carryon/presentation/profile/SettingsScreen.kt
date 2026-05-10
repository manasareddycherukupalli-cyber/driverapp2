package com.company.carryon.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.company.carryon.data.model.DocumentStatus
import com.company.carryon.data.network.getLanguage
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.i18n.getLanguageDisplayName
import com.company.carryon.presentation.components.AvatarCircle
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

@Composable
fun SettingsScreen(
    navigator: AppNavigator,
    onLanguageChanged: (String) -> Unit = {}
) {
    val strings = LocalStrings.current
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()

    var darkMode by remember { mutableStateOf(false) }
    var useMiles by remember { mutableStateOf(true) }

    val sectionBg = Color(0x33A6D2F3)
    val blue = Color(0xFF2F80ED)
    val textMuted = Color(0xFF414755)
    val driverInitials = remember(driver?.name) { initialsFromName(driver?.name, "CO") }
    val documentsBadge = remember(driver?.documents) {
        val rejected = driver?.documents.orEmpty().count { it.status == DocumentStatus.REJECTED }
        val pending = driver?.documents.orEmpty().count { it.status == DocumentStatus.PENDING }
        when {
            rejected > 0 -> "$rejected REJECTED"
            pending > 0 -> "$pending PENDING"
            driver?.documents.isNullOrEmpty() -> "SETUP"
            else -> "ALL GOOD"
        }
    }

    val currentLangCode = remember { getLanguage() ?: "en" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9FF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        DriveAppTopBar(
            title = "Profile",
            onBackClick = { navigator.goBack() },
            leadingIcon = Icons.Filled.Menu,
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
            showTitle = false
        )

        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                AvatarCircle(initials = driverInitials, size = 72.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(driver?.name?.ifBlank { "--" } ?: "--", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(strings.premiumLogistics, color = textMuted, fontSize = 14.sp)
                    Text(strings.partner, color = textMuted, fontSize = 14.sp)
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
                val rating = driver?.rating
                val ratingText = if (rating != null) ((rating * 100.0).toInt() / 100.0).toString() else "--"
                Text(ratingText, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
                Text(strings.lifetimeRating, color = Color(0xCCFFFFFF), fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        SettingsSection(strings.accountSettings)
        SettingsNavRow(strings.editProfile, Icons.Filled.Person, sectionBg) { navigator.navigateTo(Screen.EditProfile) }
        SettingsNavRow(strings.notificationPreferences, Icons.Filled.Notifications, sectionBg) { navigator.navigateTo(Screen.NotificationPreferences) }
        SettingsNavRow(strings.language, Icons.Filled.Language, sectionBg, getLanguageDisplayName(currentLangCode)) { navigator.navigateTo(Screen.Language) }

        Spacer(Modifier.height(20.dp))
        SettingsSection(strings.vehicleSettings)
        SettingsNavRow(strings.vehicleDetails, Icons.Filled.LocalShipping, sectionBg) { navigator.navigateTo(Screen.VehicleInfo) }
        SettingsNavRow(strings.documents, Icons.Filled.Description, sectionBg, trailingBadge = documentsBadge) {
            navigator.navigateTo(Screen.DocumentsHub)
        }

        Spacer(Modifier.height(20.dp))
        SettingsSection(strings.appPreferences)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = sectionBg)
        ) {
            Column(Modifier.padding(8.dp)) {
                PrefToggleRow(strings.darkMode, Icons.Filled.Brightness2, darkMode) { darkMode = it }
                UnitToggleRow(
                    strings = strings,
                    useMiles = useMiles,
                    onMilesSelected = { useMiles = true },
                    onKmSelected = { useMiles = false }
                )
                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Map, contentDescription = null, tint = Color(0xFF181C23), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(strings.mapProvider, color = Color(0xFF181C23), fontWeight = FontWeight.SemiBold)
                        }
                        Text(strings.googleMaps, color = blue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SettingsSection(strings.support)
        SupportRow(strings.helpCenter) { navigator.navigateTo(Screen.HelpCenter) }
        SupportRow(strings.privacyPolicy) { navigator.navigateTo(Screen.PrivacyPolicy) }
        SupportRow(strings.termsOfService) { navigator.navigateTo(Screen.TermsOfService) }

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
            Text(strings.logOut, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(Modifier.height(12.dp))
        Text(
            strings.appVersionLabel,
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
    val initials = name.trim().split(Regex("\\s+"))
        .map { it.trim() }.filter { it.isNotEmpty() }.take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
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
private fun PrefToggleRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
private fun UnitToggleRow(
    strings: com.company.carryon.i18n.DriverStrings,
    useMiles: Boolean,
    onMilesSelected: () -> Unit,
    onKmSelected: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Straighten, contentDescription = null, tint = Color(0xFF181C23), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(strings.units, color = Color(0xFF181C23), fontWeight = FontWeight.SemiBold)
        }
        Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFA6D2F3)).padding(4.dp)) {
            AssistChip(
                onClick = onMilesSelected,
                label = { Text(strings.miles, fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (useMiles) Color.White else Color.Transparent,
                    labelColor = if (useMiles) Color(0xFF2F80ED) else Color(0xFF414755)
                )
            )
            Spacer(Modifier.width(4.dp))
            AssistChip(
                onClick = onKmSelected,
                label = { Text(strings.km, fontSize = 12.sp) },
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
