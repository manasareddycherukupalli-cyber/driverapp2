package com.company.carryon.presentation.delivery

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PinDrop
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

private val SDBlue = Color(0xFF2F80ED)
private val SDSoft = Color(0x4DA6D2F3)
private val SDWhite = Color(0xFFFFFFFF)
private val SDBlack = Color(0xFF000000)

@Composable
fun StartDeliveryScreen(navigator: AppNavigator) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SDWhite)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(34.dp).background(SDSoft, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = SDBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("RapidDrop", color = SDBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = SDBlack.copy(alpha = 0.6f))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepDot("1", false)
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(SDBlack.copy(alpha = 0.25f)))
            StepDot("2", false)
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(SDBlack.copy(alpha = 0.25f)))
            StepDot("3", true)
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(SDBlack.copy(alpha = 0.25f)))
            StepDot("4", false)
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(SDSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = SDBlue, modifier = Modifier.size(38.dp))
            }
        }

        Text("Ready to Deliver!", color = SDBlack, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Package collected from ", color = SDBlack.copy(alpha = 0.65f), fontSize = 14.sp)
            Text("Kedai Shahril", color = SDBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SDSoft)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DESTINATION", color = SDBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text("Ara Damansara, PJ", color = SDBlack, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = SDBlack.copy(alpha = 0.65f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Nurul Ain", color = SDBlack, fontSize = 14.sp)
                        }
                    }
                    Box(modifier = Modifier.size(30.dp).background(SDWhite, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PinDrop, contentDescription = null, tint = SDBlue, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("ETA", "~22", "min", Modifier.weight(1f))
            MetricCard("DISTANCE", "3.2", "km", Modifier.weight(1f))
        }

        Button(
            onClick = { navigator.navigateTo(Screen.InTransitNavigation) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SDBlue)
        ) {
            Text("Start Navigation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Map, contentDescription = null, tint = SDWhite, modifier = Modifier.size(16.dp))
        }

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SDWhite, contentColor = SDBlue)
        ) {
            Text("Report an Issue", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallTab("ROUTE", false)
            SmallTab("EARNINGS", false)
            SmallTab("ORDERS", true)
            SmallTab("WALLET", false)
            SmallTab("ACCOUNT", false)
        }
    }
}

@Composable
private fun StepDot(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .size(if (active) 38.dp else 28.dp)
            .background(SDBlue, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = SDWhite, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricCard(title: String, major: String, minor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SDWhite)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = SDBlack.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(major, color = SDBlue, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Spacer(Modifier.width(4.dp))
                Text(minor, color = SDBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SmallTab(label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (selected) SDSoft else SDWhite, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = if (selected) SDBlue else SDBlack.copy(alpha = 0.45f), modifier = Modifier.size(16.dp))
        }
        Text(label, color = if (selected) SDBlue else SDBlack.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
