package com.company.carryon.presentation.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.util.telUriFor

private val DropBlue = Color(0xFF034094)
private val DropSoft = Color(0x4DA6D2F3)
private val DropWhite = Color(0xFFFFFFFF)
private val DropBlack = Color(0xFF000000)

@Composable
fun ArrivedAtDropScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val jobState by viewModel.currentJob.collectAsState()
    val jobId = navigator.selectedJobId

    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJob(it) }
    }

    if (jobId == null) {
        ErrorState("No active job selected") { navigator.goBack() }
        return
    }

    val job = when (val state = jobState) {
        is UiState.Success -> state.data
        is UiState.Loading, UiState.Idle -> {
            LoadingScreen("Loading delivery details...")
            return
        }
        is UiState.Error -> {
            ErrorState(state.message) { viewModel.loadJob(jobId) }
            return
        }
    }

    LaunchedEffect(job.status) {
        viewModel.redirectIfCurrentScreenInvalid(Screen.ArrivedAtDrop, job)
    }

    ArrivedAtDropContent(
        job = job,
        onBack = { navigator.goBack() },
        onProceed = { navigator.navigateTo(Screen.ProofOfDelivery) }
    )
}

@Composable
private fun ArrivedAtDropContent(
    job: DeliveryJob,
    onBack: () -> Unit,
    onProceed: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val recipientName = job.dropoff.contactName?.takeIf { it.isNotBlank() }
        ?: job.customerName.takeIf { it.isNotBlank() }
        ?: "--"
    val recipientPhone = job.dropoff.contactPhone?.takeIf { it.isNotBlank() }
        ?: job.customerPhone.takeIf { it.isNotBlank() }
        ?: "--"
    val recipientTelUri = telUriFor(recipientPhone)
    val primaryAddress = job.dropoff.shortAddress.ifBlank { job.dropoff.address }.ifBlank { "--" }
    val secondaryAddress = when {
        job.dropoff.address.isBlank() -> ""
        job.dropoff.shortAddress.isBlank() -> ""
        job.dropoff.address.equals(job.dropoff.shortAddress, ignoreCase = true) -> ""
        else -> job.dropoff.address
    }
    val instructions = job.dropoff.instructions?.takeIf { it.isNotBlank() }
        ?: job.notes?.takeIf { it.isNotBlank() }
        ?: "No special instructions provided."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DropWhite)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DropBlue,
                    modifier = Modifier.clickable { onBack() }
                )
                Spacer(Modifier.width(10.dp))
                Text("Arrived at Drop", color = DropBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = DropBlue)
                Box(modifier = Modifier.size(26.dp).background(DropSoft, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = DropBlue, modifier = Modifier.size(16.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("STEP 4 OF 4", color = DropBlue.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("ARRIVAL CONFIRMED", color = DropBlue.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(DropSoft, RoundedCornerShape(99.dp))) {
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(DropBlue, RoundedCornerShape(99.dp)))
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(DropSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = DropBlue, modifier = Modifier.size(30.dp))
            }
        }

        Text("Arrived at Destination!", color = DropBlack, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        Text("Please confirm the recipient's details below", color = DropBlack.copy(alpha = 0.65f), fontSize = 14.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DropWhite)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("RECIPIENT", color = DropBlue.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(recipientName, color = DropBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(recipientPhone, color = DropBlack.copy(alpha = 0.65f), fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(DropBlue, CircleShape)
                            .clickable(enabled = recipientTelUri != null) {
                                recipientTelUri?.let(uriHandler::openUri)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null, tint = DropWhite)
                    }
                }

                HorizontalDivider(color = DropBlack.copy(alpha = 0.08f))

                Column {
                    Text("DELIVERY ADDRESS", color = DropBlue.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DropBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(primaryAddress, color = DropBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (secondaryAddress.isNotBlank()) {
                                Text(secondaryAddress, color = DropBlack.copy(alpha = 0.65f), fontSize = 14.sp)
                            }
                        }
                    }
                }

                val instructionsShape = RoundedCornerShape(10.dp)
                Card(
                    shape = instructionsShape,
                    colors = CardDefaults.cardColors(containerColor = DropWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = instructionsShape,
                            ambientColor = Color.Black.copy(alpha = 0.35f),
                            spotColor = Color.Black.copy(alpha = 0.35f)
                        )
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = DropBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("INSTRUCTIONS", color = DropBlue.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(instructions, color = DropBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        val actionButtonShape = RoundedCornerShape(20.dp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { recipientTelUri?.let(uriHandler::openUri) },
                enabled = recipientTelUri != null,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = actionButtonShape,
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    ),
                shape = actionButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = DropWhite, contentColor = DropBlue)
            ) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = DropBlue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Call", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = actionButtonShape,
                        ambientColor = Color.Black.copy(alpha = 0.35f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    ),
                shape = actionButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = DropWhite, contentColor = DropBlue)
            ) {
                Text("Support", fontWeight = FontWeight.SemiBold)
            }
        }

        Button(
            onClick = onProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DropBlue)
        ) {
            Text("Proceed to Delivery Proof", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        }

    }
}
