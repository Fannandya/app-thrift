package com.mamay.cobain.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.DiscountItem
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ItemCategory
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

    // --- Categories -----------------------------------------------------------

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ItemCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ItemCategory): Long

    @Delete
    suspend fun deleteCategory(category: ItemCategory)

    // --- Custom attributes (Fitur A) ---------------------------------------

    @Query("SELECT * FROM item_attributes ORDER BY displayOrder ASC, id ASC")
    fun getAllAttributes(): Flow<List<ItemAttribute>>

    @Query("SELECT * FROM item_attribute_options ORDER BY displayOrder ASC, value ASC")
    fun getAllAttributeOptions(): Flow<List<ItemAttributeOption>>

    @Query("SELECT * FROM item_attribute_values")
    fun getAllAttributeValues(): Flow<List<ItemAttributeValue>>

    @Query("SELECT * FROM item_attributes WHERE name = :name LIMIT 1")
    suspend fun findAttributeByName(name: String): ItemAttribute?

    @Query("SELECT COALESCE(MAX(displayOrder), -1) + 1 FROM item_attributes")
    suspend fun nextAttributeDisplayOrder(): Int

    @Query("SELECT COALESCE(MAX(displayOrder), -1) + 1 FROM item_attribute_options WHERE attributeId = :attributeId")
    suspend fun nextOptionDisplayOrder(attributeId: Int): Int

    @Insert
    suspend fun insertAttribute(attribute: ItemAttribute): Long

    @Update
    suspend fun updateAttribute(attribute: ItemAttribute)

    @Delete
    suspend fun deleteAttribute(attribute: ItemAttribute)

    @Insert
    suspend fun insertAttributeOption(option: ItemAttributeOption): Long

    @Delete
    suspend fun deleteAttributeOption(option: ItemAttributeOption)

    @Query("DELETE FROM item_attribute_values WHERE itemId = :itemId")
    suspend fun clearAttributeValues(itemId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttributeValues(values: List<ItemAttributeValue>)

    /**
     * Insert an item and its attribute values atomically. [valuesFor] receives the
     * generated item id so callers can key their [ItemAttributeValue] list to it.
     */
    @Transaction
    suspend fun insertItemWithAttributes(
        item: ThriftItem,
        valuesFor: (Int) -> List<ItemAttributeValue>
    ): Int {
        val id = insertItem(item).toInt()
        val values = valuesFor(id)
        if (values.isNotEmpty()) insertAttributeValues(values)
        return id
    }

    @Transaction
    suspend fun updateItemWithAttributes(item: ThriftItem, values: List<ItemAttributeValue>) {
        updateItem(item)
        clearAttributeValues(item.id)
        if (values.isNotEmpty()) insertAttributeValues(values)
    }

    // --- Discounts (Fitur F) ---------------------------------------------------

    @Query("SELECT * FROM discounts ORDER BY startMillis DESC, id DESC")
    fun getAllDiscounts(): Flow<List<Discount>>

    @Query("SELECT * FROM discount_items")
    fun getAllDiscountItems(): Flow<List<DiscountItem>>

    @Insert
    suspend fun insertDiscount(discount: Discount): Long

    @Update
    suspend fun updateDiscount(discount: Discount)

    @Delete
    suspend fun deleteDiscount(discount: Discount)

    @Query("DELETE FROM discount_items WHERE discountId = :discountId")
    suspend fun clearDiscountLinks(discountId: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDiscountLinks(links: List<DiscountItem>)

    @Query("UPDATE items SET defaultDiscountId = :discountId WHERE id = :itemId")
    suspend fun setItemDefaultDiscount(itemId: Int, discountId: Int?)

    @Transaction
    suspend fun insertDiscountWithLinks(discount: Discount, itemIds: List<Int>): Int {
        val id = insertDiscount(discount).toInt()
        if (itemIds.isNotEmpty()) {
            insertDiscountLinks(itemIds.map { DiscountItem(discountId = id, itemId = it) })
        }
        return id
    }

    @Transaction
    suspend fun updateDiscountWithLinks(discount: Discount, itemIds: List<Int>) {
        updateDiscount(discount)
        clearDiscountLinks(discount.id)
        if (itemIds.isNotEmpty()) {
            insertDiscountLinks(itemIds.map { DiscountItem(discountId = discount.id, itemId = it) })
        }
    }

    // --- Sales ---------------------------------------------------------------

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
