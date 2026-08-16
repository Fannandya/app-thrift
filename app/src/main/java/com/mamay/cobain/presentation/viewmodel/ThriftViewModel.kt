package com.mamay.cobain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import com.mamay.cobain.data.repository.ThriftItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ThriftViewModel(private val repository: ThriftItemRepository) : ViewModel() {
    val items: StateFlow<List<ThriftItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<ItemCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sales: StateFlow<List<ThriftSale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addItem(
        name: String,
        size: String,
        category: String,
        quantity: Int,
        buyPrice: Int,
        sellPrice: Int
    ) {
        repository.insert(
            ThriftItem(
                name = name,
                size = size,
                category = category,
                quantity = quantity,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                isSold = false
            )
        )
    }

    fun updateItem(item: ThriftItem) {
        repository.update(item)
    }

    fun deleteItem(item: ThriftItem) {
        repository.delete(item)
    }

    fun toggleSoldStatus(item: ThriftItem) {
        repository.update(item.copy(isSold = !item.isSold))
    }

    fun addCategory(name: String) {
        repository.insertCategory(name)
    }

    fun deleteCategory(category: ItemCategory) {
        repository.deleteCategory(category)
    }

    fun recordSale(item: ThriftItem, quantity: Int) {
        val soldQuantity = quantity.coerceAtMost(item.quantity)
        val updatedItem = item.copy(
            quantity = item.quantity - soldQuantity,
            isSold = item.isSold || (item.quantity - soldQuantity) == 0
        )
        repository.update(updatedItem)
        repository.insertSale(
            ThriftSale(
                itemId = item.id,
                itemName = item.name,
                size = item.size,
                category = item.category,
                quantity = soldQuantity,
                sellPrice = item.sellPrice,
                totalPrice = item.sellPrice * soldQuantity,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}