package com.mamay.cobain.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import kotlinx.coroutines.flow.Flow

@Dao
interface ThriftItemDao {

    @Query("SELECT * FROM items ORDER BY id DESC")
    fun getAllItems(): Flow<List<ThriftItem>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Int): ThriftItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ThriftItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateItem(item: ThriftItem)

    @Delete
    suspend fun deleteItem(item: ThriftItem)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ItemCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ItemCategory): Long

    @Delete
    suspend fun deleteCategory(category: ItemCategory)

    @Query("SELECT * FROM sizes ORDER BY name ASC")
    fun getAllSizes(): Flow<List<ItemSize>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSize(size: ItemSize): Long

    @Delete
    suspend fun deleteSize(size: ItemSize)

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<ThriftSale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: ThriftSale): Long

    /**
     * Reducing stock and recording the sale must land together: a crash or process
     * death between the two writes would otherwise let a sale exist against stock
     * that was never decremented (or vice versa).
     */
    @Transaction
    suspend fun recordSale(updatedItem: ThriftItem, sale: ThriftSale) {
        updateItem(updatedItem)
        insertSale(sale)
    }
}
