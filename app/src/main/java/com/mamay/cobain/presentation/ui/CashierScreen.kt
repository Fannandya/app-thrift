package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.domain.DiscountType
import com.mamay.cobain.domain.calculateCheckoutTotals
import com.mamay.cobain.presentation.viewmodel.CartLine
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
    val cart by viewModel.cart.collectAsState()
    val profile by viewModel.storeProfile.collectAsState()
    val availableItems = items.filter { it.quantity > 0 && !it.isSold }
    val categoryNameById = remember(categories) { categories.associate { it.id to it.name } }
    val sizeNameById = remember(sizes) { sizes.associate { it.id to it.name } }
    val cartQuantityById = remember(cart) { cart.associate { it.item.id to it.quantity } }

    val availableCategories = remember(availableItems, categories) {
        categories.filter { category -> availableItems.any { it.categoryId == category.id } }
    }
    var selectedCategoryId by remember { mutableStateOf(ALL_CATEGORIES_ID) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

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
            text = "Kasir \u00b7 ${profile.storeName.ifBlank { "Toko Belum Diberi Nama" }}",
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
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tidak ada barang tersedia di kategori ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val cartQuantity = cartQuantityById[item.id] ?: 0
                    CashierItemCard(
                        item = item,
                        categoryName = categoryNameById[item.categoryId] ?: "",
                        sizeName = sizeNameById[item.sizeId] ?: "",
                        cartQuantity = cartQuantity,
                        onAdd = { viewModel.addToCart(item) },
                        onIncrement = { viewModel.updateCartQuantity(item, cartQuantity + 1) },
                        onDecrement = { viewModel.updateCartQuantity(item, cartQuantity - 1) }
                    )
                }
            }
        }

        if (cart.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            CartSummaryBar(
                cart = cart,
                onCheckoutClick = { showCheckoutDialog = true }
            )
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            cart = cart,
            sizeNameById = sizeNameById,
            onDismiss = { showCheckoutDialog = false },
            onIncrement = { item -> viewModel.updateCartQuantity(item, (cartQuantityById[item.id] ?: 0) + 1) },
            onDecrement = { item -> viewModel.updateCartQuantity(item, (cartQuantityById[item.id] ?: 0) - 1) },
            onRemove = { item -> viewModel.removeFromCart(item) },
            onConfirm = { type, value, paid ->
                viewModel.checkout(type, value, paid)
                showCheckoutDialog = false
            }
        )
    }
}

@Composable
private fun CashierItemCard(
    item: ThriftItem,
    categoryName: String,
    sizeName: String,
    cartQuantity: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (cartQuantity <= 0) {
                    Button(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.height(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah")
                    }
                } else {
                    QuantityStepper(
                        quantity = cartQuantity,
                        onIncrement = onIncrement,
                        onDecrement = onDecrement
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onDecrement) {
            Icon(Icons.Default.Remove, contentDescription = "Kurangi jumlah")
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(onClick = onIncrement) {
            Icon(Icons.Default.Add, contentDescription = "Tambah jumlah")
        }
    }
}

@Composable
private fun CartSummaryBar(
    cart: List<CartLine>,
    onCheckoutClick: () -> Unit
) {
    val itemCount = cart.sumOf { it.quantity }
    val total = cart.sumOf { it.item.sellPrice * it.quantity }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$itemCount barang",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatRupiah(total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Button(onClick = onCheckoutClick) {
            Text("Bayar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutDialog(
    cart: List<CartLine>,
    sizeNameById: Map<Int, String>,
    onDismiss: () -> Unit,
    onIncrement: (ThriftItem) -> Unit,
    onDecrement: (ThriftItem) -> Unit,
    onRemove: (ThriftItem) -> Unit,
    onConfirm: (DiscountType, Int, Int) -> Unit
) {
    var discountType by remember { mutableStateOf(DiscountType.NONE) }
    var discountValue by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("") }

    // Satu sumber kebenaran untuk semua angka di dialog ini: subtotal, diskon, total,
    // dan kembalian dihitung ulang oleh fungsi yang sama yang dipakai ViewModel saat
    // menyimpan, jadi yang dilihat kasir tidak akan pernah beda dari yang tercatat.
    val totals = calculateCheckoutTotals(
        subtotal = cart.sumOf { it.item.sellPrice * it.quantity },
        discountType = discountType,
        discountValue = discountValue.toIntOrNull() ?: 0,
        paidAmount = paidAmount.toIntOrNull() ?: 0
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Keranjang") },
        text = {
            if (cart.isEmpty()) {
                Text("Keranjang kosong.")
            } else {
                Column(
                    modifier = Modifier
                        // Cukup tinggi supaya kolom "Uang Dibayar" dan baris kembalian
                        // ikut terlihat tanpa menggulir: itu kontrol terpenting di kasir,
                        // dan menyembunyikannya di bawah lipatan memperlambat antrean.
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    cart.forEachIndexed { index, line ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        CartLineRow(
                            line = line,
                            sizeName = sizeNameById[line.item.sizeId] ?: "",
                            onIncrement = { onIncrement(line.item) },
                            onDecrement = { onDecrement(line.item) },
                            onRemove = { onRemove(line.item) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Diskon",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiscountType.entries.forEach { type ->
                            FilterChip(
                                selected = discountType == type,
                                onClick = {
                                    discountType = type
                                    if (type == DiscountType.NONE) discountValue = ""
                                },
                                label = {
                                    Text(
                                        when (type) {
                                            DiscountType.NONE -> "Tanpa"
                                            DiscountType.AMOUNT -> "Rp"
                                            DiscountType.PERCENT -> "%"
                                        }
                                    )
                                }
                            )
                        }
                    }

                    if (discountType != DiscountType.NONE) {
                        OutlinedTextField(
                            value = discountValue,
                            onValueChange = { input -> discountValue = input.filter { it.isDigit() } },
                            label = {
                                Text(
                                    if (discountType == DiscountType.PERCENT) "Diskon (%)" else "Diskon (Rp)"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SummaryRow("Subtotal", formatRupiah(totals.subtotal))
                    if (totals.discountAmount > 0) {
                        SummaryRow(
                            label = if (discountType == DiscountType.PERCENT) {
                                "Diskon (${discountValue.toIntOrNull()?.coerceIn(0, 100) ?: 0}%)"
                            } else {
                                "Diskon"
                            },
                            value = "-${formatRupiah(totals.discountAmount)}",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "TOTAL", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = formatRupiah(totals.total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = paidAmount,
                        onValueChange = { input -> paidAmount = input.filter { it.isDigit() } },
                        label = { Text("Uang Dibayar (Tunai)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = paidAmount.isNotEmpty() && !totals.isPaidEnough,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (totals.isPaidEnough) {
                        SummaryRow(
                            label = "Kembalian",
                            value = formatRupiah(totals.changeAmount),
                            valueColor = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        SummaryRow(
                            label = "Kurang",
                            value = formatRupiah(totals.total - (paidAmount.toIntOrNull() ?: 0)),
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(discountType, discountValue.toIntOrNull() ?: 0, paidAmount.toIntOrNull() ?: 0)
                },
                enabled = cart.isNotEmpty() && totals.isPaidEnough
            ) {
                Text("Selesaikan Transaksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun CartLineRow(
    line: CartLine,
    sizeName: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = line.item.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ukuran: ${sizeName.ifBlank { "-" }} · ${formatRupiah(line.item.sellPrice)} x ${line.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        QuantityStepper(
            quantity = line.quantity,
            onIncrement = onIncrement,
            onDecrement = onDecrement
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus dari keranjang",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** Satu baris ringkasan uang di dialog checkout: label di kiri, nominal rata kanan. */
@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
