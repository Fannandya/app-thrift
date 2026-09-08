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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mamay.cobain.domain.indexActiveDiscounts
import com.mamay.cobain.presentation.ui.components.SearchField
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
    val items by viewModel.items.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val attributes by viewModel.attributes.collectAsStateWithLifecycle()
    val attributeOptions by viewModel.attributeOptions.collectAsStateWithLifecycle()
    val attributeValues by viewModel.attributeValues.collectAsStateWithLifecycle()
    val discounts by viewModel.discounts.collectAsStateWithLifecycle()
    val discountItems by viewModel.discountItems.collectAsStateWithLifecycle()
    val profile by viewModel.storeProfile.collectAsStateWithLifecycle()
    val itemTerm = profile.itemTerm

    val now = remember { System.currentTimeMillis() }

    val valuesByItem = remember(attributeValues) {
        attributeValues.groupBy { it.itemId }.mapValues { entry -> entry.value.associate { it.attributeId to it.value } }
    }
    val activeByItem = remember(discounts, discountItems, now) {
        indexActiveDiscounts(discounts, discountItems, now)
    }

    var selectedItemId by rememberSaveable { mutableStateOf<Int?>(null) }

    val itemForDetail = selectedItemId?.let { id -> items.find { it.id == id } }
    if (selectedItemId != null && itemForDetail == null) {
        LaunchedEffect(selectedItemId) { selectedItemId = null }
    }

    if (itemForDetail != null) {
        ItemDetailScreen(
            item = itemForDetail,
            categories = categories,
            attributes = attributes,
            attributeOptions = attributeOptions,
            attributeValuesForItem = valuesByItem[itemForDetail.id].orEmpty(),
            activeDiscount = displayDiscountFrom(itemForDetail, activeByItem[itemForDetail.id].orEmpty()),
            itemTerm = itemTerm,
            viewModel = viewModel,
            onBack = { selectedItemId = null },
            modifier = modifier
        )
        return
    }

    val categoryNameById = remember(categories) { categories.associate { it.id to it.name } }
    val availableCategories = remember(items, categories) {
        categories.filter { category -> items.any { it.categoryId == category.id } }
    }
    val attributeOptionsPresent = remember(items, attributes, valuesByItem) {
        attributes.sortedBy { it.displayOrder }.associateWith { attr ->
            items.mapNotNull { valuesByItem[it.id]?.get(attr.id)?.takeIf { v -> v.isNotBlank() } }
                .distinct()
                .sorted()
        }
    }
    var selectedCategoryId by rememberSaveable { mutableStateOf(ALL_CATEGORIES_ID) }
    var selectedStatusFilterName by rememberSaveable { mutableStateOf(StatusFilter.ALL.name) }
    val selectedStatusFilter = StatusFilter.valueOf(selectedStatusFilterName)
    // attributeId -> chosen value; absence means "no filter on that attribute".
    val attributeFilters = remember { mutableStateMapOf<Int, String>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    val filteredItems = remember(
        items, query, selectedCategoryId, selectedStatusFilterName, attributeFilters.toMap(), valuesByItem
    ) {
        val q = query.trim()
        items
            .filter { q.isBlank() || it.name.contains(q, ignoreCase = true) }
            .filter { selectedCategoryId == ALL_CATEGORIES_ID || it.categoryId == selectedCategoryId }
            .filter {
                when (selectedStatusFilter) {
                    StatusFilter.ALL -> true
                    StatusFilter.AVAILABLE -> !it.isSold
                    StatusFilter.SOLD -> it.isSold
                }
            }
            .filter { item ->
                attributeFilters.all { (attrId, value) -> valuesByItem[item.id]?.get(attrId) == value }
            }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Inventaris $itemTerm · ${profile.storeName.ifBlank { "Toko Belum Diberi Nama" }}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah $itemTerm")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Cari nama $itemTerm..."
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                        onClick = { selectedStatusFilterName = status.name },
                        label = { Text(status.label) }
                    )
                }
            }

            // One filter row per attribute, over the distinct values present on the
            // items currently in stock (precomputed in attributeOptionsPresent).
            attributeOptionsPresent.forEach { (attr, present) ->
                if (present.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = !attributeFilters.containsKey(attr.id),
                                onClick = { attributeFilters.remove(attr.id) },
                                label = { Text("Semua ${attr.name}") }
                            )
                        }
                        items(present, key = { it }) { value ->
                            FilterChip(
                                selected = attributeFilters[attr.id] == value,
                                onClick = { attributeFilters[attr.id] = value },
                                label = { Text(value) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredItems.isEmpty()) {
                Text(
                    text = if (query.isNotBlank()) {
                        "Tidak ada $itemTerm dengan nama \"$query\"."
                    } else {
                        "Tidak ada $itemTerm yang cocok dengan filter ini."
                    },
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredItems, key = { it.id }) { item ->
                        ThriftItemCard(
                            item = item,
                            categoryName = categoryNameById[item.categoryId] ?: "",
                            attributesText = attributesTextFrom(valuesByItem[item.id].orEmpty(), attributes),
                            activeDiscount = displayDiscountFrom(item, activeByItem[item.id].orEmpty()),
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
            attributes = attributes,
            attributeOptions = attributeOptions,
            itemTerm = itemTerm,
            onDismiss = { showAddDialog = false },
            onSave = { name, categoryId, quantity, buyPrice, sellPrice, values ->
                viewModel.addItem(name, categoryId, quantity, buyPrice, sellPrice, values)
                showAddDialog = false
            }
        )
    }
}
