package com.mamay.cobain.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.SaleTransaction
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

    /**
     * @Update, not @Insert(REPLACE): Room enables foreign key constraints, and in
     * SQLite an INSERT OR REPLACE deletes the conflicting row before re-inserting,
     * which fires ON DELETE SET NULL on sales.itemId. Using REPLACE here silently
     * wiped the item link from every past sale each time stock changed at checkout.
     * insertItem keeps REPLACE on purpose - LegacyDataMigrator upserts with explicit ids.
     */
    @Update
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

    @Query("SELECT * FROM sale_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<SaleTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SaleTransaction)

    /**
     * Reducing stock, writing the transaction header, and recording its line items
     * must land together: a crash or process death partway through would otherwise
     * let some sale rows exist against stock that was never decremented, or leave a
     * header with no lines. The header goes in before the lines so nothing ever
     * reads an orphan line.
     */
    @Transaction
    suspend fun recordSaleTransaction(
        items: List<ThriftItem>,
        transaction: SaleTransaction,
        sales: List<ThriftSale>
    ) {
        items.forEach { updateItem(it) }
        insertTransaction(transaction)
        sales.forEach { insertSale(it) }
    }
}
