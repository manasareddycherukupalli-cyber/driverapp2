package com.example.drive_app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.presentation.components.DriveAppTopBar
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.theme.*

/**
 * SettingsScreen — App settings: notifications, language, theme, about.
 */
@Composable
fun SettingsScreen(navigator: AppNavigator) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }
    var autoAcceptJobs by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Settings",
            onBackClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Notifications section
            Text(
                text = "Notifications",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsToggle(
                title = "Push Notifications",
                subtitle = "Receive job alerts and updates",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            SettingsToggle(
                title = "Sound",
                subtitle = "Play sound for notifications",
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it }
            )
            SettingsToggle(
                title = "Vibration",
                subtitle = "Vibrate on new notifications",
                checked = vibrationEnabled,
                onCheckedChange = { vibrationEnabled = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Job Preferences
            Text(
                text = "Job Preferences",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsToggle(
                title = "Auto-Accept Jobs",
                subtitle = "Automatically accept nearby job requests",
                checked = autoAcceptJobs,
                onCheckedChange = { autoAcceptJobs = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Appearance
            Text(
                text = "Appearance",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsToggle(
                title = "Dark Mode",
                subtitle = "Use dark theme",
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )

            // Language selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Language", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        Text("English", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About
            Text(
                text = "About",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsInfoRow("App Version", "1.0.0")
            SettingsInfoRow("Build", "2026.03.01")
            SettingsInfoRow("Terms of Service", "View →")
            SettingsInfoRow("Privacy Policy", "View →")

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Orange500,
                    checkedTrackColor = Orange500.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
