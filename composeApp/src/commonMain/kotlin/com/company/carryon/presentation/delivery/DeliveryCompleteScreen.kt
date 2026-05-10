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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.data.model.isSettlementEligible
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

private val DoneBlue = Color(0xFF5A86E8)
private val DoneSoft = Color(0xFFD8E4F6)
private val DoneCard = Color(0xFFD3DEEF)
private val DoneBg = Color(0xFFF7F8FC)
private val DoneText = Color(0xFF242A36)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryCompleteScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val jobState by viewModel.currentJob.collectAsState()
    val jobId = navigator.selectedJobId

    if (jobId == null) {
        ErrorState("No active job selected") { navigator.navigateAndClearStack(Screen.Home) }
        return
    }

    LaunchedEffect(jobId) {
        viewModel.loadJob(jobId)
    }

    val job = when (val state = jobState) {
        is UiState.Success -> state.data
        is UiState.Loading, UiState.Idle -> {
            LoadingScreen("Loading delivery summary...")
            return
        }
        is UiState.Error -> {
            ErrorState(state.message) { viewModel.loadJob(jobId) }
            return
        }
    }

    LaunchedEffect(job.status) {
        viewModel.redirectIfCurrentScreenInvalid(Screen.DeliveryComplete, job)
    }

    var activeSheet by remember { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current

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
            "Order #${job.id.takeLast(8).uppercase()} has been dropped off.",
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
                        Text(
                            if (job.isSettlementEligible) "RM ${job.estimatedEarnings.toInt()}" else "Pending",
                            color = DoneBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(job.id.takeLast(8).uppercase(), color = DoneBlue.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(title = "TIME", value = job.displayDurationMinutes.toString(), unit = "min", modifier = Modifier.weight(1f))
                    MetricCard(title = "DISTANCE", value = job.distance.toInt().toString(), unit = "km", modifier = Modifier.weight(1f))
                }
            }
        }

        Button(
            onClick = {
                navigator.clearPersistedDeliveryState()
                navigator.navigateAndClearStack(Screen.Home)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DoneBlue)
        ) {
            Text("Back to Homepage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {
                    navigator.clearPersistedDeliveryState()
                    navigator.initialJobsTabIndex = 0
                    navigator.navigateAndClearStack(Screen.Jobs)
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DoneBlue),
                border = androidx.compose.foundation.BorderStroke(2.dp, DoneBlue)
            ) {
                Text("Find Next Job", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = { navigator.navigateTo(Screen.JobReceipt) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DoneBlue),
                border = androidx.compose.foundation.BorderStroke(2.dp, DoneBlue)
            ) {
                Text("View Receipt", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
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
                .background(Color.White, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeliveryBottomTab("ROUTE", true, Icons.Filled.Route) { activeSheet = "ROUTE" }
            DeliveryBottomTab("EARNINGS", false, Icons.Filled.AccountBalanceWallet) { activeSheet = "EARNINGS" }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(DoneBlue, CircleShape)
                    .clickable {
                        uriHandler.openUri(
                            "https://www.google.com/maps/dir/?api=1&destination=${job.dropoff.latitude},${job.dropoff.longitude}&travelmode=driving"
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Navigation, contentDescription = "Navigate", tint = Color.White)
            }
            DeliveryBottomTab("INBOX", false, Icons.Filled.Inbox) {
                navigator.openCustomerChat(job.id, job.customerName.ifBlank { "Customer" })
            }
            DeliveryBottomTab("ACCOUNT", false, Icons.Filled.Person) { navigator.navigateTo(Screen.Profile) }
        }
    }

    if (activeSheet == "ROUTE") {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            DeliveryRouteSheet(
                pickupAddress = job.pickup.shortAddress.ifBlank { job.pickup.address },
                dropoffAddress = job.dropoff.shortAddress.ifBlank { job.dropoff.address },
                blue = DoneBlue, black = DoneText
            )
        }
    }
    if (activeSheet == "EARNINGS") {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            DeliveryEarningsSheet(earnings = job.estimatedEarnings, orderId = job.id, blue = DoneBlue, black = DoneText)
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
private fun DeliveryBottomTab(label: String, selected: Boolean, icon: ImageVector, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) DoneBlue else DoneText.copy(alpha = 0.45f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = if (selected) DoneBlue else DoneText.copy(alpha = 0.55f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DeliveryRouteSheet(pickupAddress: String, dropoffAddress: String, blue: Color, black: Color) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Delivery Route", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = black)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(10.dp).background(blue, CircleShape))
            Column {
                Text("PICKUP", fontSize = 10.sp, color = black.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                Text(pickupAddress.ifBlank { "--" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = black)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(10.dp).background(Color(0xFFE53935), CircleShape))
            Column {
                Text("DROP-OFF", fontSize = 10.sp, color = black.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                Text(dropoffAddress.ifBlank { "--" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = black)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DeliveryEarningsSheet(earnings: Double, orderId: String, blue: Color, black: Color) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Job Earnings", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = black)
        Text("RM ${earnings.toInt()}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = blue)
        Text("Order #${orderId.takeLast(8).uppercase()}", color = black.copy(alpha = 0.5f), fontSize = 13.sp)
        Text("Final amount confirmed after delivery completion.", fontSize = 12.sp, color = black.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))
    }
}
