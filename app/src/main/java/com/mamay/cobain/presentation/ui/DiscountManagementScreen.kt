package com.mamay.cobain.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.presentation.ui.components.ConfirmDialog
import com.mamay.cobain.presentation.ui.components.SearchField
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id"))

private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun endExclusive(millis: Long): Long = startOfDay(millis) + 24L * 60 * 60 * 1000

private enum class DiscountStatus(val label: String) { SCHEDULED("Terjadwal"), ACTIVE("Aktif"), EXPIRED("Kadaluarsa") }

private fun statusOf(discount: Discount, now: Long): DiscountStatus = when {
    now < discount.startMillis -> DiscountStatus.SCHEDULED
    now >= discount.endMillis -> DiscountStatus.EXPIRED
    else -> DiscountStatus.ACTIVE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountManagementScreen(
    viewModel: ThriftViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discounts by viewModel.discounts.collectAsStateWithLifecycle()
    val discountItems by viewModel.discountItems.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val itemTerm = viewModel.storeProfile.collectAsStateWithLifecycle().value.itemTerm

    val now = System.currentTimeMillis()
    var editing by remember { mutableStateOf<Discount?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Discount?>(null) }
    var showCleanup by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    val itemIdsByDiscount = remember(discountItems) {
        discountItems.groupBy { it.discountId }.mapValues { entry -> entry.value.map { it.itemId } }
    }
    val expiredLinked = remember(discounts, itemIdsByDiscount, now) {
        discounts.filter {
            statusOf(it, now) == DiscountStatus.EXPIRED && itemIdsByDiscount[it.id].orEmpty().isNotEmpty()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Diskon") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showEditor = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah diskon")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (expiredLinked.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${expiredLinked.size} diskon kadaluarsa masih tertaut ke $itemTerm.",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { showCleanup = true }) { Text("Bersihkan") }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (discounts.isEmpty()) {
                item {
                    Text(
                        "Belum ada diskon. Tekan + untuk menambah.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(discounts, key = { it.id }) { discount ->
                    DiscountRow(
                        discount = discount,
                        status = statusOf(discount, now),
                        linkedCount = itemIdsByDiscount[discount.id].orEmpty().size,
                        itemTerm = itemTerm,
                        onEdit = {
                            editing = discount
                            showEditor = true
                        },
                        onDelete = { pendingDelete = discount }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showEditor) {
        DiscountEditorDialog(
            existing = editing,
            allItems = items,
            initialItemIds = editing?.let { itemIdsByDiscount[it.id].orEmpty() }.orEmpty().toSet(),
            itemTerm = itemTerm,
            onDismiss = { showEditor = false },
            onSave = { label, percent, startMillis, endMillis, itemIds ->
                val start = startOfDay(startMillis)
                val end = endExclusive(endMillis)
                val current = editing
                if (current == null) {
                    viewModel.addDiscount(label, percent, start, end, itemIds)
                } else {
                    viewModel.updateDiscount(
                        current.copy(label = label, percent = percent, startMillis = start, endMillis = end),
                        itemIds
                    )
                }
                showEditor = false
            }
        )
    }

    pendingDelete?.let { discount ->
        ConfirmDialog(
            title = "Hapus Diskon",
            message = "Hapus diskon ${discount.percent}%? Tautannya ke $itemTerm akan ikut hilang. " +
                "Riwayat penjualan tidak terpengaruh.",
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.deleteDiscount(discount)
                pendingDelete = null
            }
        )
    }

    if (showCleanup) {
        ConfirmDialog(
            title = "Bersihkan Diskon Kadaluarsa",
            message = "Hapus ${expiredLinked.size} diskon yang sudah lewat tanggal dan masih tertaut?",
            onDismiss = { showCleanup = false },
            onConfirm = {
                viewModel.deleteDiscounts(expiredLinked)
                showCleanup = false
            }
        )
    }
}

@Composable
private fun DiscountRow(
    discount: Discount,
    status: DiscountStatus,
    linkedCount: Int,
    itemTerm: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = discount.label.ifBlank { "Diskon ${discount.percent}%" },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${discount.percent}% · ${dateFormat.format(Date(discount.startMillis))} - " +
                        dateFormat.format(Date(discount.endMillis - 1)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$linkedCount $itemTerm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall,
                color = when (status) {
                    DiscountStatus.ACTIVE -> MaterialTheme.colorScheme.onTertiaryContainer
                    DiscountStatus.SCHEDULED -> MaterialTheme.colorScheme.onSecondaryContainer
                    DiscountStatus.EXPIRED -> MaterialTheme.colorScheme.onErrorContainer
                },
                modifier = Modifier
                    .background(
                        when (status) {
                            DiscountStatus.ACTIVE -> MaterialTheme.colorScheme.tertiaryContainer
                            DiscountStatus.SCHEDULED -> MaterialTheme.colorScheme.secondaryContainer
                            DiscountStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit diskon")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus diskon", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscountEditorDialog(
    existing: Discount?,
    allItems: List<ThriftItem>,
    initialItemIds: Set<Int>,
    itemTerm: String,
    onDismiss: () -> Unit,
    onSave: (label: String, percent: Int, startMillis: Long, endMillis: Long, itemIds: List<Int>) -> Unit
) {
    var label by remember { mutableStateOf(existing?.label.orEmpty()) }
    var percent by remember { mutableStateOf(existing?.percent?.toString() ?: "") }
    var startMillis by remember { mutableStateOf(existing?.startMillis ?: System.currentTimeMillis()) }
    var endMillis by remember { mutableStateOf(existing?.let { it.endMillis - 1 } ?: System.currentTimeMillis()) }
    val selectedItemIds = remember { mutableStateListOf<Int>().apply { addAll(initialItemIds) } }
    var query by remember { mutableStateOf("") }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    val percentInt = percent.toIntOrNull() ?: 0
    val isValid = percentInt in 1..100 && endMillis >= startMillis && selectedItemIds.isNotEmpty()
    val shownItems = remember(allItems, query) {
        val q = query.trim()
        allItems.filter { q.isBlank() || it.name.contains(q, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Diskon Baru" else "Edit Diskon") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                TextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nama diskon (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                TextField(
                    value = percent,
                    onValueChange = { percent = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Persen (1-100)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickingStart = true }, modifier = Modifier.weight(1f)) {
                        Text("Mulai: ${dateFormat.format(Date(startMillis))}")
                    }
                    OutlinedButton(onClick = { pickingEnd = true }, modifier = Modifier.weight(1f)) {
                        Text("Selesai: ${dateFormat.format(Date(endMillis))}")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Pilih $itemTerm (${selectedItemIds.size})", style = MaterialTheme.typography.labelLarge)
                SearchField(query = query, onQueryChange = { query = it }, placeholder = "Cari nama $itemTerm...")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    shownItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = selectedItemIds.contains(item.id),
                                    onValueChange = { checked ->
                                        if (checked) selectedItemIds.add(item.id)
                                        else selectedItemIds.remove(item.id)
                                    }
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = selectedItemIds.contains(item.id), onCheckedChange = null)
                            Text(item.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = { onSave(label.trim(), percentInt, startMillis, endMillis, selectedItemIds.toList()) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )

    if (pickingStart) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startMillis)
        DatePickerDialog(
            onDismissRequest = { pickingStart = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { startMillis = it }
                    pickingStart = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingStart = false }) { Text("Batal") } }
        ) { DatePicker(state = state) }
    }

    if (pickingEnd) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endMillis)
        DatePickerDialog(
            onDismissRequest = { pickingEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { endMillis = it }
                    pickingEnd = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingEnd = false }) { Text("Batal") } }
        ) { DatePicker(state = state) }
    }
}
