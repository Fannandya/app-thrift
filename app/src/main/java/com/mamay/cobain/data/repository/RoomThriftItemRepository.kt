package com.mamay.cobain.data.repository

import com.mamay.cobain.data.dao.StoreProfileDao
import com.mamay.cobain.data.dao.ThriftItemDao
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
    override val allAttributes: Flow<List<ItemAttribute>> = dao.getAllAttributes()
    override val allAttributeOptions: Flow<List<ItemAttributeOption>> = dao.getAllAttributeOptions()
    override val allAttributeValues: Flow<List<ItemAttributeValue>> = dao.getAllAttributeValues()
    override val allDiscounts: Flow<List<Discount>> = dao.getAllDiscounts()
    override val allDiscountItems: Flow<List<DiscountItem>> = dao.getAllDiscountItems()
    override val allSales: Flow<List<ThriftSale>> = dao.getAllSales()
    override val allTransactions: Flow<List<SaleTransaction>> = dao.getAllTransactions()

    // A fresh install creates store_profile from Room's own schema, not from
    // MIGRATION_2_3, so the seeded row is not there and the query emits null.
    // Mapping to a blank profile keeps every consumer free of null handling.
    override val storeProfile: Flow<StoreProfile> =
        storeProfileDao.observeProfile().map { it ?: StoreProfile() }

    override suspend fun insert(item: ThriftItem, attributeValues: Map<Int, String>): Result<Unit> = safeCall {
        dao.insertItemWithAttributes(item) { id -> attributeValueRows(id, attributeValues) }
    }

    override suspend fun update(item: ThriftItem, attributeValues: Map<Int, String>): Result<Unit> = safeCall {
        dao.updateItemWithAttributes(item, attributeValueRows(item.id, attributeValues))
    }

    override suspend fun delete(item: ThriftItem): Result<Unit> = safeCall {
        dao.deleteItem(item)
    }

    override suspend fun setItemDefaultDiscount(itemId: Int, discountId: Int?): Result<Unit> = safeCall {
        dao.setItemDefaultDiscount(itemId, discountId)
    }

    override suspend fun insertCategory(name: String): Result<Unit> = safeCall {
        dao.insertCategory(ItemCategory(name = name))
    }

    override suspend fun deleteCategory(category: ItemCategory): Result<Unit> = safeCall {
        dao.deleteCategory(category)
    }

    override suspend fun insertAttribute(
        name: String,
        mode: AttributeMode,
        required: Boolean
    ): Result<Unit> = safeCall {
        dao.insertAttribute(
            ItemAttribute(
                name = name,
                mode = mode.name,
                required = required,
                displayOrder = dao.nextAttributeDisplayOrder()
            )
        )
    }

    override suspend fun updateAttribute(attribute: ItemAttribute): Result<Unit> = safeCall {
        dao.updateAttribute(attribute)
    }

    override suspend fun deleteAttribute(attribute: ItemAttribute): Result<Unit> = safeCall {
        dao.deleteAttribute(attribute)
    }

    override suspend fun addAttributeOption(attributeId: Int, value: String): Result<Unit> = safeCall {
        dao.insertAttributeOption(
            ItemAttributeOption(
                attributeId = attributeId,
                value = value,
                displayOrder = dao.nextOptionDisplayOrder(attributeId)
            )
        )
    }

    override suspend fun deleteAttributeOption(option: ItemAttributeOption): Result<Unit> = safeCall {
        dao.deleteAttributeOption(option)
    }

    override suspend fun saveDiscount(
        label: String,
        percent: Int,
        startMillis: Long,
        endMillis: Long,
        itemIds: List<Int>
    ): Result<Unit> = safeCall {
        dao.insertDiscountWithLinks(
            Discount(label = label, percent = percent, startMillis = startMillis, endMillis = endMillis),
            itemIds
        )
    }

    override suspend fun updateDiscount(discount: Discount, itemIds: List<Int>): Result<Unit> = safeCall {
        dao.updateDiscountWithLinks(discount, itemIds)
    }

    override suspend fun deleteDiscount(discount: Discount): Result<Unit> = safeCall {
        dao.deleteDiscount(discount)
    }

    override suspend fun saveStoreProfile(profile: StoreProfile): Result<Unit> = safeCall {
        storeProfileDao.upsertProfile(profile.copy(id = StoreProfile.SINGLETON_ID))
    }

    override suspend fun recordSaleTransaction(
        items: List<ThriftItem>,
        transaction: SaleTransaction,
        sales: List<ThriftSale>
    ): Result<Unit> = safeCall {
        dao.recordSaleTransaction(items, transaction, sales)
    }

    private fun attributeValueRows(itemId: Int, values: Map<Int, String>): List<ItemAttributeValue> =
        values.mapNotNull { (attributeId, raw) ->
            raw.trim().takeIf { it.isNotEmpty() }?.let {
                ItemAttributeValue(itemId = itemId, attributeId = attributeId, value = it)
            }
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
