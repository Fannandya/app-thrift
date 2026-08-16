package com.mamay.cobain.data.dao

import android.content.Context
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ThriftItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class ThriftItemStorage(private val context: Context) {
    private val itemsFilename = "thrift_items.json"
    private val itemsFile: File
        get() = File(context.filesDir, itemsFilename)

    private val categoriesFilename = "thrift_categories.json"
    private val categoriesFile: File
        get() = File(context.filesDir, categoriesFilename)

    private val _itemsFlow = MutableStateFlow<List<ThriftItem>>(emptyList())
    val itemsFlow: Flow<List<ThriftItem>> = _itemsFlow.asStateFlow()

    private val _categoriesFlow = MutableStateFlow<List<ItemCategory>>(emptyList())
    val categoriesFlow: Flow<List<ItemCategory>> = _categoriesFlow.asStateFlow()

    init {
        loadItems()
        loadCategories()
    }

    private fun loadItems() {
        val items = if (itemsFile.exists()) {
            try {
                val json = itemsFile.readText()
                Json.decodeFromString(ListSerializer(ThriftItem.serializer()), json)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _itemsFlow.value = items
    }

    private fun saveItems(items: List<ThriftItem>) {
        try {
            val json = Json.encodeToString(ListSerializer(ThriftItem.serializer()), items)
            itemsFile.writeText(json)
            _itemsFlow.value = items
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadCategories() {
        val categories = if (categoriesFile.exists()) {
            try {
                val json = categoriesFile.readText()
                Json.decodeFromString(ListSerializer(ItemCategory.serializer()), json)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _categoriesFlow.value = categories
    }

    private fun saveCategories(categories: List<ItemCategory>) {
        try {
            val json = Json.encodeToString(ListSerializer(ItemCategory.serializer()), categories)
            categoriesFile.writeText(json)
            _categoriesFlow.value = categories
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun insert(item: ThriftItem) {
        val newId = (_itemsFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        val itemWithId = item.copy(id = newId)
        val updatedList = _itemsFlow.value + itemWithId
        saveItems(updatedList)
    }

    fun update(item: ThriftItem) {
        val updatedList = _itemsFlow.value.map { if (it.id == item.id) item else it }
        saveItems(updatedList)
    }

    fun delete(item: ThriftItem) {
        val updatedList = _itemsFlow.value.filter { it.id != item.id }
        saveItems(updatedList)
    }

    fun getItemById(id: Int): ThriftItem? {
        return _itemsFlow.value.find { it.id == id }
    }

    fun insertCategory(name: String) {
        val newId = (_categoriesFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        val category = ItemCategory(id = newId, name = name)
        saveCategories(_categoriesFlow.value + category)
    }

    fun deleteCategory(category: ItemCategory) {
        val updatedList = _categoriesFlow.value.filter { it.id != category.id }
        saveCategories(updatedList)
    }
}