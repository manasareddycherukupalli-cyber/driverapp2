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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val DoneBlue = Color(0xFF5A86E8)
private val DoneSoft = Color(0xFFD8E4F6)
private val DoneCard = Color(0xFFD3DEEF)
private val DoneBg = Color(0xFFF7F8FC)
private val DoneText = Color(0xFF242A36)

@Composable
fun DeliveryCompleteScreen(navigator: AppNavigator) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DoneBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Menu, contentDescription = null, tint = DoneBlue)
            Text("Delivery Complete", color = DoneBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = DoneBlue)
        }

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = DoneBlue, modifier = Modifier.size(42.dp))
            }
        }

        Text(
            "Delivery Complete!",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = DoneText,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Text(
            "Order #CD-92841 has been dropped off.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = DoneText.copy(alpha = 0.8f),
            fontSize = 14.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DoneCard)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text("EARNINGS", color = DoneBlue.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text("RM 68.00", color = DoneBlue, fontWeight = FontWeight.ExtraBold, fontSize = 38.sp)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("CD-92841", color = DoneBlue.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(title = "TIME", value = "22", unit = "min", modifier = Modifier.weight(1f))
                    MetricCard(title = "DISTANCE", value = "3.2", unit = "km", modifier = Modifier.weight(1f))
                }
            }
        }

        Button(
            onClick = {
                navigator.initialJobsTabIndex = 0
                navigator.navigateAndClearStack(Screen.Jobs)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DoneBlue)
        ) {
            Text("Find Next Job", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DoneBlue),
            border = androidx.compose.foundation.BorderStroke(2.dp, DoneBlue)
        ) {
            Text("View Receipt", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF3EA))
        ) {}

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniBottom("ROUTE", false)
            MiniBottom("EARNINGS", false)
            MiniBottom("RATINGS", false)
            MiniBottom("ROUTE", true)
            MiniBottom("ACCOUNT", false)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DoneSoft)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = DoneBlue.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = DoneText, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                Text(" $unit", color = DoneText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun MiniBottom(label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(if (selected) DoneSoft else Color.Transparent, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = if (selected) DoneBlue else DoneText.copy(alpha = 0.45f), modifier = Modifier.size(14.dp))
        }
        Text(label, color = if (selected) DoneBlue else DoneText.copy(alpha = 0.45f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}
