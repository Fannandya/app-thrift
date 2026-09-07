package com.mamay.cobain.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.StoreProfile
import com.mamay.cobain.data.entity.ThriftSale
import com.mamay.cobain.domain.DiscountType
import com.mamay.cobain.domain.buildReceiptText
import com.mamay.cobain.presentation.ui.components.DetailRow
import com.mamay.cobain.presentation.ui.components.ReceiptDialog
import com.mamay.cobain.util.formatRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transaction: SaleTransaction,
    lines: List<ThriftSale>,
    profile: StoreProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var showReceipt by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Transaksi #${transaction.id.take(8)}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = dateFormat.format(Date(transaction.timestamp)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Barang",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            lines.forEach { line ->
                DetailRow(
                    label = "${line.itemName}${if (line.size.isBlank()) "" else " (${line.size})"}" +
                        "\n${line.quantity} x ${formatRupiah(line.sellPrice)}",
                    value = formatRupiah(line.totalPrice)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Subtotal", formatRupiah(transaction.subtotal))
            if (transaction.discountAmount > 0) {
                DetailRow(
                    label = if (transaction.discountType == DiscountType.PERCENT.name) {
                        "Diskon (${transaction.discountValue}%)"
                    } else {
                        "Diskon"
                    },
                    value = "-${formatRupiah(transaction.discountAmount)}",
                    valueColor = MaterialTheme.colorScheme.error
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow(
                label = "TOTAL",
                value = formatRupiah(transaction.total),
                valueColor = MaterialTheme.colorScheme.primary,
                emphasised = true
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Tunai", formatRupiah(transaction.paidAmount))
            DetailRow(
                label = "Kembalian",
                value = formatRupiah(transaction.changeAmount),
                valueColor = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showReceipt = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cetak Ulang Struk")
            }
        }
    }

    if (showReceipt) {
        ReceiptDialog(
            receiptText = buildReceiptText(profile, transaction, lines),
            onDismiss = { showReceipt = false }
        )
    }
}
