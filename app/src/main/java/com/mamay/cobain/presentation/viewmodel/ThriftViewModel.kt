package com.mamay.cobain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.repository.ThriftItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ThriftViewModel(private val repository: ThriftItemRepository) : ViewModel() {
    val items: StateFlow<List<ThriftItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addItem(name: String, size: String, buyPrice: Int, sellPrice: Int) {
        repository.insert(
            ThriftItem(
                name = name,
                size = size,
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
}
