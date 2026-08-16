package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.presentation.ui.components.ConfirmDialog
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThriftInventoryScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val sizes by viewModel.sizes.collectAsState()
    val categoryNameById = remember(categories) { categories.associate { it.id to it.name } }
    val sizeNameById = remember(sizes) { sizes.associate { it.id to it.name } }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ThriftItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Inventaris Pakaian Thrift") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah barang")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (items.isEmpty()) {
                Text(
                    text = "Belum ada item. Tekan + untuk menambah barang.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { item ->
                        ThriftItemCard(
                            item = item,
                            categoryName = categoryNameById[item.categoryId] ?: "",
                            sizeName = sizeNameById[item.sizeId] ?: "",
                            onItemClick = {
                                selectedItem = it
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                selectedItem = it
                                showDeleteDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            categories = categories,
            sizes = sizes,
            onDismiss = { showAddDialog = false },
            onSave = { name, sizeId, categoryId, quantity, buyPrice, sellPrice ->
                viewModel.addItem(name, sizeId, categoryId, quantity, buyPrice, sellPrice)
                showAddDialog = false
            }
        )
    }

    val itemForEdit = selectedItem
    if (showEditDialog && itemForEdit != null) {
        EditItemDialog(
            item = itemForEdit,
            categories = categories,
            sizes = sizes,
            onDismiss = { showEditDialog = false },
            onSave = { updatedItem ->
                viewModel.updateItem(updatedItem)
                showEditDialog = false
                selectedItem = null
            }
        )
    }

    val itemForDelete = selectedItem
    if (showDeleteDialog && itemForDelete != null) {
        ConfirmDialog(
            title = "Hapus Item",
            message = "Hapus \"${itemForDelete.name}\"? Tindakan ini tidak bisa dibatalkan.",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteItem(itemForDelete)
                selectedItem = null
            }
        )
    }
}
