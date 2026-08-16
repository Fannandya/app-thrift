package com.mamay.cobain.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel

private const val ALL_CATEGORIES_ID = -1

private enum class StatusFilter(val label: String) {
    ALL("Semua"),
    AVAILABLE("Tersedia"),
    SOLD("Terjual")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThriftInventoryScreen(
    viewModel: ThriftViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val sizes by viewModel.sizes.collectAsState()

    var selectedItemId by rememberSaveable { mutableStateOf<Int?>(null) }

    val itemForDetail = selectedItemId?.let { id -> items.find { it.id == id } }
    if (selectedItemId != null && itemForDetail == null) {
        // Item was deleted (e.g. from another session/screen) while its detail was open.
        LaunchedEffect(selectedItemId) { selectedItemId = null }
    }

    if (itemForDetail != null) {
        ItemDetailScreen(
            item = itemForDetail,
            categories = categories,
            sizes = sizes,
            viewModel = viewModel,
            onBack = { selectedItemId = null },
            modifier = modifier
        )
        return
    }

    val categoryNameById = remember(categories) { categories.associate { it.id to it.name } }
    val sizeNameById = remember(sizes) { sizes.associate { it.id to it.name } }
    val availableCategories = remember(items, categories) {
        categories.filter { category -> items.any { it.categoryId == category.id } }
    }
    var selectedCategoryId by remember { mutableStateOf(ALL_CATEGORIES_ID) }
    var selectedStatusFilter by remember { mutableStateOf(StatusFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredItems = items
        .filter { selectedCategoryId == ALL_CATEGORIES_ID || it.categoryId == selectedCategoryId }
        .filter {
            when (selectedStatusFilter) {
                StatusFilter.ALL -> true
                StatusFilter.AVAILABLE -> !it.isSold
                StatusFilter.SOLD -> it.isSold
            }
        }

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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == ALL_CATEGORIES_ID,
                        onClick = { selectedCategoryId = ALL_CATEGORIES_ID },
                        label = { Text("Semua Kategori") }
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

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StatusFilter.entries.toList()) { status ->
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredItems.isEmpty()) {
                Text(
                    text = "Tidak ada barang yang cocok dengan filter ini.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredItems, key = { it.id }) { item ->
                        ThriftItemCard(
                            item = item,
                            categoryName = categoryNameById[item.categoryId] ?: "",
                            sizeName = sizeNameById[item.sizeId] ?: "",
                            onItemClick = { selectedItemId = it.id }
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
}
