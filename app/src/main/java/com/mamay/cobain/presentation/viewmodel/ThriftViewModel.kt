package com.mamay.cobain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.mamay.cobain.data.repository.ThriftItemRepository
import com.mamay.cobain.domain.DiscountType
import com.mamay.cobain.domain.ExcelExportRequest
import com.mamay.cobain.domain.activeDiscountsFor
import com.mamay.cobain.domain.buildExcelReport
import com.mamay.cobain.domain.calculateCheckoutTotals
import com.mamay.cobain.domain.discountedLine
import com.mamay.cobain.domain.effectiveDiscount
import com.mamay.cobain.domain.isDiscountChoiceRequired
import com.mamay.cobain.util.formatRupiah
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

/**
 * Eagerly (not WhileSubscribed/Lazily): every tab reads from these on every launch,
 * and business actions (addItem, checkout, ...) read categories/attributes/discounts
 * .value directly, so the data must already be live the moment the ViewModel is
 * created, not only once a composable first subscribes.
 */
class ThriftViewModel(
    private val repository: ThriftItemRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    val items: StateFlow<List<ThriftItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories: StateFlow<List<ItemCategory>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val attributes: StateFlow<List<ItemAttribute>> = repository.allAttributes
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val attributeOptions: StateFlow<List<ItemAttributeOption>> = repository.allAttributeOptions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val attributeValues: StateFlow<List<ItemAttributeValue>> = repository.allAttributeValues
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val discounts: StateFlow<List<Discount>> = repository.allDiscounts
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val discountItems: StateFlow<List<DiscountItem>> = repository.allDiscountItems
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sales: StateFlow<List<ThriftSale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val transactions: StateFlow<List<SaleTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val storeProfile: StateFlow<StoreProfile> = repository.storeProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, StoreProfile())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** One-shot "operation succeeded" text, shown as a modal dialog by MainScreen. */
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart.asStateFlow()

    private val _lastReceipt = MutableStateFlow<ReceiptData?>(null)
    val lastReceipt: StateFlow<ReceiptData?> = _lastReceipt.asStateFlow()

    /**
     * True until the core Room flows have each emitted once, so the UI can show a
     * loading screen over the first (empty) frame on a cold start. combine emits
     * only after every source has produced a value. Retained across config changes
     * with the ViewModel, so a warm return from background shows no loading.
     */
    val isInitializing: StateFlow<Boolean> = combine(
        repository.allItems,
        repository.allCategories,
        repository.allTransactions,
        repository.storeProfile
    ) { _, _, _, _ -> false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val itemTerm: String get() = storeProfile.value.itemTerm.ifBlank { StoreProfile.DEFAULT_ITEM_TERM }

    fun consumeReceipt() {
        _lastReceipt.value = null
    }

    fun consumeErrorMessage() {
        _errorMessage.value = null
    }

    fun consumeSuccessMessage() {
        _successMessage.value = null
    }

    /** Called by the UI after AppContainer.resetStore() succeeds. */
    fun notifyStoreReset() {
        _successMessage.value = "Toko berhasil direset. Mulai dari awal."
    }

    // --- Items -------------------------------------------------------------

    fun addItem(
        name: String,
        categoryId: Int?,
        quantity: Int,
        buyPrice: Int,
        sellPrice: Int,
        attributeValues: Map<Int, String>
    ) {
        val validationError = validateItemInput(name, quantity, buyPrice, sellPrice, attributeValues)
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        viewModelScope.launch {
            repository.insert(
                ThriftItem(
                    name = name.trim(),
                    categoryId = categoryId,
                    quantity = quantity,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    isSold = false
                ),
                attributeValues
            ).onFailure(::reportFailure)
        }
    }

    fun updateItem(item: ThriftItem, attributeValues: Map<Int, String>) {
        val validationError = validateItemInput(item.name, item.quantity, item.buyPrice, item.sellPrice, attributeValues)
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }
        viewModelScope.launch {
            repository.update(item, attributeValues).onFailure(::reportFailure)
        }
    }

    fun deleteItem(item: ThriftItem) {
        viewModelScope.launch {
            repository.delete(item)
                .onSuccess { _successMessage.value = "$itemTerm berhasil dihapus" }
                .onFailure(::reportFailure)
        }
    }

    fun toggleSoldStatus(item: ThriftItem) {
        viewModelScope.launch {
            repository.update(item.copy(isSold = !item.isSold), currentAttributeValues(item.id))
                .onFailure(::reportFailure)
        }
    }

    fun setItemDefaultDiscount(itemId: Int, discountId: Int?) {
        viewModelScope.launch {
            repository.setItemDefaultDiscount(itemId, discountId).onFailure(::reportFailure)
        }
    }

    // --- Categories -------------------------------------------------------

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
            repository.deleteCategory(category)
                .onSuccess { _successMessage.value = "Kategori berhasil dihapus" }
                .onFailure(::reportFailure)
        }
    }

    // --- Custom attributes ----------------------------------------------

    fun addAttribute(name: String, mode: AttributeMode, required: Boolean) {
        if (name.isBlank()) {
            _errorMessage.value = "Nama atribut tidak boleh kosong"
            return
        }
        if (attributes.value.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            _errorMessage.value = "Atribut \"$name\" sudah ada"
            return
        }
        viewModelScope.launch {
            repository.insertAttribute(name.trim(), mode, required).onFailure(::reportFailure)
        }
    }

    fun updateAttribute(attribute: ItemAttribute) {
        viewModelScope.launch {
            repository.updateAttribute(attribute).onFailure(::reportFailure)
        }
    }

    fun deleteAttribute(attribute: ItemAttribute) {
        viewModelScope.launch {
            repository.deleteAttribute(attribute)
                .onSuccess { _successMessage.value = "Atribut berhasil dihapus" }
                .onFailure(::reportFailure)
        }
    }

    fun addAttributeOption(attributeId: Int, value: String) {
        if (value.isBlank()) {
            _errorMessage.value = "Nilai tidak boleh kosong"
            return
        }
        val existing = attributeOptions.value.filter { it.attributeId == attributeId }
        if (existing.any { it.value.equals(value.trim(), ignoreCase = true) }) {
            _errorMessage.value = "Nilai \"$value\" sudah ada"
            return
        }
        viewModelScope.launch {
            repository.addAttributeOption(attributeId, value.trim()).onFailure(::reportFailure)
        }
    }

    fun deleteAttributeOption(option: ItemAttributeOption) {
        viewModelScope.launch {
            repository.deleteAttributeOption(option)
                .onSuccess { _successMessage.value = "Pilihan berhasil dihapus" }
                .onFailure(::reportFailure)
        }
    }

    // --- Discounts -----------------------------------------------------

    /**
     * For each of [itemIds], any OTHER discount whose window overlaps
     * [startMillis, endMillis). The editor uses this to make the admin pick a
     * default per item before two discounts can both be active.
     */
    fun discountConflicts(
        startMillis: Long,
        endMillis: Long,
        itemIds: List<Int>,
        excludeDiscountId: Int? = null
    ): Map<Int, List<Discount>> {
        val linksByItem = discountItems.value.groupBy { it.itemId }
        val discountById = discounts.value.associateBy { it.id }
        return itemIds.associateWith { itemId ->
            (linksByItem[itemId] ?: emptyList())
                .mapNotNull { discountById[it.discountId] }
                .filter { it.id != excludeDiscountId && windowsOverlap(startMillis, endMillis, it.startMillis, it.endMillis) }
        }.filterValues { it.isNotEmpty() }
    }

    fun addDiscount(label: String, percent: Int, startMillis: Long, endMillis: Long, itemIds: List<Int>) {
        val error = validateDiscountInput(percent, startMillis, endMillis, itemIds)
        if (error != null) {
            _errorMessage.value = error
            return
        }
        viewModelScope.launch {
            repository.saveDiscount(label.trim(), percent, startMillis, endMillis, itemIds.distinct())
                .onFailure(::reportFailure)
        }
    }

    fun updateDiscount(discount: Discount, itemIds: List<Int>) {
        val error = validateDiscountInput(discount.percent, discount.startMillis, discount.endMillis, itemIds)
        if (error != null) {
            _errorMessage.value = error
            return
        }
        viewModelScope.launch {
            repository.updateDiscount(
                discount.copy(label = discount.label.trim()),
                itemIds.distinct()
            ).onFailure(::reportFailure)
        }
    }

    fun deleteDiscount(discount: Discount) {
        viewModelScope.launch {
            repository.deleteDiscount(discount)
                .onSuccess { _successMessage.value = "Diskon berhasil dihapus" }
                .onFailure(::reportFailure)
        }
    }

    /** Bulk delete (the "bersihkan diskon kadaluarsa" action): one success message, not N. */
    fun deleteDiscounts(discounts: List<Discount>) {
        if (discounts.isEmpty()) return
        viewModelScope.launch {
            discounts.forEach { repository.deleteDiscount(it).onFailure(::reportFailure) }
            _successMessage.value = "${discounts.size} diskon kadaluarsa dihapus"
        }
    }

    // --- Store profile -------------------------------------------------

    fun saveStoreProfile(
        storeName: String,
        address: String,
        phone: String,
        receiptFooter: String,
        itemTerm: String,
        lowStockThreshold: Int
    ) {
        if (storeName.isBlank()) {
            _errorMessage.value = "Nama toko tidak boleh kosong"
            return
        }
        viewModelScope.launch {
            repository.saveStoreProfile(
                StoreProfile(
                    storeName = storeName.trim(),
                    address = address.trim(),
                    phone = phone.trim(),
                    receiptFooter = receiptFooter.trim(),
                    itemTerm = itemTerm.trim().ifBlank { StoreProfile.DEFAULT_ITEM_TERM },
                    lowStockThreshold = lowStockThreshold.coerceAtLeast(0)
                )
            ).onFailure(::reportFailure)
        }
    }

    // --- Excel export ----------------------------------------------

    /**
     * Builds the requested .xlsx off the main thread from the current in-memory
     * snapshots (no extra DAO round-trips) and writes it to [out]. The caller
     * (a SAF document stream) owns and closes [out].
     */
    suspend fun writeExcelReport(request: ExcelExportRequest, out: OutputStream): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                buildExcelReport(
                    out = out,
                    request = request,
                    itemTerm = itemTerm,
                    attributes = attributes.value,
                    attributeValues = attributeValues.value,
                    items = items.value,
                    categories = categories.value,
                    sales = sales.value,
                    transactions = transactions.value
                )
            }
        }

    // --- Cart --------------------------------------------------------

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

    /** Cashier's per-line pick among the active discounts for an item. */
    fun setCartLineDiscount(item: ThriftItem, discountId: Int?) {
        _cart.value = _cart.value.map {
            if (it.item.id == item.id) it.copy(overrideDiscountId = discountId) else it
        }
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
     * with its SaleTransaction header.
     *
     * Per-item discounts are resolved against `timestamp` and baked into each
     * line's sellPrice/totalPrice; the original price and percent are snapshotted
     * for reporting. The transaction-level discount is then applied on top of the
     * already-discounted subtotal.
     *
     * The subtotal is computed from the lines that actually survived that re-read,
     * not from the cart, so a discount is never applied to a bill the customer is
     * not paying. An underpayment aborts without clearing the cart: the cashier has
     * to be able to correct the amount, not start the whole sale over.
     */
    fun checkout(discountType: DiscountType, discountValue: Int, paidAmount: Int) {
        val lines = _cart.value
        if (lines.isEmpty()) {
            _errorMessage.value = "Keranjang masih kosong"
            return
        }
        val latestItemById = items.value.associateBy { it.id }
        val discountsNow = discounts.value
        val linksNow = discountItems.value
        val timestamp = System.currentTimeMillis()

        val unresolved = lines.firstOrNull { line ->
            val latest = latestItemById[line.item.id] ?: return@firstOrNull false
            val active = activeDiscountsFor(latest.id, discountsNow, linksNow, timestamp)
            isDiscountChoiceRequired(latest, active, line.overrideDiscountId)
        }
        if (unresolved != null) {
            _errorMessage.value = "Pilih diskon untuk \"${unresolved.item.name}\" sebelum checkout"
            return
        }

        val transactionId = UUID.randomUUID().toString()
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
            val active = activeDiscountsFor(latest.id, discountsNow, linksNow, timestamp)
            val effective = effectiveDiscount(latest, active, line.overrideDiscountId)
            val priced = discountedLine(latest, effective)
            sales += ThriftSale(
                transactionId = transactionId,
                itemId = latest.id,
                itemName = latest.name,
                size = sizeValueFor(latest.id),
                category = categories.value.find { it.id == latest.categoryId }?.name ?: "",
                quantity = soldQuantity,
                sellPrice = priced.unitFinal,
                totalPrice = priced.unitFinal * soldQuantity,
                timestamp = timestamp,
                buyPrice = latest.buyPrice,
                attributesSummary = attributeSummaryFor(latest.id),
                originalSellPrice = priced.unitOriginal,
                discountPercent = priced.percent
            )
        }
        if (sales.isEmpty()) {
            _errorMessage.value = "Barang di keranjang sudah tidak tersedia"
            _cart.value = emptyList()
            return
        }
        val subtotal = sales.sumOf { it.totalPrice }
        val totals = calculateCheckoutTotals(subtotal, discountType, discountValue, paidAmount)
        if (!totals.isPaidEnough) {
            _errorMessage.value =
                "Uang yang dibayar kurang ${formatRupiah(totals.total - paidAmount.coerceAtLeast(0))}"
            return
        }
        val transaction = SaleTransaction(
            id = transactionId,
            timestamp = timestamp,
            subtotal = totals.subtotal,
            discountType = discountType.name,
            discountValue = discountValue,
            discountAmount = totals.discountAmount,
            total = totals.total,
            paidAmount = paidAmount.coerceAtLeast(0),
            changeAmount = totals.changeAmount
        )
        viewModelScope.launch {
            repository.recordSaleTransaction(updatedItems, transaction, sales)
                .onSuccess {
                    _cart.value = emptyList()
                    _lastReceipt.value = ReceiptData(transaction, sales)
                    _successMessage.value =
                        "Transaksi ${formatRupiah(transaction.total)} berhasil disimpan"
                }
                .onFailure(::reportFailure)
        }
    }

    // --- Helpers ---------------------------------------------------

    private fun currentAttributeValues(itemId: Int): Map<Int, String> =
        attributeValues.value.filter { it.itemId == itemId }.associate { it.attributeId to it.value }

    private fun attributeSummaryFor(itemId: Int): String {
        val valueByAttr = currentAttributeValues(itemId)
        return attributes.value
            .sortedBy { it.displayOrder }
            .mapNotNull { attr -> valueByAttr[attr.id]?.takeIf { it.isNotBlank() } }
            .joinToString(" · ")
    }

    private fun sizeValueFor(itemId: Int): String {
        val ukuran = attributes.value.firstOrNull { it.name.equals("Ukuran", ignoreCase = true) }
            ?: return ""
        return currentAttributeValues(itemId)[ukuran.id].orEmpty()
    }

    private fun validateItemInput(
        name: String,
        quantity: Int,
        buyPrice: Int,
        sellPrice: Int,
        attributeValues: Map<Int, String>
    ): String? {
        when {
            name.isBlank() -> return "Nama $itemTerm tidak boleh kosong"
            quantity <= 0 -> return "Jumlah harus lebih dari 0"
            buyPrice < 0 -> return "Harga beli tidak boleh negatif"
            sellPrice < 0 -> return "Harga jual tidak boleh negatif"
        }
        val missing = attributes.value.firstOrNull { attr ->
            attr.required && attributeValues[attr.id].orEmpty().isBlank()
        }
        if (missing != null) return "${missing.name} wajib diisi"
        return null
    }

    private fun validateDiscountInput(
        percent: Int,
        startMillis: Long,
        endMillis: Long,
        itemIds: List<Int>
    ): String? = when {
        percent !in 1..100 -> "Persen diskon harus antara 1 dan 100"
        endMillis <= startMillis -> "Tanggal selesai harus setelah tanggal mulai"
        itemIds.isEmpty() -> "Pilih minimal satu $itemTerm untuk diskon ini"
        else -> null
    }

    private fun windowsOverlap(aStart: Long, aEnd: Long, bStart: Long, bEnd: Long): Boolean =
        aStart < bEnd && bStart < aEnd

    private fun reportFailure(e: Throwable) {
        _errorMessage.value = "Gagal menyimpan data: ${e.message ?: "terjadi kesalahan tak terduga"}"
    }
}

data class CartLine(
    val item: ThriftItem,
    val quantity: Int,
    val overrideDiscountId: Int? = null
)

/** One completed checkout, held just long enough for the receipt dialog to show it. */
data class ReceiptData(val transaction: SaleTransaction, val lines: List<ThriftSale>)
