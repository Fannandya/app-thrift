package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

@Composable
fun SettingsScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val sizes by viewModel.sizes.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Pengaturan Toko",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Kelola kategori dan ukuran barang untuk memudahkan input inventaris",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(bottom = 16.dp))
        }

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
                    onDelete = { viewModel.deleteCategory(category) }
                )
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        item {
            AddSizeRow(
                onAdd = { name ->
                    if (name.isNotBlank()) {
                        viewModel.addSize(name.trim())
                    }
                }
            )
            Spacer(modifier = Modifier.padding(bottom = 16.dp))
        }

        item {
            Text(
                text = "Daftar Ukuran",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (sizes.isEmpty()) {
            item {
                Text(
                    text = "Belum ada ukuran. Tambahkan ukuran di atas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        } else {
            items(sizes, key = { it.id }) { size ->
                SizeRow(
                    size = size,
                    onDelete = { viewModel.deleteSize(size) }
                )
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
            }
        }
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
private fun AddSizeRow(onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama ukuran baru") },
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
                contentDescription = "Tambah ukuran",
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

@Composable
private fun SizeRow(
    size: ItemSize,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = size.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus ukuran",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}