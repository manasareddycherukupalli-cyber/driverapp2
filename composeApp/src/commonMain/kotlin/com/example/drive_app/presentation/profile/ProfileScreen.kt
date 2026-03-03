package com.example.drive_app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.presentation.components.*
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*

/**
 * ProfileScreen — Driver profile overview with menu items.
 */
@Composable
fun ProfileScreen(navigator: AppNavigator) {
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Profile",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Profile card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { navigator.navigateTo(Screen.EditProfile) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarCircle(
                    initials = driver?.name?.take(2)?.uppercase() ?: "RK",
                    size = 64.dp
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driver?.name ?: "Rajesh Kumar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = driver?.phone ?: "+91 9876543210",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Yellow500, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${driver?.rating ?: 4.8} • ${driver?.totalDeliveries ?: 1247} deliveries",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Vehicle details card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏍️", fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${driver?.vehicleDetails?.make ?: "Honda"} ${driver?.vehicleDetails?.model ?: "CB Shine"}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = driver?.vehicleDetails?.licensePlate ?: "MH-12-AB-1234",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(
                    text = "Verified",
                    color = Green500,
                    backgroundColor = Green100
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Menu items
        ProfileMenuItem(Icons.Filled.Edit, "Edit Profile", "Update your personal details") {
            navigator.navigateTo(Screen.EditProfile)
        }
        ProfileMenuItem(Icons.Filled.DirectionsCar, "Vehicle Details", "Manage your vehicle info") {
            navigator.navigateTo(Screen.VehicleDetailsInput)
        }
        ProfileMenuItem(Icons.Filled.Description, "Documents", "View document status") {
            navigator.navigateTo(Screen.VerificationStatus)
        }
        ProfileMenuItem(Icons.Filled.Star, "My Ratings", "View ratings & feedback") {
            navigator.navigateTo(Screen.Ratings)
        }

        AppDivider(modifier = Modifier.padding(vertical = 8.dp))

        ProfileMenuItem(Icons.Filled.Settings, "Settings", "App preferences") {
            navigator.navigateTo(Screen.Settings)
        }
        ProfileMenuItem(Icons.AutoMirrored.Filled.HelpCenter, "Help Center", "FAQs & support") {
            navigator.navigateTo(Screen.HelpCenter)
        }
        ProfileMenuItem(Icons.Filled.Warning, "Emergency SOS", "Safety features") {
            navigator.navigateTo(Screen.Sos)
        }

        AppDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Logout
        ProfileMenuItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            title = "Logout",
            subtitle = "Sign out of your account",
            tint = Red500
        ) {
            viewModel.logout()
            navigator.navigateAndClearStack(Screen.Login)
        }

        Spacer(Modifier.height(24.dp))

        // App version
        Text(
            text = "DriveApp v1.0.0",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = if (tint == Red500) Red500 else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
