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
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

@Composable
fun HelpCenterScreen(navigator: AppNavigator) {
    val strings = LocalStrings.current
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
            Text(strings.helpCenter, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2635), fontSize = 24.sp)
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
                    Text(strings.howCanWeHelp, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF8094C5)) },
                        placeholder = { Text(strings.searchPlaceholder, fontSize = 13.sp, color = Color(0xFF90A1C7)) },
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
                Text(strings.categories, fontWeight = FontWeight.Bold, color = Color(0xFF2C3852), fontSize = 18.sp)
                Text(strings.selectATopic, fontSize = 11.sp, color = Color(0xFF9BA7C0), fontWeight = FontWeight.SemiBold)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HelpCategoryCard(
                    title = strings.gettingStarted,
                    subtitle = strings.gettingStartedDesc,
                    icon = Icons.Filled.DirectionsCar,
                    modifier = Modifier.weight(1f),
                    onClick = { navigator.navigateTo(Screen.HelpGettingStarted) }
                )
                HelpCategoryCard(
                    title = strings.payments,
                    subtitle = strings.paymentsDesc,
                    icon = Icons.Filled.AttachMoney,
                    modifier = Modifier.weight(1f),
                    onClick = { navigator.navigateTo(Screen.HelpPayments) }
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HelpCategoryCard(
                    title = strings.safety,
                    subtitle = strings.safetyDesc,
                    icon = Icons.Filled.Security,
                    modifier = Modifier.weight(1f),
                    onClick = { navigator.navigateTo(Screen.HelpSafety) }
                )
                HelpCategoryCard(
                    title = strings.appIssues,
                    subtitle = strings.appIssuesDesc,
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    modifier = Modifier.weight(1f),
                    onClick = { navigator.navigateTo(Screen.HelpAppIssues) }
                )
            }

            Text(strings.topQuestions, fontWeight = FontWeight.Bold, color = Color(0xFF2C3852), fontSize = 18.sp)
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
                    Text(strings.stillNeedHelp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3852), fontSize = 18.sp)
                    Text(
                        strings.logisticSupportAvailable,
                        color = Color(0xFF7B88A2),
                        fontSize = 13.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { navigator.navigateTo(Screen.RaiseTicket) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6))
                    ) {
                        Text(strings.contactSupport, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(strings.averageWaitTime, fontSize = 10.sp, color = Color(0xFF93A1BB))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1FF))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.supportResources, color = Color(0xFF6E7991), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ResourceRow(strings.driverHandbook) { navigator.navigateTo(Screen.HelpDriverHandbook) }
                    ResourceRow(strings.videoTutorials) { }
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3))
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4B79E6), modifier = Modifier.size(16.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF2C3852), lineHeight = 20.sp)
            Text(subtitle, color = Color(0xFF6E7991), fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun HelpTopicScreen(
    navigator: AppNavigator,
    title: String,
    summary: String,
    tips: List<String>
) {
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
            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2635), fontSize = 24.sp)
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
                colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Overview", color = Color(0xFF4B79E6), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(summary, color = Color(0xFF2C3852), fontSize = 16.sp, lineHeight = 22.sp)
                }
            }

            Text("Top Tips", fontWeight = FontWeight.Bold, color = Color(0xFF2C3852), fontSize = 18.sp)
            tips.forEach { tip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("•", color = Color(0xFF4B79E6), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(tip, color = Color(0xFF4A556F), fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }

            Button(
                onClick = { navigator.navigateTo(Screen.HelpContactSupport) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact Support", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun ContactSupportScreen(navigator: AppNavigator) {
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
            Text("Contact Support", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2635), fontSize = 24.sp)
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
                colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("We are here to help", color = Color(0xFF4B79E6), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Choose the quickest way to reach support. For urgent incidents, use SOS immediately.",
                        color = Color(0xFF2C3852),
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Email", color = Color(0xFF6E7991), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("support@carryon.com", color = Color(0xFF2C3852), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Phone", color = Color(0xFF6E7991), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("+60 3-5555 0101", color = Color(0xFF2C3852), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Average wait time: 3-5 minutes", color = Color(0xFF7B88A2), fontSize = 12.sp)
                }
            }

            Button(
                onClick = { navigator.navigateTo(Screen.RaiseTicket) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Raise Ticket", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
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
                Text(text, color = Color(0xFF4A556F), fontSize = 14.sp)
            }
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = Color(0xFFBDC7D8), modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun ResourceRow(label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(Icons.Filled.Description, contentDescription = null, tint = Color(0xFF7FA1E7), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color(0xFF4B79E6), fontSize = 13.sp)
    }
}

@Composable
fun DriverHandbookScreen(navigator: AppNavigator) {
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
            Text("Driver Handbook", fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2635), fontSize = 24.sp)
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
                colors = CardDefaults.cardColors(containerColor = Color(0x33A6D2F3))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Overview", color = Color(0xFF4B79E6), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "This handbook covers delivery standards, compliance, and safety practices for daily operations.",
                        color = Color(0xFF2C3852),
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            Text("Sections", fontWeight = FontWeight.Bold, color = Color(0xFF2C3852), fontSize = 18.sp)
            HandbookSectionCard(
                title = "1. Account & Compliance",
                description = "Profile verification, documents, and policy requirements.",
                onClick = { navigator.navigateTo(Screen.HelpHandbookAccountCompliance) }
            )
            HandbookSectionCard(
                title = "2. Delivery Workflow",
                description = "Pickup, in-transit, drop-off, and proof-of-delivery steps.",
                onClick = { navigator.navigateTo(Screen.HelpHandbookDeliveryWorkflow) }
            )
            HandbookSectionCard(
                title = "3. Earnings & Payouts",
                description = "How earnings are calculated and withdrawal timelines.",
                onClick = { navigator.navigateTo(Screen.HelpHandbookEarningsPayouts) }
            )
            HandbookSectionCard(
                title = "4. Safety & Incident Response",
                description = "Emergency protocol, escalation, and support channels.",
                onClick = { navigator.navigateTo(Screen.HelpHandbookSafetyIncident) }
            )

            Button(
                onClick = { navigator.navigateTo(Screen.HelpContactSupport) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact Support", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HandbookSectionCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Color(0xFF2C3852), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(description, color = Color(0xFF6E7991), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun HandbookDetailScreen(
    navigator: AppNavigator,
    title: String,
    points: List<String>
) {
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
            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2635), fontSize = 24.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            points.forEachIndexed { index, point ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("${index + 1}.", color = Color(0xFF4B79E6), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(point, color = Color(0xFF4A556F), fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }

            Button(
                onClick = { navigator.navigateTo(Screen.HelpContactSupport) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B79E6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact Support", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
