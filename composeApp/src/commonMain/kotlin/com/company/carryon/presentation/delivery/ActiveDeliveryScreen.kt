package com.company.carryon.presentation.delivery

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val ArriveBlue = Color(0xFF2F80ED)
private val ArriveSoft = Color(0x4DA6D2F3)
private val ArriveWhite = Color(0xFFFFFFFF)
private val ArriveBlack = Color(0xFF000000)

@Composable
fun ActiveDeliveryScreen(navigator: AppNavigator) {
    val checks = remember { mutableStateListOf(true, false, false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArriveWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ArriveBlue, modifier = Modifier.clickable { navigator.goBack() })
                Spacer(Modifier.width(10.dp))
                Text("Arrived at Pickup", color = ArriveBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Icon(Icons.Filled.MoreVert, contentDescription = null, tint = ArriveBlack.copy(alpha = 0.6f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StepLabel("ACCEPTED", false)
                StepLabel("ARRIVED", true)
                StepLabel("IN-TRANSIT", false)
                StepLabel("COMPLETED", false)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(modifier = Modifier.weight(1f).height(4.dp).background(ArriveBlue, RoundedCornerShape(99.dp)))
                Box(modifier = Modifier.weight(1f).height(4.dp).background(ArriveBlue.copy(alpha = 0.5f), RoundedCornerShape(99.dp)))
                Box(modifier = Modifier.weight(1f).height(4.dp).background(ArriveSoft, RoundedCornerShape(99.dp)))
                Box(modifier = Modifier.weight(1f).height(4.dp).background(ArriveSoft, RoundedCornerShape(99.dp)))
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(ArriveSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(ArriveBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = ArriveWhite)
                    }
                }
            }

            Text("You've Arrived!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ArriveBlack)
            Text("At Kedai Shahril, Jalan Bukit Bintang", color = ArriveBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArriveSoft)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("ORDER ID", fontSize = 10.sp, color = ArriveBlack.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                            Text("#CR-4872", fontSize = 22.sp, color = ArriveBlack, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("TYPE", fontSize = 10.sp, color = ArriveBlack.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                            Text("Parcel (~2 kg)", fontSize = 18.sp, color = ArriveBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("SENDER", fontSize = 10.sp, color = ArriveBlack.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                            Text("Shahril Bin Ahmad", fontSize = 16.sp, color = ArriveBlack, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .background(ArriveWhite, RoundedCornerShape(999.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Route, contentDescription = null, tint = ArriveBlue, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Contact", color = ArriveBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Text("VERIFICATION CHECKLIST", color = ArriveBlack.copy(alpha = 0.65f), fontWeight = FontWeight.Bold, fontSize = 12.sp)

            ChecklistRow("Package sealed securely", checks[0]) { checks[0] = !checks[0] }
            ChecklistRow("Label information correct", checks[1]) { checks[1] = !checks[1] }
            ChecklistRow("Fragile sticker attached (If applicable)", checks[2]) { checks[2] = !checks[2] }

            Button(
                onClick = { navigator.navigateTo(Screen.PickupInstructions) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArriveBlue)
            ) {
                Text("Package Picked Up", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ArriveWhite, modifier = Modifier.size(16.dp))
            }

            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ArriveWhite)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomMiniTab("Route", false)
            BottomMiniTab("Tasks", true)
            BottomMiniTab("Earnings", false)
            BottomMiniTab("Profile", false)
        }
    }
}

@Composable
private fun StepLabel(text: String, active: Boolean) {
    Text(
        text,
        color = if (active) ArriveBlack else ArriveBlack.copy(alpha = 0.55f),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ChecklistRow(text: String, checked: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArriveWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = checked,
                onClick = onToggle,
                colors = RadioButtonDefaults.colors(
                    selectedColor = ArriveBlue,
                    unselectedColor = ArriveBlack.copy(alpha = 0.45f)
                )
            )
            Spacer(Modifier.width(6.dp))
            Text(text, color = ArriveBlack, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun BottomMiniTab(label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(if (selected) ArriveSoft else ArriveWhite, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = if (selected) ArriveBlue else ArriveBlack.copy(alpha = 0.55f), modifier = Modifier.size(16.dp))
        }
        Text(label, color = if (selected) ArriveBlue else ArriveBlack.copy(alpha = 0.55f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
