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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.presentation.ui.components.ConfirmDialog
import com.mamay.cobain.presentation.ui.components.DetailRow
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import com.mamay.cobain.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    item: ThriftItem,
    categories: List<ItemCategory>,
    sizes: List<ItemSize>,
    viewModel: ThriftViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val categoryName = categories.find { it.id == item.categoryId }?.name ?: ""
    val sizeName = sizes.find { it.id == item.sizeId }?.name ?: ""
    val estimatedProfit = (item.sellPrice - item.buyPrice) * item.quantity

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(item.name) },
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
                .padding(16.dp)
        ) {
            Text(
                text = if (item.isSold) "Terjual" else "Tersedia",
                style = MaterialTheme.typography.labelMedium,
                color = if (item.isSold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .background(
                        if (item.isSold) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow("Kategori", categoryName.ifBlank { "-" })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Ukuran", sizeName.ifBlank { "-" })
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Jumlah", item.quantity.toString())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Harga Beli", formatRupiah(item.buyPrice))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Harga Jual", formatRupiah(item.sellPrice))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DetailRow("Estimasi Untung", formatRupiah(estimatedProfit))

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Barang")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.toggleSoldStatus(item) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (item.isSold) "Tandai Tersedia" else "Tandai Terjual")
            }

            Text(
                text = "Status ini hanya menyembunyikan barang dari Kasir. " +
                    "Perubahannya tidak dicatat sebagai penjualan dan tidak mengubah jumlah stok.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hapus Barang")
            }
        }
    }

    if (showEditDialog) {
        EditItemDialog(
            item = item,
            categories = categories,
            sizes = sizes,
            onDismiss = { showEditDialog = false },
            onSave = { updatedItem ->
                viewModel.updateItem(updatedItem)
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Hapus Item",
            message = "Hapus \"${item.name}\"? Tindakan ini tidak bisa dibatalkan.",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteItem(item)
                onBack()
            }
        )
    }
}
