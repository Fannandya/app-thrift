package com.mamay.cobain.data.repository

import com.mamay.cobain.data.dao.StoreProfileDao
import com.mamay.cobain.data.dao.ThriftItemDao
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.StoreProfile
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomThriftItemRepository(
    private val dao: ThriftItemDao,
    private val storeProfileDao: StoreProfileDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ThriftItemRepository {

    override val allItems: Flow<List<ThriftItem>> = dao.getAllItems()
    override val allCategories: Flow<List<ItemCategory>> = dao.getAllCategories()
    override val allSizes: Flow<List<ItemSize>> = dao.getAllSizes()
    override val allSales: Flow<List<ThriftSale>> = dao.getAllSales()

    // A fresh install creates store_profile from Room's own schema, not from
    // MIGRATION_2_3, so the seeded row is not there and the query emits null.
    // Mapping to a blank profile keeps every consumer free of null handling.
    override val storeProfile: Flow<StoreProfile> =
        storeProfileDao.observeProfile().map { it ?: StoreProfile() }

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

    override suspend fun saveStoreProfile(profile: StoreProfile): Result<Unit> = safeCall {
        storeProfileDao.upsertProfile(profile.copy(id = StoreProfile.SINGLETON_ID))
    }

    override suspend fun recordSaleTransaction(items: List<ThriftItem>, sales: List<ThriftSale>): Result<Unit> = safeCall {
        dao.recordSaleTransaction(items, sales)
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
