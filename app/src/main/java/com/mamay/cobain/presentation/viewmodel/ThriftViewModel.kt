package com.mamay.cobain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import com.mamay.cobain.data.repository.ThriftItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Eagerly (not WhileSubscribed/Lazily): every tab reads from these on every launch,
 * and business actions (addItem, recordSale, ...) read categories/sizes.value
 * directly, so the data must already be live the moment the ViewModel is created,
 * not only once a composable first subscribes.
 */
class ThriftViewModel(private val repository: ThriftItemRepository) : ViewModel() {
    val items: StateFlow<List<ThriftItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories: StateFlow<List<ItemCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sizes: StateFlow<List<ItemSize>> = repository.allSizes
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sales: StateFlow<List<ThriftSale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun consumeErrorMessage() {
        _errorMessage.value = null
    }

    fun addItem(
        name: String,
        sizeId: Int?,
        categoryId: Int?,
        quantity: Int,
        buyPrice: Int,
        sellPrice: Int
    ) {
        val validationError = validateItemInput(name, sizeId, quantity, buyPrice, sellPrice)
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        viewModelScope.launch {
            val result = repository.insert(
                ThriftItem(
                    name = name.trim(),
                    sizeId = sizeId,
                    categoryId = categoryId,
                    quantity = quantity,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    isSold = false
                )
            )
            result.onFailure(::reportFailure)
        }
    }

    fun updateItem(item: ThriftItem) {
        val validationError = validateItemInput(item.name, item.sizeId, item.quantity, item.buyPrice, item.sellPrice)
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        viewModelScope.launch {
            repository.update(item).onFailure(::reportFailure)
        }
    }

    fun deleteItem(item: ThriftItem) {
        viewModelScope.launch {
            repository.delete(item).onFailure(::reportFailure)
        }
    }

    fun toggleSoldStatus(item: ThriftItem) {
        viewModelScope.launch {
            repository.update(item.copy(isSold = !item.isSold)).onFailure(::reportFailure)
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) {
            _errorMessage.value = "Nama kategori tidak boleh kosong"
            return
        }
        if (categories.value.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            _errorMessage.value = "Kategori \"$name\" sudah ada"
            return
        }
        viewModelScope.launch {
            repository.insertCategory(name.trim()).onFailure(::reportFailure)
        }
    }

    fun deleteCategory(category: ItemCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category).onFailure(::reportFailure)
        }
    }

    fun addSize(name: String) {
        if (name.isBlank()) {
            _errorMessage.value = "Nama ukuran tidak boleh kosong"
            return
        }
        if (sizes.value.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            _errorMessage.value = "Ukuran \"$name\" sudah ada"
            return
        }
        viewModelScope.launch {
            repository.insertSize(name.trim()).onFailure(::reportFailure)
        }
    }

    fun deleteSize(size: ItemSize) {
        viewModelScope.launch {
            repository.deleteSize(size).onFailure(::reportFailure)
        }
    }

    fun recordSale(item: ThriftItem, quantity: Int) {
        if (quantity <= 0) {
            _errorMessage.value = "Jumlah yang terjual harus lebih dari 0"
            return
        }
        val soldQuantity = quantity.coerceAtMost(item.quantity)
        val updatedItem = item.copy(
            quantity = item.quantity - soldQuantity,
            isSold = item.isSold || (item.quantity - soldQuantity) == 0
        )
        val categoryName = categories.value.find { it.id == item.categoryId }?.name ?: ""
        val sizeName = sizes.value.find { it.id == item.sizeId }?.name ?: ""
        viewModelScope.launch {
            repository.recordSale(
                updatedItem = updatedItem,
                sale = ThriftSale(
                    itemId = item.id,
                    itemName = item.name,
                    size = sizeName,
                    category = categoryName,
                    quantity = soldQuantity,
                    sellPrice = item.sellPrice,
                    totalPrice = item.sellPrice * soldQuantity,
                    timestamp = System.currentTimeMillis()
                )
            ).onFailure(::reportFailure)
        }
    }

    private fun validateItemInput(
        name: String,
        sizeId: Int?,
        quantity: Int,
        buyPrice: Int,
        sellPrice: Int
    ): String? = when {
        name.isBlank() -> "Nama barang tidak boleh kosong"
        sizeId == null -> "Ukuran harus dipilih"
        quantity <= 0 -> "Jumlah harus lebih dari 0"
        buyPrice < 0 -> "Harga beli tidak boleh negatif"
        sellPrice < 0 -> "Harga jual tidak boleh negatif"
        else -> null
    }

    private fun reportFailure(e: Throwable) {
        _errorMessage.value = "Gagal menyimpan data: ${e.message ?: "terjadi kesalahan tak terduga"}"
    }
}
