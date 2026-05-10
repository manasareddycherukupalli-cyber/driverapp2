package com.company.carryon.presentation.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.carryon.data.model.InvoiceLink
import com.company.carryon.data.model.Transaction
import com.company.carryon.data.model.TransactionType
import com.company.carryon.data.model.UiState
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
                onDownloadInvoice = { viewModel.fetchInvoiceUrl(transaction.id) }
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
    onDownloadInvoice: () -> Unit
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
                    fontSize = 40.sp
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
            enabled = !isLoading && transaction.type != TransactionType.WITHDRAWAL,
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
                text = if (isLoading) "Preparing invoice…" else "Download Invoice",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
