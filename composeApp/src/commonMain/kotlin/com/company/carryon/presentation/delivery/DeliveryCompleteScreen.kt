package com.company.carryon.presentation.delivery

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.UiState
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.data.model.isSettlementEligible
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen

@Composable
fun DeliveryCompleteScreen(navigator: AppNavigator, viewModel: DeliveryViewModel) {
    val jobState by viewModel.currentJob.collectAsState()
    val jobId = navigator.selectedJobId
    val colors = MaterialTheme.colorScheme

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DriveAppTopBar(
            title = "Delivery Complete",
            onBackClick = { navigator.goBack() },
            onNotificationClick = { navigator.navigateTo(Screen.Notifications) },
            onProfileClick = { navigator.navigateTo(Screen.Profile) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(colors.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(42.dp))
            }
        }

        Text(
            "Delivery Complete!",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Text(
            "Order #${job.id.takeLast(8).uppercase()} has been dropped off.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = colors.onBackground.copy(alpha = 0.75f),
            fontSize = 14.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text("EARNINGS", color = colors.primary.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text(
                            if (job.isSettlementEligible) "RM ${job.estimatedEarnings.toInt()}" else "Pending",
                            color = colors.primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(colors.surface, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(job.id.takeLast(8).uppercase(), color = colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
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
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surface,
                    contentColor = colors.primary
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, colors.primary)
            ) {
                Text("Find Next Job", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = { navigator.navigateTo(Screen.JobReceipt) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surface,
                    contentColor = colors.primary
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, colors.primary)
            ) {
                Text("View Receipt", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
        ) {}
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val metricShape = RoundedCornerShape(12.dp)
    Card(
        modifier = modifier.shadow(
            elevation = 12.dp,
            shape = metricShape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.35f),
            spotColor = Color.Black.copy(alpha = 0.35f)
        ),
        shape = metricShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = colors.primary.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                Text(" $unit", color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
