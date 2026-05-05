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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DocumentType
import com.company.carryon.data.model.UiState
import com.company.carryon.data.network.HttpClientFactory
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.util.toImageBitmap
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.edit_profile_avatar
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.jetbrains.compose.resources.painterResource

internal val EditBg = Color(0xFFF9F9FF)
internal val EditBlue = Color(0xFF2F80ED)
internal val EditBody = Color(0xFF414755)
internal val EditInputBg = Color(0x33A6D2F3)

@Composable
fun EditProfileScreen(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val viewModel = remember { ProfileViewModel() }
    val driver by viewModel.currentDriver.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val profilePhotoUrl = remember(driver) {
        driver?.profileImageUrl?.takeIf { it.isNotBlank() }
            ?: driver?.documents
                ?.firstOrNull { it.type == DocumentType.PROFILE_PHOTO }
                ?.imageUrl
                ?.takeIf { it.isNotBlank() }
    }
    var profilePhotoBytes by remember(profilePhotoUrl) { mutableStateOf<ByteArray?>(null) }
    val profilePhotoBitmap = remember(profilePhotoBytes) { profilePhotoBytes?.toImageBitmap() }

    var name by remember { mutableStateOf(driver?.name.orEmpty()) }
    var email by remember { mutableStateOf(driver?.email.orEmpty()) }
    var phone by remember { mutableStateOf(driver?.phone.orEmpty()) }
    val address = remember(driver) {
        listOfNotNull(
            driver?.addressLine1?.takeIf { it.isNotBlank() },
            driver?.addressLine2?.takeIf { it.isNotBlank() },
            driver?.city?.takeIf { it.isNotBlank() },
            driver?.postcode?.takeIf { it.isNotBlank() },
            driver?.state?.name?.replace('_', ' ')?.takeIf { it.isNotBlank() }
        ).joinToString(", ").ifBlank { "Address unavailable" }
    }

    LaunchedEffect(driver) {
        driver?.let {
            if (it.name.isNotBlank()) name = it.name
            if (it.email.isNotBlank()) email = it.email
            phone = it.phone
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) navigator.goBack()
    }

    LaunchedEffect(profilePhotoUrl) {
        profilePhotoBytes = if (profilePhotoUrl.isNullOrBlank()) {
            null
        } else {
            runCatching { HttpClientFactory.client.get(profilePhotoUrl).body<ByteArray>() }.getOrNull()
        }
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
                Text(strings.editProfile, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    strings.updateYourDetails,
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
                        if (profilePhotoBitmap != null) {
                            Image(
                                bitmap = profilePhotoBitmap,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(Res.drawable.edit_profile_avatar),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        strings.changePhoto,
                        color = Color(0xFF0058BC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { navigator.navigateTo(Screen.PersonalIdentity) }
                    )
                }

                AccountStatusCard(isVerified = driver?.isVerified == true)
                ProfileFormCard(
                    name = name,
                    onNameChange = { name = it },
                    email = email,
                    phone = phone,
                    onPhoneChange = { phone = it },
                    address = address
                )
                PasswordCard(onChangePassword = { navigator.navigateTo(Screen.ChangePassword) })

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
                onClick = { viewModel.updateProfile(name, email, phone, "") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EditBlue)
            ) {
                Text(strings.saveChanges, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { navigator.goBack() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA6D2F3), contentColor = EditBlue)
            ) {
                Text(strings.cancel, color = EditBlue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EditTopBar(navigator: AppNavigator) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navigator.goBack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EditBlue, modifier = Modifier.size(16.dp))
        }
        Text(strings.settingsTitle, fontSize = 20.sp, lineHeight = 28.sp, color = Color(0xFF181C23), fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text(strings.driverPortal, color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun AccountStatusCard(isVerified: Boolean) {
    val strings = LocalStrings.current
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
                    Text(strings.accountStatus, color = EditBlue, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (isVerified) strings.verifiedPartner else strings.verificationPending,
                        color = Color.Black,
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                if (isVerified) strings.verifiedProfileDesc else strings.pendingProfileDesc,
                color = EditBody,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ProfileFormCard(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        EditableInfoField(
            label = strings.fullName,
            value = name,
            onValueChange = onNameChange,
            icon = Icons.Filled.PersonOutline,
            keyboardType = KeyboardType.Text
        )
        InfoField(strings.emailAddress, email, Icons.Filled.Email, singleLine = true)
        EditableInfoField(
            label = strings.phoneNumber,
            value = phone,
            onValueChange = onPhoneChange,
            icon = Icons.Filled.Phone,
            keyboardType = KeyboardType.Phone
        )
        InfoField(strings.residentialAddress, address, Icons.Filled.LocationOn, singleLine = false)
    }
}

@Composable
private fun EditableInfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = EditBody, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(EditInputBg, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x33C1C6D7), RoundedCornerShape(12.dp)),
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = Color(0xFF8A96A9), modifier = Modifier.size(16.dp))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = EditBlue
            )
        )
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
private fun PasswordCard(onChangePassword: () -> Unit) {
    val strings = LocalStrings.current
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
                Text(strings.updatePassword, color = Color(0xFF181C23), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Last changed 4 months ago", color = EditBody, fontSize = 12.sp)
            }
        }
        Text(strings.changePassword, color = Color(0xFF0058BC), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onChangePassword() })
    }
}
