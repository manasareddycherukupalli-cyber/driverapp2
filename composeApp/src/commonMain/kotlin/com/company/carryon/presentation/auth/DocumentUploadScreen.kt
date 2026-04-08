package com.company.carryon.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val VerifyBlue = Color(0xFF4D7EE7)
private val VerifyBg = Color(0xFFF6F7FB)
private val TextPrimary = Color(0xFF21242B)
private val TextMuted = Color(0xFF656E7E)

@Composable
fun DocumentUploadScreen(navigator: AppNavigator, viewModel: AuthViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerifyBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color(0xFF6F7480))
            androidx.compose.material3.Text("Carry On", color = VerifyBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = Color(0xFF6F7480))
        }

        Spacer(Modifier.height(26.dp))
        androidx.compose.material3.Text("IdentityVerification", fontSize = 39.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.Text(
            "To ensure a secure environment for all\ndrivers, please provide a clear photo of your\nofficial government ID or Driver's License.",
            color = TextMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text("DRIVER'S LICENSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(12.dp))
                androidx.compose.material3.Text("Uploaded", color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FD))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0DFE4)),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(62.dp)
                                .height(126.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFCCC5C0))
                        )
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(126.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFBDC1C8))
                        )
                        Box(
                            modifier = Modifier
                                .width(62.dp)
                                .height(126.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFC9C3BE))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color(0xFF7C808A), modifier = Modifier.size(13.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(12.dp))
                    androidx.compose.material3.Text("Replace document", color = VerifyBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2FF))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text("ID CARD (BACK)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    androidx.compose.material3.Text("Required", fontSize = 11.sp, color = Color(0xFF7D8597), fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE6EFFC))
                        .border(1.dp, VerifyBlue.copy(alpha = 0.7f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(6.dp))
                        androidx.compose.material3.Text("Choose Image or Capture", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        androidx.compose.material3.Text("Supports JPG, PNG, up to 10MB", color = TextMuted, fontSize = 10.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFC8DAF7))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(12.dp))
                        androidx.compose.material3.Text("Capture in a well-lit area without glare", color = Color(0xFF516079), fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        androidx.compose.material3.Text("Verification Checklist", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        ChecklistRow(
            title = "No Flash/Glare",
            desc = "Text must be legible and not obscured by light."
        )
        Spacer(Modifier.height(10.dp))
        ChecklistRow(
            title = "Full Corners",
            desc = "Ensure all four corners of the card are visible."
        )

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { navigator.navigateTo(Screen.SelectVehicle) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerifyBlue)
        ) {
            androidx.compose.material3.Text("Verify Identity", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.Text(
            "By clicking verify, you consent to our biometric\nprocessing policy and terms of service. Verification\nusually takes 2-5 minutes.",
            color = Color(0xFF818796),
            fontSize = 10.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ChecklistRow(title: String, desc: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFD7E5FB)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Verified, contentDescription = null, tint = VerifyBlue, modifier = Modifier.size(13.dp))
        }
        Column {
            androidx.compose.material3.Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            androidx.compose.material3.Text(desc, color = Color(0xFF7B8394), fontSize = 10.sp)
        }
    }
}
