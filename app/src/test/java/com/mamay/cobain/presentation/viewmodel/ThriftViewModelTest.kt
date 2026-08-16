package com.mamay.cobain.presentation.viewmodel

import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.repository.FakeThriftItemRepository
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
    fun `recordSale reduces stock and marks item sold when stock reaches zero`() = runTest {
        val item = ThriftItem(id = 1, name = "Jaket", sizeId = 1, categoryId = 1, quantity = 2, buyPrice = 20_000, sellPrice = 50_000)
        repository.seedItem(item)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.recordSale(item, quantity = 2)
        dispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.items.value.first { it.id == 1 }
        assertEquals(0, updated.quantity)
        assertTrue(updated.isSold)
        assertEquals(1, viewModel.sales.value.size)
        assertEquals(100_000, viewModel.sales.value.first().totalPrice)
    }

    @Test
    fun `recordSale coerces quantity to available stock instead of overselling`() = runTest {
        val item = ThriftItem(id = 1, name = "Jaket", sizeId = 1, categoryId = 1, quantity = 2, buyPrice = 20_000, sellPrice = 50_000)
        repository.seedItem(item)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.recordSale(item, quantity = 5)
        dispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.items.value.first { it.id == 1 }
        assertEquals(0, updated.quantity)
        assertEquals(2, viewModel.sales.value.first().quantity)
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
}
