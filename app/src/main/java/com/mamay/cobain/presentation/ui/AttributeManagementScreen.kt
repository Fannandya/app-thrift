package com.mamay.cobain.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.data.entity.AttributeMode
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption
import com.mamay.cobain.presentation.ui.components.ConfirmDialog
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeManagementScreen(
    viewModel: ThriftViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attributes by viewModel.attributes.collectAsStateWithLifecycle()
    val options by viewModel.attributeOptions.collectAsStateWithLifecycle()
    val itemTerm = viewModel.storeProfile.collectAsStateWithLifecycle().value.itemTerm

    var attributePendingDelete by remember { mutableStateOf<ItemAttribute?>(null) }
    var optionPendingDelete by remember { mutableStateOf<ItemAttributeOption?>(null) }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Atribut") },
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Atribut adalah kolom tambahan untuk $itemTerm, mis. Ukuran, Warna, Merek. " +
                        "Mode \"Daftar\" punya pilihan yang kamu kelola; mode \"Teks\" diketik bebas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                AddAttributeRow(onAdd = viewModel::addAttribute)
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
            }

            item {
                Text(
                    text = "Daftar Atribut",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (attributes.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada atribut. Tambahkan di atas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(attributes, key = { it.id }) { attribute ->
                    AttributeCard(
                        attribute = attribute,
                        options = options.filter { it.attributeId == attribute.id },
                        onToggleRequired = {
                            viewModel.updateAttribute(attribute.copy(required = !attribute.required))
                        },
                        onSetMode = { mode ->
                            viewModel.updateAttribute(attribute.copy(mode = mode.name))
                        },
                        onDelete = { attributePendingDelete = attribute },
                        onAddOption = { value -> viewModel.addAttributeOption(attribute.id, value) },
                        onDeleteOption = { optionPendingDelete = it }
                    )
                    Spacer(modifier = Modifier.padding(bottom = 12.dp))
                }
            }
        }
    }

    attributePendingDelete?.let { attr ->
        ConfirmDialog(
            title = "Hapus Atribut",
            message = "Hapus atribut \"${attr.name}\"? Nilai atribut ini pada semua $itemTerm akan ikut terhapus. " +
                "Riwayat penjualan tidak terpengaruh.",
            onDismiss = { attributePendingDelete = null },
            onConfirm = {
                viewModel.deleteAttribute(attr)
                attributePendingDelete = null
            }
        )
    }

    optionPendingDelete?.let { option ->
        ConfirmDialog(
            title = "Hapus Pilihan",
            message = "Hapus pilihan \"${option.value}\"? $itemTerm yang sudah memakai nilai ini tidak berubah.",
            onDismiss = { optionPendingDelete = null },
            onConfirm = {
                viewModel.deleteAttributeOption(option)
                optionPendingDelete = null
            }
        )
    }
}

@Composable
private fun AddAttributeRow(onAdd: (String, AttributeMode, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(AttributeMode.LIST) }
    var required by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama atribut baru") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                onAdd(name, mode, required)
                name = ""
                required = false
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah atribut", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = mode == AttributeMode.LIST,
                onClick = { mode = AttributeMode.LIST },
                label = { Text("Daftar pilihan") }
            )
            FilterChip(
                selected = mode == AttributeMode.FREE_TEXT,
                onClick = { mode = AttributeMode.FREE_TEXT },
                label = { Text("Teks bebas") }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = required, onCheckedChange = { required = it })
            Text("Wajib diisi")
        }
    }
}

@Composable
private fun AttributeCard(
    attribute: ItemAttribute,
    options: List<ItemAttributeOption>,
    onToggleRequired: () -> Unit,
    onSetMode: (AttributeMode) -> Unit,
    onDelete: () -> Unit,
    onAddOption: (String) -> Unit,
    onDeleteOption: (ItemAttributeOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val mode = runCatching { AttributeMode.valueOf(attribute.mode) }.getOrDefault(AttributeMode.FREE_TEXT)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = attribute.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus atribut", tint = MaterialTheme.colorScheme.error)
                }
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = mode == AttributeMode.LIST,
                    onClick = { onSetMode(AttributeMode.LIST) },
                    label = { Text("Daftar") }
                )
                FilterChip(
                    selected = mode == AttributeMode.FREE_TEXT,
                    onClick = { onSetMode(AttributeMode.FREE_TEXT) },
                    label = { Text("Teks") }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = attribute.required, onCheckedChange = { onToggleRequired() })
                Text("Wajib diisi")
            }

            if (mode == AttributeMode.LIST) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilihan (${options.size})",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Tutup" else "Buka"
                        )
                    }
                }
                if (expanded) {
                    OptionEditor(
                        options = options,
                        onAddOption = onAddOption,
                        onDeleteOption = onDeleteOption
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionEditor(
    options: List<ItemAttributeOption>,
    onAddOption: (String) -> Unit,
    onDeleteOption: (ItemAttributeOption) -> Unit
) {
    var value by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Nilai baru") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                onAddOption(value)
                value = ""
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah pilihan", tint = MaterialTheme.colorScheme.primary)
            }
        }
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = option.value, modifier = Modifier.weight(1f))
                IconButton(onClick = { onDeleteOption(option) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus pilihan", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
