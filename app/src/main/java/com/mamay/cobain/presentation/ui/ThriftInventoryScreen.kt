package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import androidx.compose.foundation.layout.Spacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThriftInventoryScreen(viewModel: ThriftViewModel) {
    val items = viewModel.items.collectAsState()
    val showAddDialog = remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf<ThriftItem?>(null) }
    val showEditDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    Scaffold(
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
                onClick = { showAddDialog.value = true },
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
            if (items.value.isEmpty()) {
                Text(
                    text = "Belum ada item. Tekan + untuk menambah barang.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items.value, key = { it.id }) { item ->
                        ThriftItemCard(
                            item = item,
                            onItemClick = {
                                selectedItem.value = it
                                showEditDialog.value = true
                            },
                            onDeleteClick = {
                                selectedItem.value = it
                                showDeleteDialog.value = true
                            }
                        )
                        Spacer(modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog.value) {
        AddItemDialog(
            onDismiss = { showAddDialog.value = false },
            onSave = { name, size, buyPrice, sellPrice ->
                viewModel.addItem(name, size, buyPrice, sellPrice)
                showAddDialog.value = false
            }
        )
    }

    if (showEditDialog.value && selectedItem.value != null) {
        EditItemDialog(
            item = selectedItem.value!!,
            onDismiss = { showEditDialog.value = false },
            onSave = { updatedItem ->
                viewModel.updateItem(updatedItem)
                showEditDialog.value = false
                selectedItem.value = null
            }
        )
    }

    if (showDeleteDialog.value && selectedItem.value != null) {
        DeleteConfirmDialog(
            item = selectedItem.value!!,
            onDismiss = { showDeleteDialog.value = false },
            onConfirm = {
                viewModel.deleteItem(selectedItem.value!!)
                showDeleteDialog.value = false
                selectedItem.value = null
            }
        )
    }
}
