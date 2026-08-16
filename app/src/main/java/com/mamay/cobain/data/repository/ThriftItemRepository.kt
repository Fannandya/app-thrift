package com.mamay.cobain.data.repository

import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import kotlinx.coroutines.flow.Flow

/**
 * Interface boundary so ThriftViewModel can be unit-tested against a fake, without
 * touching Room/SQLite (and therefore without needing an Android device/emulator).
 * Every write returns Result so the caller can surface a real error to the user
 * instead of it being swallowed.
 */
interface ThriftItemRepository {
    val allItems: Flow<List<ThriftItem>>
    val allCategories: Flow<List<ItemCategory>>
    val allSizes: Flow<List<ItemSize>>
    val allSales: Flow<List<ThriftSale>>

    suspend fun insert(item: ThriftItem): Result<Unit>
    suspend fun update(item: ThriftItem): Result<Unit>
    suspend fun delete(item: ThriftItem): Result<Unit>

    suspend fun insertCategory(name: String): Result<Unit>
    suspend fun deleteCategory(category: ItemCategory): Result<Unit>

    suspend fun insertSize(name: String): Result<Unit>
    suspend fun deleteSize(size: ItemSize): Result<Unit>

    suspend fun recordSale(updatedItem: ThriftItem, sale: ThriftSale): Result<Unit>
}
