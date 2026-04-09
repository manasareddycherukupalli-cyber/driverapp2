package com.company.carryon.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.navigation.AppNavigator
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.edit_profile_avatar
import org.jetbrains.compose.resources.painterResource

private val EditBg = Color(0xFFF9F9FF)
private val EditBlue = Color(0xFF2F80ED)
private val EditBody = Color(0xFF414755)
private val EditInputBg = Color(0x33A6D2F3)

@Composable
fun EditProfileScreen(navigator: AppNavigator) {
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var name by remember { mutableStateOf(driver?.name ?: "Marcus Thompson") }
    var email by remember { mutableStateOf(driver?.email ?: "m.thompson@logistics.com") }
    val phone = driver?.phone?.ifBlank { "+1 (555) 928-3401" } ?: "+1 (555) 928-3401"
    val address = "742 Evergreen Terrace,\nSpringfield, IL 62704"

    LaunchedEffect(driver) {
        driver?.let {
            if (name == "Marcus Thompson") name = it.name.ifBlank { name }
            if (email == "m.thompson@logistics.com") email = it.email.ifBlank { email }
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) navigator.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 170.dp)
        ) {
            EditTopBar(navigator)

            Column(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit Profile", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Update your professional details and driver\ncredentials.",
                    color = EditBody,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color(0xFFD8E2FF), CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.edit_profile_avatar),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Change Photo", color = Color(0xFF0058BC), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                AccountStatusCard()
                ProfileFormCard(name = name, email = email, phone = phone, address = address)
                PasswordCard()

                if (updateState is UiState.Error) {
                    Text((updateState as UiState.Error).message, color = Color(0xFFB3261E), fontSize = 12.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.updateProfile(name, email, "") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EditBlue)
            ) {
                Text("Save Changes", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { navigator.goBack() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA6D2F3), contentColor = EditBlue)
            ) {
                Text("Cancel", color = EditBlue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EditTopBar(navigator: AppNavigator) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navigator.goBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EditBlue, modifier = Modifier.size(16.dp))
        }
        Text("Settings", fontSize = 20.sp, lineHeight = 28.sp, color = Color(0xFF181C23), fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("DRIVER PORTAL", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun AccountStatusCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(EditInputBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(40.dp)
                        .background(EditBlue, RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("ACCOUNT STATUS", color = EditBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text("Verified Partner", color = Color.Black, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                "Your profile information is visible to\ndispatchers and warehouse managers to\nensure secure hand-offs.",
                color = EditBody,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ProfileFormCard(name: String, email: String, phone: String, address: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InfoField("Full Name", name, Icons.Filled.PersonOutline, singleLine = true)
        InfoField("Email Address", email, Icons.Filled.Email, singleLine = true)
        InfoField("Phone Number", phone, Icons.Filled.Phone, singleLine = true)
        InfoField("Residential Address", address, Icons.Filled.LocationOn, singleLine = false)
    }
}

@Composable
private fun InfoField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = EditBody, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditInputBg, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x33C1C6D7), RoundedCornerShape(12.dp))
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = if (singleLine) 14.dp else 16.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF8A96A9), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Text(value, color = Color.Black, fontSize = 16.sp, lineHeight = 24.sp)
        }
    }
}

@Composable
private fun PasswordCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EditInputBg, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x1AC1C6D7), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE6E8F3), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.History, contentDescription = null, tint = Color(0xFF5E6C89), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Update Password", color = Color(0xFF181C23), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Last changed 4 months ago", color = EditBody, fontSize = 12.sp)
            }
        }
        Text("Change", color = Color(0xFF0058BC), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { })
    }
}
