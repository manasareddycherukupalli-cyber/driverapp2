package com.company.carryon.presentation.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.*
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*

/**
 * JobDetailsScreen — Full details of a selected delivery job.
 * Shows pickup/dropoff, customer info, package details, and action buttons.
 */
@Composable
fun JobDetailsScreen(navigator: AppNavigator) {
    val viewModel = remember { JobsViewModel() }
    val jobDetailsState by viewModel.jobDetails.collectAsState()
    val jobId = navigator.selectedJobId

    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJobDetails(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Job Details",
            onBackClick = { navigator.goBack() }
        )

        when (val state = jobDetailsState) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorState(state.message) { jobId?.let { viewModel.loadJobDetails(it) } }
            is UiState.Success -> JobDetailsContent(
                job = state.data,
                navigator = navigator,
                onUpdateStatus = { status -> viewModel.updateJobStatus(state.data.id, status) }
            )
            is UiState.Idle -> LoadingScreen()
        }
    }
}

@Composable
private fun JobDetailsContent(
    job: DeliveryJob,
    navigator: AppNavigator,
    onUpdateStatus: (JobStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status and earnings header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Orange100)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    StatusBadge(
                        text = job.status.displayName,
                        color = Orange600,
                        backgroundColor = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Job #${job.id}",
                        fontSize = 13.sp,
                        color = Orange700
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RM${job.estimatedEarnings.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Orange600
                    )
                    Text(
                        text = "Estimated Earnings",
                        fontSize = 12.sp,
                        color = Orange700
                    )
                }
            }
        }

        // Route info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Route", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))

                // Pickup
                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📍", fontSize = 18.sp)
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(Gray300)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PICKUP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Green500)
                        Text(job.pickup.address, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        job.pickup.contactName?.let {
                            Text("$it • ${job.pickup.contactPhone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        job.pickup.instructions?.let {
                            Text("📝 $it", fontSize = 12.sp, color = Blue500)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Dropoff
                Row(verticalAlignment = Alignment.Top) {
                    Text("🏁", fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DROP-OFF", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Red500)
                        Text(job.dropoff.address, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        job.dropoff.contactName?.let {
                            Text("$it • ${job.dropoff.contactPhone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        job.dropoff.instructions?.let {
                            Text("📝 $it", fontSize = 12.sp, color = Blue500)
                        }
                    }
                }
            }
        }

        // Package details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Package Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                InfoRow(Icons.Filled.Inventory2, "Package Type", job.packageType)
                InfoRow(Icons.Filled.FitScreen, "Package Size", job.packageSize.displayName)
                InfoRow(Icons.Filled.Route, "Distance", "${job.distance} km")
                InfoRow(Icons.Filled.Schedule, "Est. Duration", "${job.displayDurationMinutes} min")
                job.notes?.let {
                    InfoRow(Icons.Filled.Notes, "Notes", it)
                }
            }
        }

        // Customer info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Customer", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                InfoRow(Icons.Filled.Person, "Name", job.customerName)
                InfoRow(Icons.Filled.Phone, "Phone", job.customerPhone)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Action buttons based on status
        when (job.status) {
            JobStatus.ACCEPTED -> {
                PrimaryButton(
                    text = "Navigate to Pickup 🗺️",
                    onClick = {
                        navigator.selectedJobId = job.id
                        onUpdateStatus(JobStatus.HEADING_TO_PICKUP)
                        navigator.navigateTo(Screen.ActiveDelivery)
                    }
                )
            }
            JobStatus.HEADING_TO_PICKUP, JobStatus.ARRIVED_AT_PICKUP, JobStatus.PICKED_UP, JobStatus.IN_TRANSIT -> {
                PrimaryButton(
                    text = "Continue Delivery →",
                    onClick = {
                        navigator.selectedJobId = job.id
                        navigator.navigateTo(Screen.ActiveDelivery)
                    }
                )
            }
            JobStatus.ARRIVED_AT_DROP -> {
                PrimaryButton(
                    text = "Complete Delivery ✓",
                    onClick = {
                        navigator.selectedJobId = job.id
                        navigator.navigateTo(Screen.ProofOfDelivery)
                    }
                )
            }
            else -> {}
        }

        Spacer(Modifier.height(16.dp))
    }
}
