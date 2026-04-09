package com.company.carryon.presentation.jobs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.ErrorState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.jobs_profile_avatar
import org.jetbrains.compose.resources.painterResource

private val JobsBlue = Color(0xFF2F80ED)
private val JobsBlueLight = Color(0xFF97CBF1)
private val JobsBg = Color(0xFFF4F5F8)
private val JobsText = Color(0xFF202124)
private val JobsMuted = Color(0xDE000000)
private val JobsDivider = Color(0x1A000000)

@Composable
fun JobsListScreen(navigator: AppNavigator) {
    val viewModel = remember { JobsViewModel() }
    val activeJobs by viewModel.activeJobs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JobsBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color(0xFF6F7480), modifier = Modifier.size(26.dp))
            Text("Carry On", color = JobsBlue, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = Color(0xFF6F7480), modifier = Modifier.size(24.dp))
        }

        Text(
            "Jobs",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = JobsText,
            modifier = Modifier.padding(start = 24.dp, bottom = 14.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(topStart = 27.dp, topEnd = 27.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            when (activeJobs) {
                is UiState.Loading, UiState.Idle -> LoadingScreen()
                is UiState.Error -> ErrorState((activeJobs as UiState.Error).message)
                is UiState.Success -> {
                    val jobs = (activeJobs as UiState.Success<List<DeliveryJob>>).data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item { Spacer(Modifier.height(8.dp)) }
                        items(jobs.take(3)) { job ->
                            JobTruckItem(
                                job = job,
                                onOpen = {
                                    navigator.selectedJobId = job.id
                                    navigator.navigateTo(Screen.JobDetails)
                                }
                            )
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobTruckItem(job: DeliveryJob, onOpen: () -> Unit) {
    val driverName = job.customerName.ifBlank { "Name" }
    val taskName = job.packageType.ifBlank { "Chemical Delivery" }
    val departed = job.scheduledAt?.takeIf { it.isNotBlank() } ?: "20 Feb, 05:00 PM"
    val location = job.pickup.address.ifBlank { "123 Main Street, Anytown, IND 845103" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Text(
            "These are the available truck",
            fontSize = 12.7.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6E6E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.jobs_profile_avatar),
                        contentDescription = null,
                        modifier = Modifier.size(68.dp)
                    )
                }
                Text(driverName, fontSize = 19.5.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                JobDetailLine("Task", taskName)
                JobDetailLine("Departed", departed)
                JobDetailLine("Current Location", location)
                JobDetailLine("Trip Cost", "Rs ${job.estimatedEarnings.toInt().coerceAtLeast(10000)}")
            }
        }

        HorizontalDivider(
            color = JobsDivider,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpen() }
            ) {
                Icon(Icons.Outlined.Call, contentDescription = null, tint = JobsBlue, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(10.dp))
                Text("Get in Contact", fontSize = 13.65.sp, fontWeight = FontWeight.Medium, color = JobsBlue)
            }
            Text(
                "Decline",
                fontSize = 13.65.sp,
                fontWeight = FontWeight.Medium,
                color = JobsBlueLight,
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
private fun JobDetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.75.sp,
            fontWeight = FontWeight.Medium,
            color = JobsMuted,
            modifier = Modifier.width(86.dp)
        )
        Text(
            text = value,
            fontSize = if (label == "Current Location") 9.5.sp else 9.75.sp,
            color = Color.Black,
            maxLines = if (label == "Current Location") 2 else 1
        )
    }
}
