package com.mamay.cobain.data.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mamay.cobain.data.AppDatabase
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThriftItemDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ThriftItemDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.thriftItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deletingACategoryClearsItsIdOnItemsInsteadOfBlockingOrCascading() = runBlocking {
        val categoryId = dao.insertCategory(ItemCategory(name = "Atasan")).toInt()
        val sizeId = dao.insertSize(ItemSize(name = "M")).toInt()
        val itemId = dao.insertItem(
            ThriftItem(name = "Kemeja", sizeId = sizeId, categoryId = categoryId, buyPrice = 10_000, sellPrice = 20_000)
        ).toInt()

        dao.deleteCategory(ItemCategory(id = categoryId, name = "Atasan"))

        val item = dao.getItemById(itemId)
        assertNull(item?.categoryId)
    }

    @Test
    fun recordSaleUpdatesStockAndInsertsSaleTogether() = runBlocking {
        val sizeId = dao.insertSize(ItemSize(name = "L")).toInt()
        val itemId = dao.insertItem(
            ThriftItem(name = "Jaket", sizeId = sizeId, categoryId = null, quantity = 3, buyPrice = 20_000, sellPrice = 50_000)
        ).toInt()
        val item = dao.getItemById(itemId)!!

        dao.recordSale(
            updatedItem = item.copy(quantity = item.quantity - 1),
            sale = ThriftSale(
                itemId = itemId,
                itemName = item.name,
                size = "L",
                category = "",
                quantity = 1,
                sellPrice = item.sellPrice,
                totalPrice = item.sellPrice,
                timestamp = System.currentTimeMillis()
            )
        )

        assertEquals(2, dao.getItemById(itemId)?.quantity)
        assertEquals(1, dao.getAllSales().first().size)
    }
}
