package com.mamay.cobain.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.presentation.ui.components.ConfirmDialog
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: ThriftViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    var categoryPendingDelete by remember { mutableStateOf<ItemCategory?>(null) }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Kategori") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                AddCategoryRow(
                    onAdd = { name ->
                        if (name.isNotBlank()) {
                            viewModel.addCategory(name.trim())
                        }
                    }
                )
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
            }

            item {
                Text(
                    text = "Daftar Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (categories.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada kategori. Tambahkan kategori di atas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            } else {
                items(categories, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onDelete = { categoryPendingDelete = category }
                    )
                    Spacer(modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }

    val pendingCategory = categoryPendingDelete
    if (pendingCategory != null) {
        ConfirmDialog(
            title = "Hapus Kategori",
            message = "Hapus kategori \"${pendingCategory.name}\"? Barang yang memakai kategori ini akan menjadi tanpa kategori.",
            onDismiss = { categoryPendingDelete = null },
            onConfirm = { viewModel.deleteCategory(pendingCategory) }
        )
    }
}

@Composable
private fun AddCategoryRow(onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama kategori baru") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                onAdd(name)
                name = ""
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah kategori",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: ItemCategory,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus kategori",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}