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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import com.mamay.cobain.util.formatRupiah

private const val ALL_CATEGORIES_ID = -1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val sizes by viewModel.sizes.collectAsState()
    val availableItems = items.filter { it.quantity > 0 && !it.isSold }
    val categoryNameById = remember(categories) { categories.associate { it.id to it.name } }
    val sizeNameById = remember(sizes) { sizes.associate { it.id to it.name } }

    val availableCategories = remember(availableItems, categories) {
        categories.filter { category -> availableItems.any { it.categoryId == category.id } }
    }
    var selectedCategoryId by remember { mutableStateOf(ALL_CATEGORIES_ID) }
    var selectedItemForSale by remember { mutableStateOf<ThriftItem?>(null) }
    var showSaleDialog by remember { mutableStateOf(false) }

    val filteredItems = if (selectedCategoryId == ALL_CATEGORIES_ID) {
        availableItems
    } else {
        availableItems.filter { it.categoryId == selectedCategoryId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Kasir",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Layani penjualan dengan memilih barang yang dibeli",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategoryId == ALL_CATEGORIES_ID,
                    onClick = { selectedCategoryId = ALL_CATEGORIES_ID },
                    label = { Text("Semua") }
                )
            }
            items(availableCategories, key = { it.id }) { category ->
                FilterChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { selectedCategoryId = category.id },
                    label = { Text(category.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {
            Text(
                text = "Tidak ada barang tersedia di kategori ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    CashierItemCard(
                        item = item,
                        categoryName = categoryNameById[item.categoryId] ?: "",
                        sizeName = sizeNameById[item.sizeId] ?: "",
                        onClick = {
                            selectedItemForSale = item
                            showSaleDialog = true
                        }
                    )
                }
            }
        }
    }

    val itemForSale = selectedItemForSale
    if (showSaleDialog && itemForSale != null) {
        SaleDialog(
            item = itemForSale,
            sizeName = sizeNameById[itemForSale.sizeId] ?: "",
            onDismiss = { showSaleDialog = false },
            onConfirm = { quantity ->
                viewModel.recordSale(itemForSale, quantity)
                showSaleDialog = false
                selectedItemForSale = null
            }
        )
    }
}

@Composable
private fun CashierItemCard(
    item: ThriftItem,
    categoryName: String,
    sizeName: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ukuran: ${sizeName.ifBlank { "-" }} · Kategori: ${categoryName.ifBlank { "-" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Stok: ${item.quantity}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (item.quantity > 0)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = formatRupiah(item.sellPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SaleDialog(
    item: ThriftItem,
    sizeName: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    val quantityInt = quantity.toIntOrNull() ?: 0
    val validatedQuantity = quantityInt.coerceIn(0, item.quantity)
    val totalPrice = item.sellPrice * validatedQuantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transaksi Penjualan") },
        text = {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Ukuran: ${sizeName.ifBlank { "-" }} · Stok: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { value ->
                        val digits = value.filter { it.isDigit() }
                        val parsed = digits.toIntOrNull() ?: 0
                        quantity = parsed.coerceAtMost(item.quantity).toString()
                    },
                    label = { Text("Jumlah dibeli") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Harga",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatRupiah(totalPrice),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(validatedQuantity) },
                enabled = validatedQuantity > 0
            ) {
                Text("Simpan Transaksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
