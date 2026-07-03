package com.company.carryon.presentation.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.InvoiceLink
import com.company.carryon.data.model.Transaction
import com.company.carryon.data.model.TransactionType
import com.company.carryon.data.model.UiState
import com.company.carryon.i18n.LocalStrings
import com.company.carryon.presentation.components.DriveAppTopBar
import com.company.carryon.presentation.navigation.AppNavigator

private val DetailBlue = Color(0xFF2F80ED)
private val DetailBg = Color(0xFFF9F9FF)
private val SectionBg = Color(0xFFA6D2F3).copy(alpha = 0.15f)

/**
 * TransactionDetailScreen — Shows full transaction details and allows invoice download.
 */
@Composable
fun TransactionDetailScreen(navigator: AppNavigator) {
    val viewModel = remember { EarningsViewModel() }
    val transactionsState by viewModel.transactions.collectAsState()
    val invoiceLinkState by viewModel.invoiceLink.collectAsState()
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    val transactionId = navigator.selectedTransactionId ?: ""
    val transaction = (transactionsState as? UiState.Success)?.data
        ?.firstOrNull { it.id == transactionId }

    // Open invoice URL as soon as it's ready
    LaunchedEffect(invoiceLinkState) {
        val url = (invoiceLinkState as? UiState.Success)?.data?.url
        if (!url.isNullOrBlank()) {
            uriHandler.openUri(url)
            viewModel.resetInvoiceLink()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBg)
    ) {
        DriveAppTopBar(
            title = "Transaction Details",
            onBackClick = { navigator.goBack() }
        )

        if (transaction != null) {
            TransactionDetailContent(
                transaction = transaction,
                invoiceLinkState = invoiceLinkState,
                onDownloadInvoice = {
                    if (transaction.type == TransactionType.WITHDRAWAL) {
                        viewModel.fetchPayoutReceiptUrl(transaction.id)
                    } else {
                        viewModel.fetchInvoiceUrl(transaction.id)
                    }
                },
                onCopyStripeId = { clipboardManager.setText(AnnotatedString(it)) }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DetailBlue)
            }
        }
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    invoiceLinkState: UiState<InvoiceLink>,
    onDownloadInvoice: () -> Unit,
    onCopyStripeId: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Amount card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DetailBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = transaction.type.displayName,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "RM${kotlin.math.abs(transaction.amount).toInt()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = transaction.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }

        // Details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SectionBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("Description", transaction.description.ifBlank { transaction.type.displayName })
                DetailRow("Date", transaction.timestamp?.take(10) ?: "-")
                if (!transaction.jobId.isNullOrBlank()) {
                    DetailRow("Job ID", transaction.jobId.take(8).uppercase())
                }
                DetailRow("Transaction ID", transaction.id.take(8).uppercase())
                DetailRow("Type", transaction.type.displayName)
            }
        }

        if (transaction.type == TransactionType.WITHDRAWAL) {
            PayoutBreakdownSection(
                transaction = transaction,
                onCopyStripeId = onCopyStripeId
            )
        }

        Spacer(Modifier.weight(1f))

        // Download invoice button
        val isLoading = invoiceLinkState is UiState.Loading
        val isError = invoiceLinkState is UiState.Error

        if (isError) {
            Text(
                text = (invoiceLinkState as UiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))
        }

        Button(
            onClick = onDownloadInvoice,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DetailBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isLoading) "Preparing invoice..." else "Download Invoice",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PayoutBreakdownSection(
    transaction: Transaction,
    onCopyStripeId: (String) -> Unit
) {
    val strings = LocalStrings.current
    val requestedAmount = transaction.requestedAmount
        .takeIf { it > 0.0 }
        ?: transaction.grossAmount.takeIf { it > 0.0 }
        ?: kotlin.math.abs(transaction.amount)
    val feeAmount = transaction.feeAmount
        .takeIf { it > 0.0 }
        ?: transaction.platformFeeAmount
    val transferAmount = transaction.transferAmount
        .takeIf { it > 0.0 }
        ?: maxOf(0.0, requestedAmount - feeAmount)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.payoutBreakdownTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            DetailRow(strings.requestedAmount, "RM ${formatDetailMoney(requestedAmount)}")
            if (feeAmount > 0.0) {
                DetailRow(strings.feeAmount, "RM ${formatDetailMoney(feeAmount)}")
            }
            DetailRow(strings.transferAmount, "RM ${formatDetailMoney(transferAmount)}", highlight = true)
            transaction.stripeTransferId?.takeIf { it.isNotBlank() }?.let { transferId ->
                CopyableDetailRow(strings.stripeTransferId, transferId, onCopyStripeId)
            }
            if (transaction.status.name == "FAILED" && !transaction.failureMessage.isNullOrBlank()) {
                DetailRow(strings.failureReason, transaction.failureMessage, isError = true)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, highlight: Boolean = false, isError: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                highlight -> DetailBlue
                else -> MaterialTheme.colorScheme.onSurface
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CopyableDetailRow(label: String, value: String, onCopy: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.weight(0.58f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            IconButton(onClick = { onCopy(value) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun formatDetailMoney(value: Double): String {
    val cents = kotlin.math.round(value * 100.0).toLong()
    val whole = cents / 100
    val fraction = (kotlin.math.abs(cents) % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
}
