package com.mamay.cobain.data.repository

import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
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
    private val sizesFlow = MutableStateFlow<List<ItemSize>>(emptyList())
    private val salesFlow = MutableStateFlow<List<ThriftSale>>(emptyList())

    /** Set true to make the next mutating call return Result.failure. */
    var failNextCall = false

    override val allItems: Flow<List<ThriftItem>> = itemsFlow
    override val allCategories: Flow<List<ItemCategory>> = categoriesFlow
    override val allSizes: Flow<List<ItemSize>> = sizesFlow
    override val allSales: Flow<List<ThriftSale>> = salesFlow

    override suspend fun insert(item: ThriftItem): Result<Unit> = mutate {
        val newId = (itemsFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        itemsFlow.value = itemsFlow.value + item.copy(id = newId)
    }

    override suspend fun update(item: ThriftItem): Result<Unit> = mutate {
        itemsFlow.value = itemsFlow.value.map { if (it.id == item.id) item else it }
    }

    override suspend fun delete(item: ThriftItem): Result<Unit> = mutate {
        itemsFlow.value = itemsFlow.value.filter { it.id != item.id }
    }

    override suspend fun insertCategory(name: String): Result<Unit> = mutate {
        val newId = (categoriesFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        categoriesFlow.value = categoriesFlow.value + ItemCategory(id = newId, name = name)
    }

    override suspend fun deleteCategory(category: ItemCategory): Result<Unit> = mutate {
        categoriesFlow.value = categoriesFlow.value.filter { it.id != category.id }
    }

    override suspend fun insertSize(name: String): Result<Unit> = mutate {
        val newId = (sizesFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        sizesFlow.value = sizesFlow.value + ItemSize(id = newId, name = name)
    }

    override suspend fun deleteSize(size: ItemSize): Result<Unit> = mutate {
        sizesFlow.value = sizesFlow.value.filter { it.id != size.id }
    }

    override suspend fun recordSale(updatedItem: ThriftItem, sale: ThriftSale): Result<Unit> = mutate {
        itemsFlow.value = itemsFlow.value.map { if (it.id == updatedItem.id) updatedItem else it }
        val newId = (salesFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        salesFlow.value = salesFlow.value + sale.copy(id = newId)
    }

    fun seedItem(item: ThriftItem) {
        itemsFlow.value = itemsFlow.value + item
    }

    fun seedCategory(category: ItemCategory) {
        categoriesFlow.value = categoriesFlow.value + category
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
