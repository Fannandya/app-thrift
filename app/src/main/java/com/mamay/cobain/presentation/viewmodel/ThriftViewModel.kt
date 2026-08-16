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
import java.util.UUID

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

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart.asStateFlow()

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

    /**
     * One tap adds one unit, clamped to live stock, so the cashier can't add more of
     * an item than is actually on the shelf.
     */
    fun addToCart(item: ThriftItem) {
        val currentQuantity = _cart.value.find { it.item.id == item.id }?.quantity ?: 0
        if (currentQuantity >= item.quantity) {
            _errorMessage.value = "Stok \"${item.name}\" tidak cukup"
            return
        }
        setCartQuantity(item, currentQuantity + 1)
    }

    fun updateCartQuantity(item: ThriftItem, quantity: Int) {
        setCartQuantity(item, quantity)
    }

    fun removeFromCart(item: ThriftItem) {
        _cart.value = _cart.value.filter { it.item.id != item.id }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    private fun setCartQuantity(item: ThriftItem, quantity: Int) {
        val clamped = quantity.coerceIn(0, item.quantity)
        _cart.value = if (clamped <= 0) {
            _cart.value.filter { it.item.id != item.id }
        } else if (_cart.value.any { it.item.id == item.id }) {
            _cart.value.map { if (it.item.id == item.id) it.copy(quantity = clamped) else it }
        } else {
            _cart.value + CartLine(item, clamped)
        }
    }

    /**
     * Re-reads live stock instead of trusting the quantities captured when items
     * were added to the cart, in case stock changed (edited/deleted) in the
     * meantime. Every line item of the checkout shares one transactionId/timestamp
     * so the dashboard can show it as a single transaction.
     */
    fun checkout() {
        val lines = _cart.value
        if (lines.isEmpty()) {
            _errorMessage.value = "Keranjang masih kosong"
            return
        }
        val latestItemById = items.value.associateBy { it.id }
        val transactionId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val updatedItems = mutableListOf<ThriftItem>()
        val sales = mutableListOf<ThriftSale>()
        for (line in lines) {
            val latest = latestItemById[line.item.id] ?: continue
            val soldQuantity = line.quantity.coerceIn(0, latest.quantity)
            if (soldQuantity <= 0) continue
            updatedItems += latest.copy(
                quantity = latest.quantity - soldQuantity,
                isSold = latest.isSold || (latest.quantity - soldQuantity) == 0
            )
            sales += ThriftSale(
                transactionId = transactionId,
                itemId = latest.id,
                itemName = latest.name,
                size = sizes.value.find { it.id == latest.sizeId }?.name ?: "",
                category = categories.value.find { it.id == latest.categoryId }?.name ?: "",
                quantity = soldQuantity,
                sellPrice = latest.sellPrice,
                totalPrice = latest.sellPrice * soldQuantity,
                timestamp = timestamp
            )
        }
        if (sales.isEmpty()) {
            _errorMessage.value = "Barang di keranjang sudah tidak tersedia"
            _cart.value = emptyList()
            return
        }
        viewModelScope.launch {
            repository.recordSaleTransaction(updatedItems, sales)
                .onSuccess { _cart.value = emptyList() }
                .onFailure(::reportFailure)
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

data class CartLine(val item: ThriftItem, val quantity: Int)
