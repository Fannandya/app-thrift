package com.mamay.cobain.presentation.viewmodel

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
        viewModel.addItem("Kemeja Flanel", sizeId = 1, categoryId = 1, quantity = 2, buyPrice = 10_000, sellPrice = 25_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.items.value.size)
        assertEquals("Kemeja Flanel", viewModel.items.value.first().name)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addItem with blank name is rejected without touching the repository`() = runTest {
        viewModel.addItem("   ", sizeId = 1, categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 0)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.items.value.isEmpty())
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addItem with no size selected is rejected`() = runTest {
        viewModel.addItem("Kaos", sizeId = null, categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 10_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.items.value.isEmpty())
        assertEquals("Ukuran harus dipilih", viewModel.errorMessage.value)
    }

    @Test
    fun `addToCart clamps to available stock and reports an error past the limit`() = runTest {
        val item = ThriftItem(id = 1, name = "Jaket", sizeId = 1, categoryId = 1, quantity = 2, buyPrice = 20_000, sellPrice = 50_000)
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
        val item = ThriftItem(id = 1, name = "Rok", sizeId = 1, categoryId = null, quantity = 3, buyPrice = 5_000, sellPrice = 15_000)
        repository.seedItem(item)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.addToCart(item)
        viewModel.updateCartQuantity(item, 0)

        assertTrue(viewModel.cart.value.isEmpty())
    }

    @Test
    fun `checkout with multiple cart lines reduces stock for every item and shares one transaction`() = runTest {
        val jaket = ThriftItem(id = 1, name = "Jaket", sizeId = 1, categoryId = 1, quantity = 2, buyPrice = 20_000, sellPrice = 50_000)
        val kaos = ThriftItem(id = 2, name = "Kaos", sizeId = 1, categoryId = 1, quantity = 5, buyPrice = 8_000, sellPrice = 20_000)
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
    fun `checkout with an empty cart is rejected without touching the repository`() = runTest {
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 0)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.sales.value.isEmpty())
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `toggleSoldStatus flips the flag`() = runTest {
        val item = ThriftItem(id = 1, name = "Rok", sizeId = 1, categoryId = null, quantity = 1, buyPrice = 5_000, sellPrice = 15_000)
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
    fun `a failed repository call surfaces an error message instead of failing silently`() = runTest {
        repository.failNextCall = true

        viewModel.addItem("Celana", sizeId = 1, categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 10_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.items.value.isEmpty())
        assertFalse(viewModel.errorMessage.value.isNullOrBlank())
    }

    @Test
    fun `saveStoreProfile with blank name is rejected`() = runTest {
        viewModel.saveStoreProfile("  ", "Jl. Merdeka", "0812", "Terima kasih")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.storeProfile.value.storeName)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `saveStoreProfile trims and persists the profile`() = runTest {
        viewModel.saveStoreProfile("  Toko Berkah  ", " Jl. Merdeka 12 ", " 0812 ", " Terima kasih ")
        dispatcher.scheduler.advanceUntilIdle()

        val profile = viewModel.storeProfile.value
        assertEquals("Toko Berkah", profile.storeName)
        assertEquals("Jl. Merdeka 12", profile.address)
        assertEquals("0812", profile.phone)
        assertEquals("Terima kasih", profile.receiptFooter)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `checkout with insufficient payment is rejected and keeps the cart`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", sizeId = 1, categoryId = null, quantity = 2, buyPrice = 20_000, sellPrice = 50_000))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.addToCart(viewModel.items.value.first())

        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 10_000)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.transactions.value.isEmpty())
        // Keranjang sengaja dipertahankan: kasir harus bisa memperbaiki nominalnya.
        assertEquals(1, viewModel.cart.value.size)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `checkout records discount and change on the transaction`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", sizeId = 1, categoryId = null, quantity = 2, buyPrice = 20_000, sellPrice = 50_000))
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
        // Barang kedua habis setelah masuk keranjang: diskon tidak boleh dihitung dari
        // tagihan yang tidak jadi dibayar pembeli.
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", sizeId = 1, categoryId = null, quantity = 1, buyPrice = 20_000, sellPrice = 50_000))
        repository.seedItem(ThriftItem(id = 2, name = "Kaos", sizeId = 1, categoryId = null, quantity = 1, buyPrice = 8_000, sellPrice = 30_000))
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
    fun `consumeReceipt clears the receipt so it does not reappear`() = runTest {
        repository.seedItem(ThriftItem(id = 1, name = "Jaket", sizeId = 1, categoryId = null, quantity = 1, buyPrice = 20_000, sellPrice = 50_000))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.addToCart(viewModel.items.value.first())
        viewModel.checkout(DiscountType.NONE, 0, paidAmount = 50_000)
        dispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.lastReceipt.value)

        viewModel.consumeReceipt()

        assertNull(viewModel.lastReceipt.value)
    }
}
