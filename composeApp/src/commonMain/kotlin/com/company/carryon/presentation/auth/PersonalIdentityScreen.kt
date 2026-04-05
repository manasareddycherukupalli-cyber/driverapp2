package com.company.carryon.presentation.auth

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val Blue = Color(0xFF2F80ED)
private val Bg = Color(0xFFF9F9FF)

@Composable
fun PersonalIdentityScreen(navigator: AppNavigator, authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf(authViewModel.driverName.ifBlank { "" }) }
    var email by remember { mutableStateOf(authViewModel.driverEmail.ifBlank { "" }) }
    var phone by remember { mutableStateOf(authViewModel.driverPhone.ifBlank { "" }) }
    var dl by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(Bg).padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.clickable { navigator.goBack() })
            Text("Carry On", color = Blue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.NotificationsNone, contentDescription = null)
        }

        Spacer(Modifier.height(10.dp))
        Text("Step 1 of 3: Personal Identity", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text("STEP 1 OF 3", color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(width = 24.dp, height = 6.dp).clip(RoundedCornerShape(99.dp)).background(Blue))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFA6D2F3)))
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFA6D2F3)))
        }
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xFFA6D2F3))) {
            Box(modifier = Modifier.fillMaxWidth(0.33f).height(3.dp).clip(RoundedCornerShape(99.dp)).background(Blue))
        }

        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Profile Photo", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.size(128.dp).clip(CircleShape).background(Color(0xFFA6D2F3)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Blue, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Upload a clear front-facing photo of yourself for verification.", fontSize = 12.sp, color = Color(0xFF414755))
            }
        }

        Spacer(Modifier.height(14.dp))
        IdentityField("Full Name", name, { name = it }, "e.g. Marcus Aurelius")
        IdentityField("Email Address", email, { email = it }, "name@carryon.com", KeyboardType.Email)
        IdentityField("Phone Number", phone, { phone = it }, "+1 (555) 000-0000", KeyboardType.Phone)
        IdentityField("Driver's License Number", dl, { dl = it }, "DL-882910")
        IdentityField("Date of Birth", dob, { dob = it }, "mm/dd/yyyy")

        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x33A6D2F3)).padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = Blue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(8.dp))
            Text("By proceeding, you agree to Carry On's background check policy and driver terms of service.", fontSize = 11.sp, color = Color(0xFF414755))
        }

        Spacer(Modifier.height(14.dp))
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
        ) { Text("Save & Continue") }
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
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(2.dp))
    }
}
