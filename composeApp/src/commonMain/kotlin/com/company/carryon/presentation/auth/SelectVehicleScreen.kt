package com.company.carryon.presentation.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val Blue = Color(0xFF2F80ED)
private val Bg = Color(0xFFF9F9FF)
private val Soft = Color(0x33A6D2F3)

@Composable
fun SelectVehicleScreen(navigator: AppNavigator) {
    var selected by remember { mutableStateOf("53' Dry Van") }

    Column(modifier = Modifier.fillMaxSize().background(Bg)) {
        DriveAppTopBar(
            title = "Select Vehicle",
            onBackClick = { navigator.goBack() },
            leadingIcon = Icons.Filled.Menu,
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
            showTitle = false
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Profile Completion", fontSize = 12.sp, color = Color(0xFF414755))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFA6D2F3), RoundedCornerShape(99.dp))) {
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(6.dp).background(Blue, RoundedCornerShape(99.dp)))
            }

            Spacer(Modifier.height(6.dp))
            Text("Select your vehicle", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text("Choose the primary vehicle type you will be operating to receive relevant load matches.", color = Color(0xFF414755), fontSize = 14.sp)

            VehicleChoice("53' Dry Van", "Standard enclosed trailer for general freight cargo.", selected == "53' Dry Van") { selected = "53' Dry Van" }
            VehicleChoice("Reefer", "Temperature-controlled trailers for perishable goods.", selected == "Reefer") { selected = "Reefer" }
            VehicleChoice("Flatbed", "Open trailers for oversized or irregular industrial equipment.", selected == "Flatbed") { selected = "Flatbed" }
            VehicleChoice("Step Deck", "Multi-level open trailers for tall cargo height clearance.", selected == "Step Deck") { selected = "Step Deck" }

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Soft), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "You can add multiple vehicle configurations later in your driver profile settings.",
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFF414755),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { navigator.navigateTo(Screen.ReadyToDrive) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Continue") }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun VehicleChoice(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Blue else Color(0x00FFFFFF))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(28.dp).background(Color(0xFFA6D2F3), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Blue, modifier = Modifier.size(15.dp))
                }
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text(subtitle, fontSize = 13.sp, color = Color(0xFF414755))
        }
    }
}
