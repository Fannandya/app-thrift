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
    fun recordSaleTransactionUpdatesAllStocksAndInsertsAllSalesTogether() = runBlocking {
        val sizeId = dao.insertSize(ItemSize(name = "L")).toInt()
        val jaketId = dao.insertItem(
            ThriftItem(name = "Jaket", sizeId = sizeId, categoryId = null, quantity = 3, buyPrice = 20_000, sellPrice = 50_000)
        ).toInt()
        val kaosId = dao.insertItem(
            ThriftItem(name = "Kaos", sizeId = sizeId, categoryId = null, quantity = 5, buyPrice = 8_000, sellPrice = 20_000)
        ).toInt()
        val jaket = dao.getItemById(jaketId)!!
        val kaos = dao.getItemById(kaosId)!!
        val transactionId = "txn-1"
        val timestamp = System.currentTimeMillis()

        dao.recordSaleTransaction(
            items = listOf(
                jaket.copy(quantity = jaket.quantity - 1),
                kaos.copy(quantity = kaos.quantity - 2)
            ),
            sales = listOf(
                ThriftSale(
                    transactionId = transactionId,
                    itemId = jaketId,
                    itemName = jaket.name,
                    size = "L",
                    category = "",
                    quantity = 1,
                    sellPrice = jaket.sellPrice,
                    totalPrice = jaket.sellPrice,
                    timestamp = timestamp
                ),
                ThriftSale(
                    transactionId = transactionId,
                    itemId = kaosId,
                    itemName = kaos.name,
                    size = "L",
                    category = "",
                    quantity = 2,
                    sellPrice = kaos.sellPrice,
                    totalPrice = kaos.sellPrice * 2,
                    timestamp = timestamp
                )
            )
        )

        assertEquals(2, dao.getItemById(jaketId)?.quantity)
        assertEquals(3, dao.getItemById(kaosId)?.quantity)
        val sales = dao.getAllSales().first()
        assertEquals(2, sales.size)
        assertEquals(1, sales.map { it.transactionId }.distinct().size)
    }
}
