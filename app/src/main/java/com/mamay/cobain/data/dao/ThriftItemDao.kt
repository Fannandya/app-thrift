package com.mamay.cobain.data.dao

import android.content.Context
import com.mamay.cobain.data.entity.ThriftItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class ThriftItemStorage(private val context: Context) {
    private val filename = "thrift_items.json"
    private val file: File
        get() = File(context.filesDir, filename)

    private val _itemsFlow = MutableStateFlow<List<ThriftItem>>(emptyList())
    val itemsFlow: Flow<List<ThriftItem>> = _itemsFlow.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        val items = if (file.exists()) {
            try {
                val json = file.readText()
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
            file.writeText(json)
            _itemsFlow.value = items
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
}
