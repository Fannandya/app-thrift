package com.mamay.cobain.presentation.viewmodel

import com.mamay.cobain.data.entity.AttributeMode
import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.DiscountItem
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.repository.FakeThriftItemRepository
import com.mamay.cobain.domain.DiscountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThriftViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeThriftItemRepository
    private lateinit var viewModel: ThriftViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeThriftItemRepository()
        viewModel = ThriftViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addItem with valid input inserts the item`() = runTest {
        viewModel.addItem("Kemeja Flanel", categoryId = 1, quantity = 2, buyPrice = 10_000, sellPrice = 25_000, attributeValues = emptyMap())
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.items.value.size)
        assertEquals("Kemeja Flanel", viewModel.items.value.first().name)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addItem persists attribute values`() = runTest {
        repository.seedAttribute(ItemAttribute(id = 1, name = "Ukuran", mode = AttributeMode.LIST.name, displayOrder = 0))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addItem("Kaos", categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 10_000, attributeValues = mapOf(1 to "M"))
        dispatcher.scheduler.advanceUntilIdle()

        val itemId = viewModel.items.value.first().id
        assertEquals("M", viewModel.attributeValues.value.first { it.itemId == itemId && it.attributeId == 1 }.value)
    }

    @Test
    fun `addItem with blank name is rejected without touching the repository`() = runTest {
        viewModel.addItem("   ", categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 0, attributeValues = emptyMap())
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.items.value.isEmpty())
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addItem missing a required attribute is rejected`() = runTest {
        repository.seedAttribute(ItemAttribute(id = 1, name = "Ukuran", mode = AttributeMode.LIST.name, required = true, displayOrder = 0))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addItem("Kaos", categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 10_000, attributeValues = emptyMap())
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.items.value.isEmpty())
        assertEquals("Ukuran wajib diisi", viewModel.errorMessage.value)
    }

    @Test
    fun `addToCart clamps to available stock and reports an error past the limit`() = runTest {
        val item = ThriftItem(id = 1, name = "Jaket", categoryId = 1, quantity = 2, buyPrice = 20_000, sellPrice = 50_000)
        repository.seedItem(item)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(item)
        viewModel.addToCart(item)
        viewModel.addToCart(item)

        assertEquals(2, viewModel.cart.value.first().quantity)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `updateCartQuantity of zero removes the line from the cart`() = runTest {
        val item = ThriftItem(id = 1, name = "Rok", categoryId = null, quantity = 3, buyPrice = 5_000, sellPrice = 15_000)
        repository.seedItem(item)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(item)
        viewModel.updateCartQuantity(item, 0)

        assertTrue(viewModel.cart.value.isEmpty())
    }

    @Test
    fun `checkout with multiple cart lines reduces stock for every item and shares one transaction`() = runTest {
        val jaket = ThriftItem(id = 1, name = "Jaket", categoryId = 1, quantity = 2, buyPrice = 20_000, sellPrice = 50_000)
        val kaos = ThriftItem(id = 2, name = "Kaos", categoryId = 1, quantity = 5, buyPrice = 8_000, sellPrice = 20_000)
        repository.seedItem(jaket)
        repository.seedItem(kaos)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(jaket)
        viewModel.addToCart(jaket)
        viewModel.addToCart(kaos)
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 120_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.items.value.first { it.id == 1 }.quantity)
        assertTrue(viewModel.items.value.first { it.id == 1 }.isSold)
        assertEquals(4, viewModel.items.value.first { it.id == 2 }.quantity)
        assertEquals(2, viewModel.sales.value.size)
        val transactionIds = viewModel.sales.value.map { it.transactionId }.distinct()
        assertEquals(1, transactionIds.size)
        assertEquals(120_000, viewModel.sales.value.sumOf { it.totalPrice })
        assertTrue(viewModel.cart.value.isEmpty())
    }

    @Test
    fun `checkout snapshots buy price and attribute summary on each sale line`() = runTest {
        repository.seedAttribute(ItemAttribute(id = 1, name = "Ukuran", mode = AttributeMode.LIST.name, displayOrder = 0))
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", categoryId = null, quantity = 1, buyPrice = 20_000, sellPrice = 50_000))
        repository.seedAttributeValue(ItemAttributeValue(id = 1, itemId = 1, attributeId = 1, value = "L"))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(viewModel.items.value.first())
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 50_000)
        dispatcher.scheduler.advanceUntilIdle()

        val sale = viewModel.sales.value.single()
        assertEquals(20_000, sale.buyPrice)
        assertEquals("L", sale.attributesSummary)
        assertEquals("L", sale.size)
        assertEquals(0, sale.discountPercent)
        assertEquals(sale.sellPrice, sale.originalSellPrice)
    }

    @Test
    fun `checkout applies an active per-item discount to the sale line`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Kaos", categoryId = null, quantity = 1, buyPrice = 8_000, sellPrice = 20_000))
        repository.seedDiscount(Discount(id = 1, percent = 25, startMillis = 0, endMillis = Long.MAX_VALUE))
        repository.seedDiscountLink(DiscountItem(id = 1, discountId = 1, itemId = 1))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(viewModel.items.value.first())
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 20_000)
        dispatcher.scheduler.advanceUntilIdle()

        val sale = viewModel.sales.value.single()
        assertEquals(20_000, sale.originalSellPrice)
        assertEquals(25, sale.discountPercent)
        assertEquals(15_000, sale.sellPrice)
        assertEquals(15_000, sale.totalPrice)
    }

    @Test
    fun `checkout is blocked when a line has two active discounts and none is chosen`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Kaos", categoryId = null, quantity = 1, buyPrice = 8_000, sellPrice = 20_000))
        repository.seedDiscount(Discount(id = 1, percent = 10, startMillis = 0, endMillis = Long.MAX_VALUE))
        repository.seedDiscount(Discount(id = 2, percent = 20, startMillis = 0, endMillis = Long.MAX_VALUE))
        repository.seedDiscountLink(DiscountItem(id = 1, discountId = 1, itemId = 1))
        repository.seedDiscountLink(DiscountItem(id = 2, discountId = 2, itemId = 1))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(viewModel.items.value.first())
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 20_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.sales.value.isEmpty())
        assertNotNull(viewModel.errorMessage.value)

        viewModel.setCartLineDiscount(viewModel.items.value.first(), 2)
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 20_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(16_000, viewModel.sales.value.single().sellPrice)
    }

    @Test
    fun `checkout with an empty cart is rejected without touching the repository`() = runTest {
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 0)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.sales.value.isEmpty())
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `toggleSoldStatus flips the flag`() = runTest {
        val item = ThriftItem(id = 1, name = "Rok", categoryId = null, quantity = 1, buyPrice = 5_000, sellPrice = 15_000)
        repository.seedItem(item)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSoldStatus(item)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.items.value.first { it.id == 1 }.isSold)
    }

    @Test
    fun `addCategory rejects a case-insensitive duplicate name`() = runTest {
        repository.seedCategory(ItemCategory(id = 1, name = "Atasan"))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addCategory("atasan")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.categories.value.size)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addAttribute rejects a duplicate name and addAttributeOption rejects a duplicate value`() = runTest {
        repository.seedAttribute(ItemAttribute(id = 1, name = "Warna", mode = AttributeMode.LIST.name, displayOrder = 0))
        repository.seedAttributeOption(ItemAttributeOption(id = 1, attributeId = 1, value = "Merah", displayOrder = 0))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addAttribute("warna", AttributeMode.FREE_TEXT, required = false)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.attributes.value.size)
        assertNotNull(viewModel.errorMessage.value)
        viewModel.consumeErrorMessage()

        viewModel.addAttributeOption(1, "merah")
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.attributeOptions.value.size)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addDiscount validates percent, dates and item selection`() = runTest {
        viewModel.addDiscount("", percent = 0, startMillis = 0, endMillis = 10, itemIds = listOf(1))
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.discounts.value.isEmpty())
        assertNotNull(viewModel.errorMessage.value)
        viewModel.consumeErrorMessage()

        viewModel.addDiscount("", percent = 10, startMillis = 100, endMillis = 100, itemIds = listOf(1))
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.discounts.value.isEmpty())
        viewModel.consumeErrorMessage()

        viewModel.addDiscount("", percent = 10, startMillis = 0, endMillis = 100, itemIds = emptyList())
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.discounts.value.isEmpty())
        viewModel.consumeErrorMessage()

        viewModel.addDiscount("Promo", percent = 10, startMillis = 0, endMillis = 100, itemIds = listOf(1))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.discounts.value.size)
    }

    @Test
    fun `discountConflicts reports overlapping discounts for a linked item`() = runTest {
        repository.seedDiscount(Discount(id = 1, percent = 10, startMillis = 0, endMillis = 100))
        repository.seedDiscountLink(DiscountItem(id = 1, discountId = 1, itemId = 7))
        dispatcher.scheduler.advanceUntilIdle()

        val conflicts = viewModel.discountConflicts(startMillis = 50, endMillis = 150, itemIds = listOf(7))
        assertEquals(listOf(1), conflicts[7]?.map { it.id })
    }

    @Test
    fun `a failed repository call surfaces an error message instead of failing silently`() = runTest {
        repository.failNextCall = true

        viewModel.addItem("Celana", categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 10_000, attributeValues = emptyMap())
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.items.value.isEmpty())
        assertFalse(viewModel.errorMessage.value.isNullOrBlank())
    }

    @Test
    fun `saveStoreProfile with blank name is rejected`() = runTest {
        viewModel.saveStoreProfile("  ", "Jl. Merdeka", "0812", "Terima kasih", "Barang", 2)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.storeProfile.value.storeName)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `saveStoreProfile trims, falls back on blank item term and persists the threshold`() = runTest {
        viewModel.saveStoreProfile("  Toko Berkah  ", " Jl. Merdeka 12 ", " 0812 ", " Terima kasih ", "   ", -3)
        dispatcher.scheduler.advanceUntilIdle()

        val profile = viewModel.storeProfile.value
        assertEquals("Toko Berkah", profile.storeName)
        assertEquals("Jl. Merdeka 12", profile.address)
        assertEquals("0812", profile.phone)
        assertEquals("Terima kasih", profile.receiptFooter)
        assertEquals("Barang", profile.itemTerm)
        assertEquals(0, profile.lowStockThreshold)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `checkout with insufficient payment is rejected and keeps the cart`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", categoryId = null, quantity = 2, buyPrice = 20_000, sellPrice = 50_000))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.addToCart(viewModel.items.value.first())

        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 10_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.transactions.value.isEmpty())
        assertEquals(1, viewModel.cart.value.size)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `checkout records discount and change on the transaction`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", categoryId = null, quantity = 2, buyPrice = 20_000, sellPrice = 50_000))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.addToCart(viewModel.items.value.first())

        viewModel.checkout(DiscountType.PERCENT, 10, paidAmount = 100_000)
        dispatcher.scheduler.advanceUntilIdle()

        val transaction = viewModel.transactions.value.single()
        assertEquals(50_000, transaction.subtotal)
        assertEquals(5_000, transaction.discountAmount)
        assertEquals(45_000, transaction.total)
        assertEquals(100_000, transaction.paidAmount)
        assertEquals(55_000, transaction.changeAmount)
        assertEquals(DiscountType.PERCENT.name, transaction.discountType)
        assertTrue(viewModel.cart.value.isEmpty())
        assertNotNull(viewModel.lastReceipt.value)
    }

    @Test
    fun `checkout discounts only the lines that survived the live stock re-read`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", categoryId = null, quantity = 1, buyPrice = 20_000, sellPrice = 50_000))
        repository.seedItem(ThriftItem(id = 2, name = "Kaos", categoryId = null, quantity = 1, buyPrice = 8_000, sellPrice = 30_000))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.addToCart(viewModel.items.value.first { it.id == 1 })
        viewModel.addToCart(viewModel.items.value.first { it.id == 2 })

        viewModel.deleteItem(viewModel.items.value.first { it.id == 2 })
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.checkout(DiscountType.AMOUNT, 5_000, paidAmount = 50_000)
        dispatcher.scheduler.advanceUntilIdle()

        val transaction = viewModel.transactions.value.single()
        assertEquals(50_000, transaction.subtotal)
        assertEquals(45_000, transaction.total)
        assertEquals(5_000, transaction.changeAmount)
    }

    @Test
    fun `isInitializing starts true and flips false once the core flows have emitted`() = runTest {
        assertTrue(viewModel.isInitializing.value)
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.isInitializing.value)
    }

    @Test
    fun `delete operations surface a success message that can be consumed`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 1))
        repository.seedCategory(ItemCategory(id = 1, name = "Atasan"))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteItem(viewModel.items.value.first())
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Barang berhasil dihapus", viewModel.successMessage.value)

        viewModel.consumeSuccessMessage()
        assertNull(viewModel.successMessage.value)

        viewModel.deleteCategory(viewModel.categories.value.first())
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Kategori berhasil dihapus", viewModel.successMessage.value)
    }

    @Test
    fun `deleting an attribute option reports success`() = runTest {
        repository.seedAttribute(
            com.mamay.cobain.data.entity.ItemAttribute(id = 1, name = "Warna", mode = "LIST", displayOrder = 0)
        )
        repository.seedAttributeOption(ItemAttributeOption(id = 1, attributeId = 1, value = "Merah", displayOrder = 0))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteAttributeOption(viewModel.attributeOptions.value.first())
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Pilihan berhasil dihapus", viewModel.successMessage.value)
    }

    @Test
    fun `deleteDiscounts removes all and reports a single count message`() = runTest {
        repository.seedDiscount(Discount(id = 1, percent = 10, startMillis = 0, endMillis = 5))
        repository.seedDiscount(Discount(id = 2, percent = 20, startMillis = 0, endMillis = 5))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteDiscounts(viewModel.discounts.value)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.discounts.value.isEmpty())
        assertEquals("2 diskon kadaluarsa dihapus", viewModel.successMessage.value)
    }

    @Test
    fun `checkout reports a success message with the total`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", categoryId = null, quantity = 1, buyPrice = 20_000, sellPrice = 50_000))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.addToCart(viewModel.items.value.first())

        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 50_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Transaksi Rp50.000 berhasil disimpan", viewModel.successMessage.value)
        assertNotNull(viewModel.lastReceipt.value)
    }

    @Test
    fun `notifyStoreReset raises a success message`() = runTest {
        viewModel.notifyStoreReset()
        assertEquals("Toko berhasil direset. Mulai dari awal.", viewModel.successMessage.value)
        viewModel.consumeSuccessMessage()
        assertNull(viewModel.successMessage.value)
    }

    @Test
    fun `consumeReceipt clears the receipt so it does not reappear`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", categoryId = null, quantity = 1, buyPrice = 20_000, sellPrice = 50_000))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.addToCart(viewModel.items.value.first())
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 50_000)
        dispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.lastReceipt.value)

        viewModel.consumeReceipt()

        assertNull(viewModel.lastReceipt.value)
    }
}
