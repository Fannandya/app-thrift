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
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory test double, so ThriftViewModel's business logic (validation, sale
 * math, error surfacing) can be unit-tested on the JVM without Room/SQLite.
 */
class FakeThriftItemRepository : ThriftItemRepository {
    private val itemsFlow = MutableStateFlow<List<ThriftItem>>(emptyList())
    private val categoriesFlow = MutableStateFlow<List<ItemCategory>>(emptyList())
    private val attributesFlow = MutableStateFlow<List<ItemAttribute>>(emptyList())
    private val attributeOptionsFlow = MutableStateFlow<List<ItemAttributeOption>>(emptyList())
    private val attributeValuesFlow = MutableStateFlow<List<ItemAttributeValue>>(emptyList())
    private val discountsFlow = MutableStateFlow<List<Discount>>(emptyList())
    private val discountItemsFlow = MutableStateFlow<List<DiscountItem>>(emptyList())
    private val salesFlow = MutableStateFlow<List<ThriftSale>>(emptyList())
    private val transactionsFlow = MutableStateFlow<List<SaleTransaction>>(emptyList())
    private val storeProfileFlow = MutableStateFlow(StoreProfile())

    /** Set true to make the next mutating call return Result.failure. */
    var failNextCall = false

    override val allItems: Flow<List<ThriftItem>> = itemsFlow
    override val allCategories: Flow<List<ItemCategory>> = categoriesFlow
    override val allAttributes: Flow<List<ItemAttribute>> = attributesFlow
    override val allAttributeOptions: Flow<List<ItemAttributeOption>> = attributeOptionsFlow
    override val allAttributeValues: Flow<List<ItemAttributeValue>> = attributeValuesFlow
    override val allDiscounts: Flow<List<Discount>> = discountsFlow
    override val allDiscountItems: Flow<List<DiscountItem>> = discountItemsFlow
    override val allSales: Flow<List<ThriftSale>> = salesFlow
    override val allTransactions: Flow<List<SaleTransaction>> = transactionsFlow
    override val storeProfile: Flow<StoreProfile> = storeProfileFlow

    override suspend fun insert(item: ThriftItem, attributeValues: Map<Int, String>): Result<Unit> = mutate {
        val newId = (itemsFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        itemsFlow.value = itemsFlow.value + item.copy(id = newId)
        writeAttributeValues(newId, attributeValues)
    }

    override suspend fun update(item: ThriftItem, attributeValues: Map<Int, String>): Result<Unit> = mutate {
        itemsFlow.value = itemsFlow.value.map { if (it.id == item.id) item else it }
        writeAttributeValues(item.id, attributeValues)
    }

    override suspend fun delete(item: ThriftItem): Result<Unit> = mutate {
        itemsFlow.value = itemsFlow.value.filter { it.id != item.id }
        attributeValuesFlow.value = attributeValuesFlow.value.filter { it.itemId != item.id }
        discountItemsFlow.value = discountItemsFlow.value.filter { it.itemId != item.id }
    }

    override suspend fun setItemDefaultDiscount(itemId: Int, discountId: Int?): Result<Unit> = mutate {
        itemsFlow.value = itemsFlow.value.map {
            if (it.id == itemId) it.copy(defaultDiscountId = discountId) else it
        }
    }

    override suspend fun insertCategory(name: String): Result<Unit> = mutate {
        val newId = (categoriesFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        categoriesFlow.value = categoriesFlow.value + ItemCategory(id = newId, name = name)
    }

    override suspend fun deleteCategory(category: ItemCategory): Result<Unit> = mutate {
        categoriesFlow.value = categoriesFlow.value.filter { it.id != category.id }
    }

    override suspend fun insertAttribute(
        name: String,
        mode: AttributeMode,
        required: Boolean
    ): Result<Unit> = mutate {
        val newId = (attributesFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        val order = (attributesFlow.value.maxOfOrNull { it.displayOrder } ?: -1) + 1
        attributesFlow.value = attributesFlow.value +
            ItemAttribute(id = newId, name = name, mode = mode.name, required = required, displayOrder = order)
    }

    override suspend fun updateAttribute(attribute: ItemAttribute): Result<Unit> = mutate {
        attributesFlow.value = attributesFlow.value.map { if (it.id == attribute.id) attribute else it }
    }

    override suspend fun deleteAttribute(attribute: ItemAttribute): Result<Unit> = mutate {
        attributesFlow.value = attributesFlow.value.filter { it.id != attribute.id }
        attributeOptionsFlow.value = attributeOptionsFlow.value.filter { it.attributeId != attribute.id }
        attributeValuesFlow.value = attributeValuesFlow.value.filter { it.attributeId != attribute.id }
    }

    override suspend fun addAttributeOption(attributeId: Int, value: String): Result<Unit> = mutate {
        val newId = (attributeOptionsFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        val order = (attributeOptionsFlow.value.filter { it.attributeId == attributeId }
            .maxOfOrNull { it.displayOrder } ?: -1) + 1
        attributeOptionsFlow.value = attributeOptionsFlow.value +
            ItemAttributeOption(id = newId, attributeId = attributeId, value = value, displayOrder = order)
    }

    override suspend fun deleteAttributeOption(option: ItemAttributeOption): Result<Unit> = mutate {
        attributeOptionsFlow.value = attributeOptionsFlow.value.filter { it.id != option.id }
    }

    override suspend fun saveDiscount(
        label: String,
        percent: Int,
        startMillis: Long,
        endMillis: Long,
        itemIds: List<Int>
    ): Result<Unit> = mutate {
        val newId = (discountsFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        discountsFlow.value = discountsFlow.value +
            Discount(id = newId, label = label, percent = percent, startMillis = startMillis, endMillis = endMillis)
        var linkId = discountItemsFlow.value.maxOfOrNull { it.id } ?: 0
        discountItemsFlow.value = discountItemsFlow.value +
            itemIds.map { DiscountItem(id = ++linkId, discountId = newId, itemId = it) }
    }

    override suspend fun updateDiscount(discount: Discount, itemIds: List<Int>): Result<Unit> = mutate {
        discountsFlow.value = discountsFlow.value.map { if (it.id == discount.id) discount else it }
        discountItemsFlow.value = discountItemsFlow.value.filter { it.discountId != discount.id }
        var linkId = discountItemsFlow.value.maxOfOrNull { it.id } ?: 0
        discountItemsFlow.value = discountItemsFlow.value +
            itemIds.map { DiscountItem(id = ++linkId, discountId = discount.id, itemId = it) }
    }

    override suspend fun deleteDiscount(discount: Discount): Result<Unit> = mutate {
        discountsFlow.value = discountsFlow.value.filter { it.id != discount.id }
        discountItemsFlow.value = discountItemsFlow.value.filter { it.discountId != discount.id }
        itemsFlow.value = itemsFlow.value.map {
            if (it.defaultDiscountId == discount.id) it.copy(defaultDiscountId = null) else it
        }
    }

    override suspend fun saveStoreProfile(profile: StoreProfile): Result<Unit> = mutate {
        storeProfileFlow.value = profile
    }

    override suspend fun recordSaleTransaction(
        items: List<ThriftItem>,
        transaction: SaleTransaction,
        sales: List<ThriftSale>
    ): Result<Unit> = mutate {
        val updatedById = items.associateBy { it.id }
        itemsFlow.value = itemsFlow.value.map { updatedById[it.id] ?: it }
        transactionsFlow.value = transactionsFlow.value + transaction
        var nextId = (salesFlow.value.maxOfOrNull { it.id } ?: 0)
        salesFlow.value = salesFlow.value + sales.map { sale -> sale.copy(id = ++nextId) }
    }

    fun seedItem(item: ThriftItem) {
        itemsFlow.value = itemsFlow.value + item
    }

    fun seedCategory(category: ItemCategory) {
        categoriesFlow.value = categoriesFlow.value + category
    }

    fun seedAttribute(attribute: ItemAttribute) {
        attributesFlow.value = attributesFlow.value + attribute
    }

    fun seedAttributeOption(option: ItemAttributeOption) {
        attributeOptionsFlow.value = attributeOptionsFlow.value + option
    }

    fun seedAttributeValue(value: ItemAttributeValue) {
        attributeValuesFlow.value = attributeValuesFlow.value + value
    }

    fun seedDiscount(discount: Discount) {
        discountsFlow.value = discountsFlow.value + discount
    }

    fun seedDiscountLink(link: DiscountItem) {
        discountItemsFlow.value = discountItemsFlow.value + link
    }

    private fun writeAttributeValues(itemId: Int, values: Map<Int, String>) {
        var nextId = attributeValuesFlow.value.maxOfOrNull { it.id } ?: 0
        val kept = attributeValuesFlow.value.filter { it.itemId != itemId }
        val added = values.mapNotNull { (attributeId, raw) ->
            raw.trim().takeIf { it.isNotEmpty() }?.let {
                ItemAttributeValue(id = ++nextId, itemId = itemId, attributeId = attributeId, value = it)
            }
        }
        attributeValuesFlow.value = kept + added
    }

    private inline fun mutate(block: () -> Unit): Result<Unit> {
        if (failNextCall) {
            failNextCall = false
            return Result.failure(IllegalStateException("simulated failure"))
        }
        block()
        return Result.success(Unit)
    }
}
