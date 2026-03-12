package com.company.carryon.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.*
import com.company.carryon.di.ServiceLocator
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.theme.*
import kotlinx.coroutines.launch

/**
 * NotificationsScreen — Displays all driver notifications with read/unread state.
 */
private fun formatTimestamp(ts: String?): String {
    if (ts.isNullOrEmpty()) return ""
    return try {
        // Parse ISO-8601 timestamp e.g. "2026-03-11T12:00:00Z"
        val datePart = ts.substringBefore("T")
        val timePart = ts.substringAfter("T").substringBefore("Z").substringBefore("+").substringBefore("-")
        val parts = datePart.split("-")
        val day = parts.getOrNull(2)?.trimStart('0') ?: ""
        val month = parts.getOrNull(1)?.trimStart('0') ?: ""
        val time = timePart.substringBeforeLast(":") // HH:mm
        "$day/$month $time"
    } catch (_: Exception) {
        ts ?: ""
    }
}
@Composable
fun NotificationsScreen(navigator: AppNavigator) {
    val repository = remember { ServiceLocator.notificationsRepository }
    var notifications by remember { mutableStateOf<UiState<List<AppNotification>>>(UiState.Idle) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        notifications = UiState.Loading
        repository.getNotifications()
            .onSuccess { notifications = UiState.Success(it) }
            .onFailure { notifications = UiState.Error(it.message ?: "Failed to load") }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Notifications",
            onBackClick = { navigator.goBack() }
        )

        when (val state = notifications) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorState(state.message) {
                scope.launch {
                    notifications = UiState.Loading
                    repository.getNotifications()
                        .onSuccess { notifications = UiState.Success(it) }
                        .onFailure { notifications = UiState.Error(it.message ?: "Failed") }
                }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyState("No Notifications", "You're all caught up!", "🔔")
                } else {
                    // Mark all read button
                    TextButton(
                        onClick = {
                            scope.launch {
                                state.data.filter { !it.isRead }.forEach {
                                    repository.markAsRead(it.id)
                                }
                                repository.getNotifications()
                                    .onSuccess { notifications = UiState.Success(it) }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("Mark all read", color = Orange500, fontWeight = FontWeight.Medium)
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data) { notification ->
                            NotificationCard(notification) {
                                scope.launch {
                                    repository.markAsRead(notification.id)
                                    repository.getNotifications()
                                        .onSuccess { notifications = UiState.Success(it) }
                                }
                            }
                        }
                    }
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
private fun NotificationCard(notification: AppNotification, onTap: () -> Unit) {
    val (emoji, iconColor) = when (notification.type) {
        NotificationType.JOB_REQUEST -> "📦" to Orange500
        NotificationType.JOB_UPDATE -> "🔄" to Blue500
        NotificationType.PAYMENT -> "💰" to Green500
        NotificationType.PROMO -> "🎉" to Yellow500
        NotificationType.SYSTEM -> "⚙️" to Gray500
        NotificationType.ALERT -> "⚠️" to Red500
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.5.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Orange500)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(notification.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
