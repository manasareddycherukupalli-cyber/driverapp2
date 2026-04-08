package com.company.carryon.presentation.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.*
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*

/**
 * JobsListScreen — Tabbed list of Active, Scheduled, and Completed jobs.
 */
@Composable
fun JobsListScreen(navigator: AppNavigator) {
    val viewModel = remember { JobsViewModel() }
    val activeJobs by viewModel.activeJobs.collectAsState()
    val scheduledJobs by viewModel.scheduledJobs.collectAsState()
    val completedJobs by viewModel.completedJobs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(navigator.initialJobsTabIndex.coerceIn(0, 2)) }
    LaunchedEffect(Unit) {
        navigator.initialJobsTabIndex = 0
    }
    val tabs = listOf("Active", "Scheduled", "Completed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "My Jobs",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Tab row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Orange500,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Orange500
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == index) Orange500 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Tab content
        when (selectedTab) {
            0 -> JobsList(activeJobs, navigator, "No active jobs", "Go online to receive job requests", "📋")
            1 -> JobsList(scheduledJobs, navigator, "No scheduled jobs", "Scheduled jobs will appear here", "📅")
            2 -> JobsList(completedJobs, navigator, "No completed jobs", "Your delivery history will appear here", "✅")
        }
    }
}

@Composable
private fun JobsList(
    state: UiState<List<DeliveryJob>>,
    navigator: AppNavigator,
    emptyTitle: String,
    emptySubtitle: String,
    emptyEmoji: String
) {
    when (state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorState(state.message)
        is UiState.Success -> {
            if (state.data.isEmpty()) {
                EmptyState(title = emptyTitle, subtitle = emptySubtitle, emoji = emptyEmoji)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { job ->
                        JobListCard(
                            job = job,
                            onClick = {
                                navigator.selectedJobId = job.id
                                navigator.navigateTo(Screen.JobDetails)
                            }
                        )
                    }
                }
            }
        }
        is UiState.Idle -> LoadingScreen()
    }
}

@Composable
private fun JobListCard(job: DeliveryJob, onClick: () -> Unit) {
    val statusColor = when (job.status) {
        JobStatus.DELIVERED -> Green500
        JobStatus.CANCELLED -> Red500
        JobStatus.PENDING -> Yellow500
        else -> Orange500
    }
    val statusBg = when (job.status) {
        JobStatus.DELIVERED -> Green100
        JobStatus.CANCELLED -> Red100
        JobStatus.PENDING -> Yellow100
        else -> Orange100
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: ID + Status + Earnings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "#${job.id}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    StatusBadge(
                        text = job.status.displayName,
                        color = statusColor,
                        backgroundColor = statusBg
                    )
                }
                Text(
                    text = "RM${job.estimatedEarnings.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Orange500
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // Pickup
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = job.pickup.shortAddress.ifBlank { job.pickup.address },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(6.dp))

            // Dropoff
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏁", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = job.dropoff.shortAddress.ifBlank { job.dropoff.address },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Bottom row: package info + distance + duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("📦 ${job.packageSize.displayName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("📏 ${job.distance} km", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("⏱ ${job.displayDurationMinutes} min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
