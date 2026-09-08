package com.mamay.cobain.data.repository

import com.mamay.cobain.data.entity.AttributeMode
import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.DiscountItem
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.StoreProfile
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
    val allAttributes: Flow<List<ItemAttribute>>
    val allAttributeOptions: Flow<List<ItemAttributeOption>>
    val allAttributeValues: Flow<List<ItemAttributeValue>>
    val allDiscounts: Flow<List<Discount>>
    val allDiscountItems: Flow<List<DiscountItem>>
    val allSales: Flow<List<ThriftSale>>
    val allTransactions: Flow<List<SaleTransaction>>
    val storeProfile: Flow<StoreProfile>

    /** [attributeValues] maps an attribute id to its raw value; blanks are dropped. */
    suspend fun insert(item: ThriftItem, attributeValues: Map<Int, String>): Result<Unit>
    suspend fun update(item: ThriftItem, attributeValues: Map<Int, String>): Result<Unit>
    suspend fun delete(item: ThriftItem): Result<Unit>
    suspend fun setItemDefaultDiscount(itemId: Int, discountId: Int?): Result<Unit>

    suspend fun insertCategory(name: String): Result<Unit>
    suspend fun deleteCategory(category: ItemCategory): Result<Unit>

    suspend fun insertAttribute(name: String, mode: AttributeMode, required: Boolean): Result<Unit>
    suspend fun updateAttribute(attribute: ItemAttribute): Result<Unit>
    suspend fun deleteAttribute(attribute: ItemAttribute): Result<Unit>
    suspend fun addAttributeOption(attributeId: Int, value: String): Result<Unit>
    suspend fun deleteAttributeOption(option: ItemAttributeOption): Result<Unit>

    suspend fun saveDiscount(
        label: String,
        percent: Int,
        startMillis: Long,
        endMillis: Long,
        itemIds: List<Int>
    ): Result<Unit>

    suspend fun updateDiscount(discount: Discount, itemIds: List<Int>): Result<Unit>
    suspend fun deleteDiscount(discount: Discount): Result<Unit>

    suspend fun saveStoreProfile(profile: StoreProfile): Result<Unit>

    suspend fun recordSaleTransaction(
        items: List<ThriftItem>,
        transaction: SaleTransaction,
        sales: List<ThriftSale>
    ): Result<Unit>
}
