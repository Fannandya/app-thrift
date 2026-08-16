package com.mamay.cobain.data.repository

import com.mamay.cobain.data.dao.ThriftItemStorage
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import kotlinx.coroutines.flow.Flow

class ThriftItemRepository(private val storage: ThriftItemStorage) {
    val allItems: Flow<List<ThriftItem>> = storage.itemsFlow

    val allCategories: Flow<List<ItemCategory>> = storage.categoriesFlow

    val allSizes: Flow<List<ItemSize>> = storage.sizesFlow

    val allSales: Flow<List<ThriftSale>> = storage.salesFlow

    fun insert(item: ThriftItem) = storage.insert(item)

    fun update(item: ThriftItem) = storage.update(item)

    fun delete(item: ThriftItem) = storage.delete(item)

    fun getItemById(id: Int): ThriftItem? = storage.getItemById(id)

    fun insertCategory(name: String) = storage.insertCategory(name)

    fun deleteCategory(category: ItemCategory) = storage.deleteCategory(category)

    fun insertSize(name: String) = storage.insertSize(name)

    fun deleteSize(size: ItemSize) = storage.deleteSize(size)

    fun insertSale(sale: ThriftSale) = storage.insertSale(sale)
}