package com.company.carryon.presentation.jobs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.JobStatus
import com.company.carryon.data.model.LocationInfo
import com.company.carryon.data.model.PackageSize
import com.company.carryon.data.model.UiState
import com.company.carryon.presentation.components.LoadingScreen
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import drive_app.composeapp.generated.resources.Res
import drive_app.composeapp.generated.resources.job_dispatch_section
import org.jetbrains.compose.resources.painterResource

private val DispatchBlue = Color(0xFF4D7EE7)
private val DispatchBg = Color(0xFFF6F7FB)
private val CardBg = Color(0xFFF3F5FA)
private val TextDark = Color(0xFF222631)
private val TextMuted = Color(0xFF717A8D)

@Composable
fun JobDetailsScreen(navigator: AppNavigator) {
    val viewModel = remember { JobsViewModel() }
    val jobDetailsState by viewModel.jobDetails.collectAsState()
    val jobId = navigator.selectedJobId

    LaunchedEffect(jobId) {
        jobId?.let { viewModel.loadJobDetails(it) }
    }

    when (val state = jobDetailsState) {
        is UiState.Loading, UiState.Idle -> LoadingScreen()
        is UiState.Error -> JobDetailsContent(
            job = fallbackDispatchJob(jobId),
            navigator = navigator,
            onUpdateStatus = {}
        )
        is UiState.Success -> JobDetailsContent(
            job = state.data,
            navigator = navigator,
            onUpdateStatus = { status -> viewModel.updateJobStatus(state.data.id, status) }
        )
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
            .background(DispatchBg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DispatchTopBar(onBack = { navigator.goBack() })
            DispatchMapHero()
            OrderSummaryCard(job)
            RouteCard(job)
            CustomerCard(job)
            PackageCard(job)
            StartDeliveryButton(job, navigator, onUpdateStatus)
            ReportIssueButton()
            Spacer(Modifier.height(6.dp))
        }
        DispatchBottomBar()
    }
}

@Composable
private fun DispatchTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DispatchBlue,
                modifier = Modifier.size(18.dp).clickable { onBack() }
            )
            Spacer(Modifier.width(8.dp))
            Text("Dispatch", color = DispatchBlue, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("JOB DETAILS", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = DispatchBlue, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun DispatchMapHero() {
    Card(
        modifier = Modifier.fillMaxWidth().height(232.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12202E))
    ) {
        Image(
            painter = painterResource(Res.drawable.job_dispatch_section),
            contentDescription = "Estimated time section",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun OrderSummaryCard(job: DeliveryJob) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ORDER ID", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("#${job.id}", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("POTENTIAL EARNINGS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("RM ${job.estimatedEarnings.toInt()}", color = DispatchBlue, fontSize = 37.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun RouteCard(job: DeliveryJob) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RouteStop(
                label = "PICKUP",
                title = job.pickup.shortAddress.ifBlank { job.pickup.address },
                subtitle = job.pickup.address,
                highlight = DispatchBlue
            )
            RouteStop(
                label = "DROP-OFF",
                title = job.dropoff.shortAddress.ifBlank { job.dropoff.address },
                subtitle = job.dropoff.address,
                highlight = DispatchBlue,
                isLast = true
            )
        }
    }
}

@Composable
private fun RouteStop(
    label: String,
    title: String,
    subtitle: String,
    highlight: Color,
    isLast: Boolean = false
) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(highlight.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(highlight))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(34.dp).background(highlight.copy(alpha = 0.35f)))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(title, color = DispatchBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = TextDark, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CustomerCard(job: DeliveryJob) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFD5E5FF)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = DispatchBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("CUSTOMER", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text(job.customerName, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("★ 4.9", color = DispatchBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircleActionIcon(Icons.Filled.Phone)
                CircleActionIcon(Icons.Filled.ChatBubbleOutline)
            }
        }
    }
}

@Composable
private fun CircleActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFD5E5FF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = DispatchBlue, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PackageCard(job: DeliveryJob) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PACKAGE DETAILS", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFD5E5FF)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = DispatchBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(job.packageType, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TinyChip(text = job.packageSize.displayName.uppercase())
                        TinyChip(text = job.notes?.takeIf { it.isNotBlank() }?.uppercase() ?: "FRAGILE")
                    }
                }
            }
        }
    }
}

@Composable
private fun TinyChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFC9DCFA))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, color = DispatchBlue, fontWeight = FontWeight.Bold, fontSize = 8.sp)
    }
}

@Composable
private fun StartDeliveryButton(
    job: DeliveryJob,
    navigator: AppNavigator,
    onUpdateStatus: (JobStatus) -> Unit
) {
    val label = when (job.status) {
        JobStatus.ACCEPTED -> "Start Delivery"
        JobStatus.HEADING_TO_PICKUP, JobStatus.ARRIVED_AT_PICKUP, JobStatus.PICKED_UP, JobStatus.IN_TRANSIT -> "Continue Delivery"
        JobStatus.ARRIVED_AT_DROP -> "Complete Delivery"
        else -> "Start Delivery"
    }

    Button(
        onClick = {
            navigator.selectedJobId = job.id
            when (job.status) {
                JobStatus.ACCEPTED -> {
                    onUpdateStatus(JobStatus.HEADING_TO_PICKUP)
                    navigator.navigateTo(Screen.MapNavigation)
                }
                JobStatus.HEADING_TO_PICKUP, JobStatus.ARRIVED_AT_PICKUP, JobStatus.PICKED_UP, JobStatus.IN_TRANSIT -> {
                    navigator.navigateTo(Screen.MapNavigation)
                }
                JobStatus.ARRIVED_AT_DROP -> navigator.navigateTo(Screen.ProofOfDelivery)
                else -> navigator.navigateTo(Screen.MapNavigation)
            }
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DispatchBlue)
    ) {
        Icon(Icons.Filled.MailOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReportIssueButton() {
    OutlinedButton(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DispatchBlue),
        border = androidx.compose.foundation.BorderStroke(2.dp, DispatchBlue)
    ) {
        Text("Report Issue", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DispatchBottomBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem("Deliveries", Icons.Filled.LocalShipping, selected = false)
        BottomItem("Earnings", Icons.Filled.AccountCircle, selected = true)
        BottomItem("Inbox", Icons.Filled.MailOutline, selected = false)
        BottomItem("Account", Icons.Filled.Person, selected = false)
    }
}

@Composable
private fun BottomItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) Color(0xFFE4E8FF) else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(icon, contentDescription = label, tint = if (selected) DispatchBlue else TextMuted, modifier = Modifier.size(16.dp))
        }
        Text(label, color = if (selected) DispatchBlue else TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
    }
}

private fun fallbackDispatchJob(jobId: String?): DeliveryJob {
    return DeliveryJob(
        id = if (jobId.isNullOrBlank()) "CR-4872" else jobId,
        status = JobStatus.ACCEPTED,
        pickup = LocationInfo(
            address = "Jalan Bukit Bintang, Kuala Lumpur, 55100",
            shortAddress = "Jalan Bukit Bintang"
        ),
        dropoff = LocationInfo(
            address = "Ara Damansara, Petaling Jaya, 47301",
            shortAddress = "Ara Damansara"
        ),
        customerName = "Nurul Ain",
        customerPhone = "+60 12-555 8493",
        packageType = "Parcel",
        packageSize = PackageSize.SMALL,
        estimatedEarnings = 68.0,
        distance = 1.4,
        estimatedDuration = 24,
        notes = "Fragile"
    )
}
