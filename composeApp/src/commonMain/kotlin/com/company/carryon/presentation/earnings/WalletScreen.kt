package com.company.carryon.presentation.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.*
import com.company.carryon.presentation.components.*
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.theme.*

/**
 * WalletScreen — Wallet balance, withdrawal, and full transaction history.
 */
@Composable
fun WalletScreen(navigator: AppNavigator) {
    val viewModel = remember { EarningsViewModel() }
    val walletState by viewModel.walletInfo.collectAsState()
    val transactionsState by viewModel.transactions.collectAsState()
    val withdrawalState by viewModel.withdrawalState.collectAsState()

    var showWithdrawDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DriveAppTopBar(
            title = "Wallet",
            onBackClick = { navigator.goBack() }
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wallet balance card
            item {
                when (val state = walletState) {
                    is UiState.Success -> WalletBalanceCard(
                        wallet = state.data,
                        onWithdraw = { showWithdrawDialog = true }
                    )
                    is UiState.Loading -> LoadingScreen()
                    else -> {}
                }
            }

            // Quick stats
            item {
                when (val state = walletState) {
                    is UiState.Success -> WalletStatsRow(state.data)
                    else -> {}
                }
            }

            // Transaction history
            item {
                Text(
                    text = "Transaction History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            when (val state = transactionsState) {
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item { EmptyState("No transactions yet", emoji = "💳") }
                    } else {
                        items(state.data) { TransactionItem(it) }
                    }
                }
                is UiState.Loading -> item { LoadingScreen() }
                else -> {}
            }
        }
    }

    // Withdraw dialog
    if (showWithdrawDialog) {
        WithdrawDialog(
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount ->
                viewModel.requestWithdrawal(amount)
                showWithdrawDialog = false
            },
            maxAmount = (walletState as? UiState.Success)?.data?.balance ?: 0.0
        )
    }
}

@Composable
private fun WalletBalanceCard(wallet: WalletInfo, onWithdraw: () -> Unit) {
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
                        colors = listOf(Green500, Color(0xFF2E7D32))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text("Available Balance", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "RM${wallet.balance.toInt()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )
                if (wallet.pendingAmount > 0) {
                    Text(
                        text = "RM${wallet.pendingAmount.toInt()} pending",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onWithdraw,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Green500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Withdraw to Bank", fontWeight = FontWeight.Bold)
                }
                if (wallet.bankAccountLinked) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "💳 Bank account ****${wallet.bankAccountLast4}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletStatsRow(wallet: WalletInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Lifetime",
            value = "RM ${(wallet.lifetimeEarnings / 1000).toInt()}K",
            icon = Icons.Filled.TrendingUp,
            iconTint = Green500,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Last Payout",
            value = "RM ${(wallet.lastPayout ?: 0.0).toInt()}",
            icon = Icons.Filled.Payment,
            iconTint = Blue500,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = transaction.description.ifBlank { transaction.type.displayName },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = transaction.timestamp?.take(10) ?: "-",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "RM${kotlin.math.abs(transaction.amount).toInt()}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount >= 0) Green500 else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun WithdrawDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    maxAmount: Double
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw Funds", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Available: RM${maxAmount.toInt()}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (RM)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("RM", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0 && (amount.toDoubleOrNull() ?: 0.0) <= maxAmount,
                colors = ButtonDefaults.buttonColors(containerColor = Green500)
            ) {
                Text("Withdraw")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
