package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.domain.ExcelExportRequest
import com.mamay.cobain.domain.ExcelSheet
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class ExportRange(val label: String) {
    THIS_MONTH("Bulan ini"),
    LAST_30("30 hari"),
    ALL("Semua"),
    CUSTOM("Pilih tanggal")
}

private data class ResolvedRange(val start: Long?, val end: Long?)

private fun resolveRange(
    mode: ExportRange,
    customStart: Long?,
    customEnd: Long?,
    now: Long
): ResolvedRange = when (mode) {
    ExportRange.ALL -> ResolvedRange(null, null)
    ExportRange.LAST_30 -> ResolvedRange(now - 30L * 24 * 60 * 60 * 1000, now)
    ExportRange.THIS_MONTH -> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        ResolvedRange(cal.timeInMillis, now)
    }
    ExportRange.CUSTOM -> {
        val end = customEnd?.let { it + 24L * 60 * 60 * 1000 } // picked day inclusive
        ResolvedRange(customStart, end)
    }
}

private val summaryDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    viewModel: ThriftViewModel,
    onDismiss: () -> Unit,
    onExport: (ExcelExportRequest) -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val selectedSheets: SnapshotStateMap<ExcelSheet, Boolean> = remember {
        mutableStateMapOf(
            ExcelSheet.INVENTORY to true,
            ExcelSheet.SALES_BY_ITEM to true,
            ExcelSheet.TRANSACTION_RECAP to true,
            ExcelSheet.PERIOD_SUMMARY to true
        )
    }
    var rangeMode by remember { mutableStateOf(ExportRange.THIS_MONTH) }
    var customStart by remember { mutableStateOf<Long?>(null) }
    var customEnd by remember { mutableStateOf<Long?>(null) }
    var showRangePicker by remember { mutableStateOf(false) }

    val now = remember { System.currentTimeMillis() }
    val resolved = resolveRange(rangeMode, customStart, customEnd, now)

    fun inRange(ts: Long) =
        (resolved.start == null || ts >= resolved.start) && (resolved.end == null || ts < resolved.end)

    val chosen = selectedSheets.filterValues { it }.keys
    val salesRows = remember(sales, resolved) { sales.count { inRange(it.timestamp) } }
    val txRows = remember(transactions, resolved) { transactions.count { inRange(it.timestamp) } }
    val rangeText = when {
        rangeMode == ExportRange.CUSTOM && customStart != null && customEnd != null ->
            "${summaryDateFormat.format(Date(customStart!!))} - ${summaryDateFormat.format(Date(customEnd!!))}"
        rangeMode == ExportRange.CUSTOM -> "tanggal belum dipilih"
        else -> rangeMode.label
    }
    val estimate = buildString {
        append("${chosen.size} sheet")
        if (ExcelSheet.INVENTORY in chosen) append(" · ${items.size} barang")
        if (ExcelSheet.SALES_BY_ITEM in chosen) append(" · $salesRows baris jual")
        if (ExcelSheet.TRANSACTION_RECAP in chosen) append(" · $txRows transaksi")
        append(" · $rangeText")
    }

    val canExport = chosen.isNotEmpty() &&
        !(rangeMode == ExportRange.CUSTOM && (customStart == null || customEnd == null))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ekspor ke Excel") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Sheet yang diekspor", style = MaterialTheme.typography.labelLarge)
                SheetCheckbox("Inventaris", selectedSheets, ExcelSheet.INVENTORY)
                SheetCheckbox("Penjualan per item", selectedSheets, ExcelSheet.SALES_BY_ITEM)
                SheetCheckbox("Rekap transaksi", selectedSheets, ExcelSheet.TRANSACTION_RECAP)
                SheetCheckbox("Ringkasan periode", selectedSheets, ExcelSheet.PERIOD_SUMMARY)

                Text(
                    "Rentang waktu (untuk sheet penjualan)",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportRange.entries.take(2).forEach { mode ->
                        FilterChip(
                            selected = rangeMode == mode,
                            onClick = { rangeMode = mode },
                            label = { Text(mode.label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportRange.entries.drop(2).forEach { mode ->
                        FilterChip(
                            selected = rangeMode == mode,
                            onClick = {
                                rangeMode = mode
                                if (mode == ExportRange.CUSTOM) showRangePicker = true
                            },
                            label = { Text(mode.label) }
                        )
                    }
                }

                Text(
                    text = estimate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canExport,
                onClick = { onExport(ExcelExportRequest(chosen, resolved.start, resolved.end)) }
            ) {
                Text("Ekspor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )

    if (showRangePicker) {
        val state = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    customStart = state.selectedStartDateMillis
                    customEnd = state.selectedEndDateMillis
                    showRangePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) { Text("Batal") }
            }
        ) {
            DateRangePicker(state = state, modifier = Modifier.height(420.dp))
        }
    }
}

@Composable
private fun SheetCheckbox(
    label: String,
    map: SnapshotStateMap<ExcelSheet, Boolean>,
    sheet: ExcelSheet
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = map[sheet] == true,
            onCheckedChange = { map[sheet] = it }
        )
        Text(label)
    }
}
