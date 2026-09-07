package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import com.mamay.cobain.util.formatRupiah
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class HistoryRange(val label: String, val days: Int?) {
    TODAY("Hari Ini", 1),
    WEEK("7 Hari", 7),
    MONTH("30 Hari", 30),
    ALL("Semua", null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val profile by viewModel.storeProfile.collectAsState()

    val linesByTransaction = remember(sales) { sales.groupBy { it.transactionId } }

    var selectedTransactionId by rememberSaveable { mutableStateOf<String?>(null) }

    val selected = selectedTransactionId?.let { id -> transactions.find { it.id == id } }
    if (selectedTransactionId != null && selected == null) {
        // Transaksinya sudah tidak ada (mis. data dipulihkan dari backup lain) saat
        // detailnya terbuka - sama seperti penanganan di ThriftInventoryScreen.
        LaunchedEffect(selectedTransactionId) { selectedTransactionId = null }
    }

    if (selected != null) {
        TransactionDetailScreen(
            transaction = selected,
            lines = linesByTransaction[selected.id].orEmpty(),
            profile = profile,
            onBack = { selectedTransactionId = null },
            modifier = modifier
        )
        return
    }

    var rangeName by rememberSaveable { mutableStateOf(HistoryRange.WEEK.name) }
    val range = HistoryRange.valueOf(rangeName)

    val cutoff = range.days?.let { days ->
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(days - 1))
        }.timeInMillis
    }
    val visible = if (cutoff == null) transactions else transactions.filter { it.timestamp >= cutoff }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Riwayat Transaksi",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${visible.size} transaksi · ${formatRupiah(visible.sumOf { it.total.toLong() })}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(HistoryRange.entries.toList()) { option ->
                FilterChip(
                    selected = range == option,
                    onClick = { rangeName = option.name },
                    label = { Text(option.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (visible.isEmpty()) {
            Text(
                text = "Belum ada transaksi pada rentang ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visible, key = { it.id }) { transaction ->
                    val lines = linesByTransaction[transaction.id].orEmpty()
                    Card(
                        onClick = { selectedTransactionId = transaction.id },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lines.size == 1) {
                                        lines.first().itemName
                                    } else {
                                        "${lines.size} jenis barang"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${lines.sumOf { it.quantity }} barang · " +
                                        dateFormat.format(Date(transaction.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (transaction.discountAmount > 0) {
                                    Text(
                                        text = "Diskon ${formatRupiah(transaction.discountAmount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Text(
                                text = formatRupiah(transaction.total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
