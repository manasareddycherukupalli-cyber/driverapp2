package com.company.carryon.presentation.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.company.carryon.data.model.DeliveryJob
import com.company.carryon.data.model.displayDurationMinutes
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.theme.*
import kotlinx.coroutines.delay

/**
 * JobRequestPopup — Modal dialog for incoming job requests.
 * Shows job details with accept/reject buttons and a countdown timer.
 */
@Composable
fun JobRequestPopup(
    job: DeliveryJob,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    var timeRemaining by remember(job.id) { mutableStateOf(60) }

    // Countdown timer — auto-reject after 60 seconds
    LaunchedEffect(job.id) {
        timeRemaining = 60
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
        onReject()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with timer
                Text(text = "🚚", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "New Job Request!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))

                // Timer badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (timeRemaining <= 15) Red100 else Orange100
                ) {
                    Text(
                        text = "⏱ ${timeRemaining}s remaining",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = if (timeRemaining <= 15) Red500 else Orange600,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))

                // Pickup info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("📍", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pickup", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = job.pickup.shortAddress.ifBlank { job.pickup.address },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Dropoff info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🏁", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Drop-off", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = job.dropoff.shortAddress.ifBlank { job.dropoff.address },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Job stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💰", fontSize = 20.sp)
                        Text(
                            text = "RM${job.estimatedEarnings.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Green500
                        )
                        Text("Earnings", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📏", fontSize = 20.sp)
                        Text(
                            text = "${job.distance} km",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text("Distance", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏱", fontSize = 20.sp)
                        Text(
                            text = "${job.displayDurationMinutes} min",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text("Duration", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Accept / Reject buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500)
                    ) {
                        Text("Reject", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green500, contentColor = Color.White)
                    ) {
                        Text("Accept ✓", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
