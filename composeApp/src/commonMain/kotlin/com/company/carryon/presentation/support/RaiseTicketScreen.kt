package com.company.carryon.presentation.support

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.SupportIssueOption
import com.company.carryon.data.model.UiState
import com.company.carryon.di.ServiceLocator
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.components.PrimaryButton
import com.company.carryon.presentation.navigation.AppNavigator

@Composable
fun RaiseTicketScreen(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val viewModel = remember { SupportViewModel() }
    val jobRepository = remember { ServiceLocator.jobRepository }
    val createState by viewModel.createTicketState.collectAsState()
    val optionsState by viewModel.intakeOptions.collectAsState()

    var selectedGroup by remember { mutableStateOf<SupportIssueOption?>(null) }
    var selectedIssue by remember { mutableStateOf<SupportIssueOption?>(null) }
    var selectedJob by remember { mutableStateOf<DeliveryJob?>(null) }
    var jobs by remember { mutableStateOf<List<DeliveryJob>>(emptyList()) }
    var showJobPicker by remember { mutableStateOf(false) }
    var promptedJobIssueId by remember { mutableStateOf<String?>(null) }
    var jobSearch by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    LaunchedEffect(createState) {
        if (createState is UiState.Success) navigator.goBack()
    }

    LaunchedEffect(Unit) {
        val loadedJobs = buildList {
            jobRepository.getActiveJobs().onSuccess { addAll(it) }
            jobRepository.getScheduledJobs().onSuccess { addAll(it) }
            jobRepository.getCompletedJobs().onSuccess { addAll(it) }
        }
        jobs = loadedJobs.distinctBy { it.id }
    }

    LaunchedEffect(selectedIssue?.id, jobs.size) {
        val issue = selectedIssue
        if (
            issue?.requiresBooking == true &&
            selectedJob == null &&
            promptedJobIssueId != issue.id &&
            jobs.isNotEmpty()
        ) {
            promptedJobIssueId = issue.id
            showJobPicker = true
        }
    }

    if (showJobPicker) {
        JobPickerDialog(
            jobs = jobs,
            query = jobSearch,
            onQueryChange = { jobSearch = it },
            onDismiss = { showJobPicker = false },
            onSelect = {
                selectedJob = it
                showJobPicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = strings.raiseATicket,
            onBackClick = { navigator.goBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(strings.howCanWeHelpQuestion, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Select the issue. The support team will receive a structured ticket.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            when (val state = optionsState) {
                UiState.Loading, UiState.Idle -> CircularProgressIndicator()
                is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                is UiState.Success -> {
                    if (selectedGroup == null) {
                        state.data.forEach { group ->
                            SupportOptionRow(group.label) {
                                selectedGroup = group
                                selectedIssue = null
                                selectedJob = null
                                details = ""
                            }
                        }
                    } else if (selectedIssue == null) {
                        SelectedOptionRow(selectedGroup!!.label) { selectedGroup = null }
                        selectedGroup!!.children.forEach { issue ->
                            SupportOptionRow(issue.label) {
                                selectedIssue = issue
                                selectedJob = null
                                promptedJobIssueId = null
                                details = ""
                            }
                        }
                    } else {
                        SelectedOptionRow(selectedGroup!!.label) {
                            selectedGroup = null
                            selectedIssue = null
                            selectedJob = null
                        }
                        SelectedOptionRow(selectedIssue!!.label) {
                            selectedIssue = null
                            selectedJob = null
                        }
                        if (selectedIssue!!.requiresBooking) {
                            JobSelectorCard(
                                job = selectedJob,
                                hasJobs = jobs.isNotEmpty(),
                                onClick = { showJobPicker = true }
                            )
                        }
                        OutlinedTextField(
                            value = details,
                            onValueChange = { details = it },
                            label = { Text(if (selectedIssue!!.requiresDetails) strings.describeYourIssue else "Add optional details") },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 6
                        )
                        PrimaryButton(
                            text = strings.submitTicket,
                            onClick = {
                                val issue = selectedIssue ?: return@PrimaryButton
                                viewModel.createIntakeTicket(issue, selectedJob?.id, details.trim(), listOfNotNull(selectedGroup?.label, issue.label))
                            },
                            enabled = createState !is UiState.Loading &&
                                (!selectedIssue!!.requiresDetails || details.trim().length >= 3) &&
                                (!selectedIssue!!.requiresBooking || selectedJob != null),
                            isLoading = createState is UiState.Loading
                        )
                    }
                }
            }

            if (createState is UiState.Error) {
                Text((createState as UiState.Error).message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun JobPickerDialog(
    jobs: List<DeliveryJob>,
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelect: (DeliveryJob) -> Unit
) {
    val normalized = query.trim().lowercase()
    val filtered = remember(jobs, normalized) {
        if (normalized.isBlank()) {
            jobs
        } else {
            jobs
                .map { job ->
                    val haystack = listOf(
                        job.id,
                        job.displayOrderId,
                        job.pickup.address,
                        job.pickup.shortAddress,
                        job.dropoff.address,
                        job.dropoff.shortAddress,
                        job.status.name
                    ).joinToString(" ").lowercase()
                    val score = when {
                        job.displayOrderId.lowercase().startsWith(normalized) -> 0
                        haystack.contains(normalized) -> 1
                        else -> 2
                    }
                    score to job
                }
                .filter { it.first < 2 }
                .sortedBy { it.first }
                .map { it.second }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Select your job/order", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search order ID or location") }
                )
                if (filtered.isEmpty()) {
                    Text("No matching jobs found.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered.take(20)) { job ->
                            JobPickRow(job = job, onClick = { onSelect(job) })
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun JobSelectorCard(job: DeliveryJob?, hasJobs: Boolean, onClick: () -> Unit) {
    val accent = if (job == null) MaterialTheme.colorScheme.primary else Color(0xFF10B981)
    val title = if (job == null) "Required: select your job/order" else "Selected job/order"
    val actionLabel = when {
        !hasJobs -> "No recent jobs found"
        job == null -> "Tap to select job"
        else -> "Change job"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(1.5.dp, accent, RoundedCornerShape(14.dp))
            .clickable(enabled = hasJobs) { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(accent, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        if (job == null) {
            Text(
                if (hasJobs) "Choose the related job before submitting this support ticket." else "We could not find a recent job on this account.",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        } else {
            JobSummary(job)
        }
        Surface(
            color = if (job == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            shape = RoundedCornerShape(999.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    actionLabel,
                    color = if (job == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (hasJobs) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("›", color = if (job == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun JobPickRow(job: DeliveryJob, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        JobSummary(job)
    }
}

@Composable
private fun JobSummary(job: DeliveryJob) {
    val orderLabel = job.displayOrderId.takeIf { it.isNotBlank() } ?: "#${job.id.takeLast(8).uppercase()}"
    Text(orderLabel, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    Text("From: ${job.pickup.shortAddress.ifBlank { job.pickup.address }}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    Text("To: ${job.dropoff.shortAddress.ifBlank { job.dropoff.address }}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    Text(job.status.name.replace("_", " "), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun SupportOptionRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SelectedOptionRow(label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(label, color = MaterialTheme.colorScheme.onPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
