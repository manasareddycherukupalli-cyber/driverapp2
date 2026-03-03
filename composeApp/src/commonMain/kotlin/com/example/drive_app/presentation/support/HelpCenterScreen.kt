package com.example.drive_app.presentation.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.data.model.*
import com.example.drive_app.presentation.components.*
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*

/**
 * HelpCenterScreen — FAQs, help articles, and quick access to support.
 */
@Composable
fun HelpCenterScreen(navigator: AppNavigator) {
    val viewModel = remember { SupportViewModel() }
    val articlesState by viewModel.helpArticles.collectAsState()
    val ticketsState by viewModel.tickets.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Help Center",
            onBackClick = { navigator.goBack() }
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search for help...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Orange500) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Quick actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHelpCard("📝", "Raise Ticket", Modifier.weight(1f)) {
                        navigator.navigateTo(Screen.RaiseTicket)
                    }
                    QuickHelpCard("🆘", "Emergency", Modifier.weight(1f)) {
                        navigator.navigateTo(Screen.Sos)
                    }
                }
            }

            // My tickets
            item {
                SectionHeader(
                    title = "My Tickets",
                    action = "View All"
                )
            }

            when (val state = ticketsState) {
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item {
                            Text(
                                "No tickets yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        items(state.data) { ticket ->
                            TicketCard(ticket) {
                                navigator.selectedTicketId = ticket.id
                                navigator.navigateTo(Screen.SupportChat)
                            }
                        }
                    }
                }
                else -> {}
            }

            // FAQ articles
            item {
                SectionHeader(title = "Frequently Asked Questions")
            }

            when (val state = articlesState) {
                is UiState.Success -> {
                    val filtered = if (searchQuery.isBlank()) state.data
                    else state.data.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
                    }
                    items(filtered) { article ->
                        HelpArticleCard(article)
                    }
                }
                is UiState.Loading -> item { LoadingScreen() }
                else -> {}
            }
        }
    }
}

@Composable
private fun QuickHelpCard(emoji: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun TicketCard(ticket: SupportTicket, onClick: () -> Unit) {
    val statusColor = when (ticket.status) {
        TicketStatus.OPEN -> Blue500
        TicketStatus.IN_PROGRESS -> Orange500
        TicketStatus.RESOLVED -> Green500
        TicketStatus.CLOSED -> Gray500
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ticket.subject, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text("#${ticket.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusBadge(
                text = ticket.status.displayName,
                color = statusColor,
                backgroundColor = statusColor.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun HelpArticleCard(article: HelpArticle) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = article.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Category: ${article.category}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
