package com.company.carryon.presentation.support

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

@Composable
fun HelpCenterScreen(navigator: AppNavigator) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigator.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF4B79E6))
            }
            Text("Help Center", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2635))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4B79E6))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("How can we help\nyou today?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF8094C5)) },
                        placeholder = { Text("Search for articles, guides, or keywords.", fontSize = 12.sp, color = Color(0xFF90A1C7)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White
                        )
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Categories", fontWeight = FontWeight.Bold, color = Color(0xFF2C3852))
                Text("SELECT A TOPIC", fontSize = 10.sp, color = Color(0xFF9BA7C0), fontWeight = FontWeight.SemiBold)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HelpCategoryCard("Getting\nStarted", "Account setup and first steps for new drivers.", Icons.Filled.DirectionsCar, Modifier.weight(1f))
                HelpCategoryCard("Payments", "Direct deposit, earnings, and tax reimbursement.", Icons.Filled.AttachMoney, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HelpCategoryCard("Safety", "Incident reporting and safety guidelines.", Icons.Filled.Security, Modifier.weight(1f))
                HelpCategoryCard("App Issues", "Technical glitches and app navigation help.", Icons.AutoMirrored.Filled.HelpOutline, Modifier.weight(1f))
            }

            Text("Top Questions", fontWeight = FontWeight.Bold, color = Color(0xFF2C3852))
            QuestionRow("How do I update my vehicle registration?")
            QuestionRow("What to do if a customer is not available for delivery?")
            QuestionRow("Understanding the weekly pay summary.")
            QuestionRow("How to request a route change?")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F3FA))
            ) {
                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFDDE5F6), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Emergency, contentDescription = null, tint = Color(0xFF4B79E6))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Still need help?", fontWeight = FontWeight.Bold, color = Color(0xFF2C3852))
                    Text(
                        "Our logistic support team is available 24/7 to assist with active route issues or account inquiries.",
                        color = Color(0xFF7B88A2),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { navigator.navigateTo(Screen.RaiseTicket) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6))
                    ) {
                        Text("Contact Support", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("AVERAGE WAIT TIME: 1 MIN", fontSize = 10.sp, color = Color(0xFF93A1BB))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Support Resources", color = Color(0xFF6E7991), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    ResourceRow("Driver Handbook PDF")
                    ResourceRow("Video Tutorials")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HelpCategoryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF))
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4B79E6), modifier = Modifier.size(16.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF2C3852), lineHeight = 16.sp)
            Text(subtitle, color = Color(0xFF6E7991), fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun QuestionRow(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = Color(0xFFA6B2C8), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(text, color = Color(0xFF4A556F), fontSize = 12.sp)
            }
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = Color(0xFFBDC7D8), modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun ResourceRow(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Description, contentDescription = null, tint = Color(0xFF7FA1E7), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color(0xFF4B79E6), fontSize = 12.sp)
    }
}
