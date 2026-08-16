package com.mamay.cobain.data.repository

import com.mamay.cobain.data.dao.ThriftItemStorage
import com.mamay.cobain.data.entity.ThriftItem
import kotlinx.coroutines.flow.Flow

class ThriftItemRepository(private val storage: ThriftItemStorage) {
    val allItems: Flow<List<ThriftItem>> = storage.itemsFlow

    fun insert(item: ThriftItem) = storage.insert(item)

    fun update(item: ThriftItem) = storage.update(item)

    fun delete(item: ThriftItem) = storage.delete(item)

    fun getItemById(id: Int): ThriftItem? = storage.getItemById(id)
}
