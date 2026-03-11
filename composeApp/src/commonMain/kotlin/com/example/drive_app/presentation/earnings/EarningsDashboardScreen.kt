package com.example.drive_app.presentation.earnings

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drive_app.data.model.*
import com.example.drive_app.presentation.components.*
import com.example.drive_app.presentation.navigation.AppNavigator
import com.example.drive_app.presentation.navigation.Screen
import com.example.drive_app.presentation.theme.*

/**
 * EarningsDashboardScreen — Overview of earnings with period toggle,
 * summary cards, and recent transactions list.
 */
@Composable
fun EarningsDashboardScreen(navigator: AppNavigator) {
    val viewModel = remember { EarningsViewModel() }
    val earningsState by viewModel.earningsSummary.collectAsState()
    val transactionsState by viewModel.transactions.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "Earnings",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EarningsPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { viewModel.setSelectedPeriod(period) },
                            label = { Text(period.displayName) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Orange500,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Main earnings card
            item {
                when (val state = earningsState) {
                    is UiState.Success -> EarningsHeaderCard(state.data, selectedPeriod)
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Orange500)
                        }
                    }
                    else -> {}
                }
            }

            // Stats grid
            item {
                when (val state = earningsState) {
                    is UiState.Success -> EarningsStatsGrid(state.data)
                    else -> {}
                }
            }

            // Wallet card
            item {
                WalletQuickCard(onClick = { navigator.navigateTo(Screen.Wallet) })
            }

            // Recent transactions header
            item {
                SectionHeader(
                    title = "Recent Transactions",
                    action = "See All",
                    onActionClick = { navigator.navigateTo(Screen.Wallet) }
                )
            }

            // Transaction list
            when (val state = transactionsState) {
                is UiState.Success -> {
                    items(state.data.take(5)) { transaction ->
                        TransactionItem(transaction)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun EarningsHeaderCard(earnings: EarningsSummary, period: EarningsPeriod) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Orange500, Orange600)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "${period.displayName}'s Earnings",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                val amount = when (period) {
                    EarningsPeriod.TODAY -> earnings.todayEarnings
                    EarningsPeriod.THIS_WEEK -> earnings.weeklyEarnings
                    EarningsPeriod.THIS_MONTH -> earnings.monthlyEarnings
                }
                Text(
                    text = "RM${amount.toInt()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column {
                        Text("${earnings.todayDeliveries}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Deliveries", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Column {
                        Text("RM${earnings.tipEarnings.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Tips", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Column {
                        Text("RM${earnings.bonusEarnings.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Bonus", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsStatsGrid(earnings: EarningsSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Deliveries",
            value = "${earnings.totalDeliveries}",
            icon = Icons.Filled.LocalShipping,
            iconTint = Blue500,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Online Hours",
            value = "${earnings.onlineHours}h",
            icon = Icons.Filled.Schedule,
            iconTint = Green500,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Bonus",
            value = "RM${earnings.bonusEarnings.toInt()}",
            icon = Icons.Filled.Star,
            iconTint = Yellow500,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WalletQuickCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Green100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = Green500, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Wallet", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Tap to manage withdrawals", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Green500)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val (icon, color) = when (transaction.type) {
        TransactionType.DELIVERY_EARNING -> "🚚" to Green500
        TransactionType.BONUS -> "⭐" to Yellow500
        TransactionType.TIP -> "💰" to Blue500
        TransactionType.WITHDRAWAL -> "🏧" to Red500
        TransactionType.INCENTIVE -> "🎯" to Orange500
        TransactionType.ADJUSTMENT -> "⚙️" to Gray500
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = transaction.type.displayName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${if (transaction.amount >= 0) "+" else ""}RM${transaction.amount.toInt()}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transaction.amount >= 0) Green500 else Red500
            )
        }
    }
}
