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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val Blue = Color(0xFF2F80ED)
private val Bg = Color(0xFFF9F9FF)
private val Soft = Color(0xFFA6D2F3)

@Composable
fun DocumentUploadScreen(navigator: AppNavigator, viewModel: AuthViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.clickable { navigator.goBack() })
            Text("Carry On", color = Blue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(Icons.Filled.NotificationsNone, contentDescription = null)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("STEP 3 OF 3: VEHICLE DETAILS", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Soft, RoundedCornerShape(99.dp))) {
                Box(modifier = Modifier.fillMaxWidth(1f).height(4.dp).background(Blue, RoundedCornerShape(99.dp)))
            }

            Text("Identity Verification", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text("To ensure a secure environment for all users, please provide a clear photo of your official government ID or Driver's License.", color = Color(0xFF414755), fontSize = 14.sp)

            Text("DRIVER'S LICENSE", fontSize = 11.sp, color = Color(0xFF414755), fontWeight = FontWeight.SemiBold)
            UploadIdCard("Upload")
            Text("ID CARD (BACK)", fontSize = 11.sp, color = Color(0xFF414755), fontWeight = FontWeight.SemiBold)
            UploadIdCard("Required")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Verification Checklist", fontWeight = FontWeight.Bold)
                    Checklist("No Flash/Glare")
                    Checklist("Full Corners")
                }
            }

            Button(
                onClick = { navigator.navigateTo(Screen.SelectVehicle) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Verify Identity") }

            Text(
                "By clicking verify, you consent to automatic processing under terms of service. Verification usually takes 2-5 minutes.",
                color = Color(0x99414755),
                fontSize = 11.sp
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun UploadIdCard(tag: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(158.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6E8F3)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(44.dp).background(Color.White, RoundedCornerShape(999.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Blue)
            }
            Spacer(Modifier.height(8.dp))
            Text("Choose Image or Capture", fontWeight = FontWeight.SemiBold)
            Text("Support: JPG, PNG, up to 5MB", color = Color(0xFF64748B), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Text(tag, color = Blue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Checklist(item: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Blue, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(8.dp))
        Text(item, fontSize = 13.sp, color = Color(0xFF414755))
    }
}
