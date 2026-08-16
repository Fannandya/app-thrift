package com.mamay.cobain.data.repository

import com.mamay.cobain.data.dao.ThriftItemDao
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomThriftItemRepository(
    private val dao: ThriftItemDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ThriftItemRepository {

    override val allItems: Flow<List<ThriftItem>> = dao.getAllItems()
    override val allCategories: Flow<List<ItemCategory>> = dao.getAllCategories()
    override val allSizes: Flow<List<ItemSize>> = dao.getAllSizes()
    override val allSales: Flow<List<ThriftSale>> = dao.getAllSales()

    override suspend fun insert(item: ThriftItem): Result<Unit> = safeCall {
        dao.insertItem(item)
    }

    override suspend fun update(item: ThriftItem): Result<Unit> = safeCall {
        dao.updateItem(item)
    }

    override suspend fun delete(item: ThriftItem): Result<Unit> = safeCall {
        dao.deleteItem(item)
    }

    override suspend fun insertCategory(name: String): Result<Unit> = safeCall {
        dao.insertCategory(ItemCategory(name = name))
    }

    override suspend fun deleteCategory(category: ItemCategory): Result<Unit> = safeCall {
        dao.deleteCategory(category)
    }

    override suspend fun insertSize(name: String): Result<Unit> = safeCall {
        dao.insertSize(ItemSize(name = name))
    }

    override suspend fun deleteSize(size: ItemSize): Result<Unit> = safeCall {
        dao.deleteSize(size)
    }

    override suspend fun recordSale(updatedItem: ThriftItem, sale: ThriftSale): Result<Unit> = safeCall {
        dao.recordSale(updatedItem, sale)
    }

    private suspend fun safeCall(block: suspend () -> Unit): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                block()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
