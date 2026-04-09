package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val Blue = Color(0xFF2F80ED)
private val Bg = Color(0xFFF7F8FC)

@Composable
fun PersonalIdentityScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf(authViewModel.driverName.ifBlank { "" }) }
    var email by remember { mutableStateOf(authViewModel.driverEmail.ifBlank { "" }) }
    var phone by remember { mutableStateOf(authViewModel.driverPhone.ifBlank { "" }) }
    var dl by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color(0xFF6F7480))
            Text("Carry On", color = Blue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = Color(0xFF6F7480))
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Personal Identity", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF1E1F25))
        }

        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(206.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF3F4FB)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Profile Photo", fontWeight = FontWeight.SemiBold, color = Color(0xFF676C78), fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD0E3F8))
                        .drawBehind {
                            val strokeWidth = 3.dp.toPx()
                            drawCircle(
                                color = Blue.copy(alpha = 0.8f),
                                radius = (size.minDimension - strokeWidth) / 2f,
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ProfilePhotoIcon()
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Upload a clear front-facing\nphoto of yourself for\nverification.",
                    fontSize = 12.sp,
                    color = Color(0xFF636A7A),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        IdentityField("Full Name", name, { name = it }, "e.g. Marcus Aurelius")
        IdentityField("Email Address", email, { email = it }, "name@carryon.com", KeyboardType.Email)
        IdentityField("Phone Number", phone, { phone = it }, "+1 (555) 000-0000", KeyboardType.Phone)
        IdentityField("Driver's License Number", dl, { dl = it }, "DL-882910")
        IdentityField("Date of Birth", dob, { dob = it }, "mm/dd/yyyy")

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEAF2FE))
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = Blue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                "By proceeding, you agree to Carry On's\nbackground check policy and driver terms of\nservice. Your data is encrypted and handled\naccording to our high-security standards.",
                fontSize = 11.sp,
                color = Color(0xFF5E6574),
                lineHeight = 15.sp
            )
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                authViewModel.driverName = name
                authViewModel.driverEmail = email
                authViewModel.driverPhone = phone
                navigator.navigateTo(Screen.VehicleDetailsInput)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            enabled = name.isNotBlank() && email.isNotBlank()
        ) {
            Text("Save & Continue", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ProfilePhotoIcon() {
    Box(modifier = Modifier.size(74.dp), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Outlined.AddAPhoto,
            contentDescription = null,
            tint = Blue,
            modifier = Modifier.size(42.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(Blue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun IdentityField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF414755))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF8A8F9B)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFADB3C0),
                unfocusedBorderColor = Color(0xFFADB3C0),
                focusedContainerColor = Color(0xFFF7F8FC),
                unfocusedContainerColor = Color(0xFFF7F8FC)
            )
        )
        Spacer(Modifier.height(3.dp))
    }
}
