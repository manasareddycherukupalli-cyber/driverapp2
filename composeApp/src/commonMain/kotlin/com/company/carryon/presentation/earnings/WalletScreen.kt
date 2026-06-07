package com.company.carryon.presentation.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.*
import com.company.carryon.presentation.components.*
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.navigation.AppNavigator
import com.company.carryon.presentation.navigation.Screen
import com.company.carryon.presentation.theme.*
import kotlin.math.round

private val WalletBlue = Color(0xFF034094)
private val WalletBlueDark = Color(0xFF1A6ED4)
private val WalletBg = Color(0xFFF9F9FF)
private val TransactionCardBg = Color(0xFFA6D2F3).copy(alpha = 0.2f)

/**
 * WalletScreen — Wallet balance, withdrawal, and full transaction history.
 */
@Composable
fun WalletScreen(navigator: AppNavigator) {
    val strings = LocalStrings.current
    val viewModel = remember { EarningsViewModel() }
    val walletState by viewModel.walletInfo.collectAsState()
    val transactionsState by viewModel.transactions.collectAsState()
    val withdrawalState by viewModel.withdrawalState.collectAsState()
    val payoutStatus by viewModel.payoutStatus.collectAsState()
    val onboardingLink by viewModel.onboardingLink.collectAsState()
    val uriHandler = LocalUriHandler.current

    var showWithdrawDialog by remember { mutableStateOf(false) }

    LaunchedEffect(onboardingLink) {
        val link = (onboardingLink as? UiState.Success)?.data?.url
        if (!link.isNullOrBlank()) {
            uriHandler.openUri(link)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WalletBg)
    ) {
        DriveAppTopBar(
            title = strings.wallet,
            onBackClick = { navigator.goBack() }
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wallet balance card
            item {
                when (val state = walletState) {
                    is UiState.Success -> WalletBalanceCard(
                        wallet = state.data,
                        payoutStatus = (payoutStatus as? UiState.Success)?.data,
                        onWithdraw = { showWithdrawDialog = true },
                        onSetupPayouts = { viewModel.createPayoutOnboardingLink() }
                    )
                    is UiState.Loading -> LoadingScreen()
                    else -> {}
                }
            }

            item {
                PayoutMessage(
                    payoutStatus = payoutStatus,
                    withdrawalState = withdrawalState,
                    onboardingLink = onboardingLink
                )
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
                    text = strings.transactionHistory,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            when (val state = transactionsState) {
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item { EmptyState(strings.noTransactionsYet, emoji = "💳") }
                    } else {
                        items(state.data) { transaction ->
                            TransactionItem(transaction) {
                                navigator.selectedTransactionId = transaction.id
                                navigator.navigateTo(Screen.TransactionDetail)
                            }
                        }
                    }
                }
                is UiState.Loading -> item { LoadingScreen() }
                else -> {}
            }
        }
    }

    // Withdraw dialog
    if (showWithdrawDialog) {
        val status = (payoutStatus as? UiState.Success)?.data
        WithdrawDialog(
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount ->
                viewModel.requestWithdrawal(amount)
                showWithdrawDialog = false
            },
            maxAmount = (walletState as? UiState.Success)?.data?.balance ?: 0.0,
            minimumAmount = status?.minimumWithdrawalAmount ?: 50.0,
            feeFlat = status?.withdrawalFeeFlat ?: 0.0,
            feeRate = status?.withdrawalFeeRate ?: 0.0
        )
    }
}

@Composable
private fun WalletBalanceCard(
    wallet: WalletInfo,
    payoutStatus: PayoutStatus?,
    onWithdraw: () -> Unit,
    onSetupPayouts: () -> Unit
) {
    val strings = LocalStrings.current
    val payoutsEnabled = payoutStatus?.payoutsEnabled ?: wallet.stripePayoutsEnabled
    val hasAccount = payoutStatus?.accountId != null || wallet.stripeAccountId != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(WalletBlue, WalletBlueDark)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(strings.availableBalance, color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "RM${wallet.balance.toInt()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    onClick = if (payoutsEnabled) onWithdraw else onSetupPayouts,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = WalletBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (payoutsEnabled) strings.withdrawToBank else if (hasAccount) "Continue payout setup" else "Set up payouts",
                        fontWeight = FontWeight.Bold
                    )
                }
                if (payoutsEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Stripe payouts enabled. Withdrawals are manual.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                } else if (hasAccount) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Stripe needs a few more payout details",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PayoutMessage(
    payoutStatus: UiState<PayoutStatus>,
    withdrawalState: UiState<Transaction>,
    onboardingLink: UiState<PayoutOnboardingLink>
) {
    val message = when (withdrawalState) {
        is UiState.Success -> "Withdrawal submitted. Check transaction history for the transfer record."
        is UiState.Error -> withdrawalState.message
        else -> when (payoutStatus) {
            is UiState.Success -> {
                val requirements = payoutStatus.data.requirements
                when {
                    payoutStatus.data.payoutsEnabled -> "Minimum withdrawal: RM ${formatWalletMoney(payoutStatus.data.minimumWithdrawalAmount)}"
                    requirements?.pastDue?.isNotEmpty() == true -> "Stripe needs urgent payout details: ${requirements.pastDue.take(2).joinToString()}"
                    requirements?.currentlyDue?.isNotEmpty() == true -> "Stripe needs payout details: ${requirements.currentlyDue.take(2).joinToString()}"
                    payoutStatus.data.accountId != null -> "Continue Stripe setup to enable withdrawals and online driving."
                    else -> "Set up Stripe payouts before going online or withdrawing earnings."
                }
            }
            is UiState.Error -> payoutStatus.message
            else -> null
        }
    } ?: when (onboardingLink) {
        is UiState.Error -> onboardingLink.message
        else -> null
    }

    if (message.isNullOrBlank()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WalletStatsRow(wallet: WalletInfo) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WalletStatCard(
            title = strings.lifetimeLabel,
            value = "RM ${(wallet.lifetimeEarnings / 1000).toInt()}K",
            icon = Icons.Filled.TrendingUp,
            iconTint = WalletBlue,
            modifier = Modifier.weight(1f)
        )
        WalletStatCard(
            title = strings.lastPayout,
            value = "RM ${(wallet.lastPayout ?: 0.0).toInt()}",
            icon = Icons.Filled.Payment,
            iconTint = WalletBlueDark,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TransactionCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                color = if (transaction.amount >= 0) WalletBlue else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun WalletStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WithdrawDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    maxAmount: Double,
    minimumAmount: Double,
    feeFlat: Double,
    feeRate: Double
) {
    val strings = LocalStrings.current
    var amount by remember { mutableStateOf("") }
    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
    val estimatedFee = minOf(parsedAmount, feeFlat + (parsedAmount * feeRate))
    val estimatedTransfer = maxOf(0.0, parsedAmount - estimatedFee)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.withdrawFunds, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "${strings.available}: RM ${formatWalletMoney(maxAmount)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Minimum withdrawal: RM ${formatWalletMoney(minimumAmount)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(strings.amountLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("RM", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                )
                if (parsedAmount > 0.0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Estimated transfer: RM ${formatWalletMoney(estimatedTransfer)}" +
                            if (estimatedFee > 0.0) " after RM ${formatWalletMoney(estimatedFee)} fee" else "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = parsedAmount >= minimumAmount && parsedAmount <= maxAmount && estimatedTransfer > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = WalletBlue)
            ) {
                Text(strings.withdraw)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

private fun formatWalletMoney(value: Double): String {
    val rounded = round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}
