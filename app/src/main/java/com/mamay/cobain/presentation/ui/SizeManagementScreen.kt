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
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.presentation.ui.components.ConfirmDialog
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeManagementScreen(
    viewModel: ThriftViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sizes by viewModel.sizes.collectAsState()
    var sizePendingDelete by remember { mutableStateOf<ItemSize?>(null) }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Ukuran") },
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
                        onDelete = { sizePendingDelete = size }
                    )
                    Spacer(modifier = Modifier.padding(bottom = 8.dp))
                }
            }
        }
    }

    val pendingSize = sizePendingDelete
    if (pendingSize != null) {
        ConfirmDialog(
            title = "Hapus Ukuran",
            message = "Hapus ukuran \"${pendingSize.name}\"? Barang yang memakai ukuran ini akan menjadi tanpa ukuran.",
            onDismiss = { sizePendingDelete = null },
            onConfirm = { viewModel.deleteSize(pendingSize) }
        )
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