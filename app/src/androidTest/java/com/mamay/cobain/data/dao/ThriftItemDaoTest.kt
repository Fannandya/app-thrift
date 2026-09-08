package com.mamay.cobain.data.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mamay.cobain.data.AppDatabase
import com.mamay.cobain.data.entity.AttributeMode
import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.DiscountItem
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        val itemId = dao.insertItem(
            ThriftItem(name = "Kemeja", categoryId = categoryId, buyPrice = 10_000, sellPrice = 20_000)
        ).toInt()

        dao.deleteCategory(ItemCategory(id = categoryId, name = "Atasan"))

        val item = dao.getItemById(itemId)
        assertNull(item?.categoryId)
    }

    @Test
    fun deletingAnItemCascadesItsAttributeValues() = runBlocking {
        val attrId = dao.insertAttribute(
            ItemAttribute(name = "Ukuran", mode = AttributeMode.LIST.name, displayOrder = 0)
        ).toInt()
        val itemId = dao.insertItem(
            ThriftItem(name = "Kaos", categoryId = null, buyPrice = 8_000, sellPrice = 20_000)
        ).toInt()
        dao.insertAttributeValues(listOf(ItemAttributeValue(itemId = itemId, attributeId = attrId, value = "M")))
        assertEquals(1, dao.getAllAttributeValues().first().size)

        dao.deleteItem(dao.getItemById(itemId)!!)

        assertTrue(dao.getAllAttributeValues().first().isEmpty())
    }

    @Test
    fun deletingAnAttributeCascadesOptionsAndValues() = runBlocking {
        val attrId = dao.insertAttribute(
            ItemAttribute(name = "Warna", mode = AttributeMode.LIST.name, displayOrder = 0)
        ).toInt()
        dao.insertAttributeOption(ItemAttributeOption(attributeId = attrId, value = "Merah", displayOrder = 0))
        val itemId = dao.insertItem(
            ThriftItem(name = "Kaos", categoryId = null, buyPrice = 8_000, sellPrice = 20_000)
        ).toInt()
        dao.insertAttributeValues(listOf(ItemAttributeValue(itemId = itemId, attributeId = attrId, value = "Merah")))

        dao.deleteAttribute(ItemAttribute(id = attrId, name = "Warna", mode = AttributeMode.LIST.name))

        assertTrue(dao.getAllAttributeOptions().first().isEmpty())
        assertTrue(dao.getAllAttributeValues().first().isEmpty())
    }

    @Test
    fun deletingADiscountCascadesLinksAndClearsTheDefaultOnItems() = runBlocking {
        val itemId = dao.insertItem(
            ThriftItem(name = "Kaos", categoryId = null, buyPrice = 8_000, sellPrice = 20_000)
        ).toInt()
        val discountId = dao.insertDiscountWithLinks(
            Discount(label = "Promo", percent = 20, startMillis = 0, endMillis = 1_000),
            itemIds = listOf(itemId)
        )
        dao.setItemDefaultDiscount(itemId, discountId)
        assertEquals(1, dao.getAllDiscountItems().first().size)

        dao.deleteDiscount(Discount(id = discountId, label = "Promo", percent = 20, startMillis = 0, endMillis = 1_000))

        assertTrue(dao.getAllDiscountItems().first().isEmpty())
        assertNull(dao.getItemById(itemId)?.defaultDiscountId)
    }

    @Test
    fun deletingAnItemCascadesItsDiscountLinks() = runBlocking {
        val itemId = dao.insertItem(
            ThriftItem(name = "Kaos", categoryId = null, buyPrice = 8_000, sellPrice = 20_000)
        ).toInt()
        dao.insertDiscountWithLinks(
            Discount(label = "Promo", percent = 20, startMillis = 0, endMillis = 1_000),
            itemIds = listOf(itemId)
        )

        dao.deleteItem(dao.getItemById(itemId)!!)

        assertTrue(dao.getAllDiscountItems().first().isEmpty())
    }

    @Test
    fun recordSaleTransactionUpdatesAllStocksAndInsertsAllSalesTogether() = runBlocking {
        val jaketId = dao.insertItem(
            ThriftItem(name = "Jaket", categoryId = null, quantity = 3, buyPrice = 20_000, sellPrice = 50_000)
        ).toInt()
        val kaosId = dao.insertItem(
            ThriftItem(name = "Kaos", categoryId = null, quantity = 5, buyPrice = 8_000, sellPrice = 20_000)
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
            transaction = SaleTransaction(
                id = transactionId,
                timestamp = timestamp,
                subtotal = 90_000,
                discountType = "NONE",
                discountValue = 0,
                discountAmount = 0,
                total = 90_000,
                paidAmount = 100_000,
                changeAmount = 10_000
            ),
            sales = listOf(
                ThriftSale(
                    transactionId = transactionId, itemId = jaketId, itemName = jaket.name, size = "L",
                    category = "", quantity = 1, sellPrice = jaket.sellPrice, totalPrice = jaket.sellPrice,
                    timestamp = timestamp
                ),
                ThriftSale(
                    transactionId = transactionId, itemId = kaosId, itemName = kaos.name, size = "L",
                    category = "", quantity = 2, sellPrice = kaos.sellPrice, totalPrice = kaos.sellPrice * 2,
                    timestamp = timestamp
                )
            )
        )

        assertEquals(2, dao.getItemById(jaketId)?.quantity)
        assertEquals(3, dao.getItemById(kaosId)?.quantity)
        val sales = dao.getAllSales().first()
        assertEquals(2, sales.size)
        assertEquals(1, sales.map { it.transactionId }.distinct().size)

        val transactions = dao.getAllTransactions().first()
        assertEquals(1, transactions.size)
        assertEquals(transactionId, transactions.single().id)
        assertEquals(10_000, transactions.single().changeAmount)
    }

    @Test
    fun updatingAnItemKeepsTheItemIdLinkOnItsExistingSales() = runBlocking {
        val itemId = dao.insertItem(
            ThriftItem(name = "Hoodie", categoryId = null, quantity = 5, buyPrice = 30_000, sellPrice = 90_000)
        ).toInt()
        dao.insertSale(
            ThriftSale(
                transactionId = "txn-lama", itemId = itemId, itemName = "Hoodie", size = "M", category = "",
                quantity = 1, sellPrice = 90_000, totalPrice = 90_000, timestamp = 1_000L
            )
        )

        val item = dao.getItemById(itemId)!!
        dao.updateItem(item.copy(quantity = 4))

        val sales = dao.getAllSales().first()
        assertEquals(itemId, sales.single().itemId)
    }

    @Test
    fun everyCheckoutGetsItsOwnTransactionHeader() = runBlocking {
        val itemId = dao.insertItem(
            ThriftItem(name = "Topi", categoryId = null, quantity = 10, buyPrice = 5_000, sellPrice = 15_000)
        ).toInt()

        repeat(2) { index ->
            val item = dao.getItemById(itemId)!!
            dao.recordSaleTransaction(
                items = listOf(item.copy(quantity = item.quantity - 1)),
                transaction = SaleTransaction(
                    id = "txn-$index", timestamp = 1_000L + index, subtotal = 15_000, discountType = "NONE",
                    discountValue = 0, discountAmount = 0, total = 15_000, paidAmount = 15_000, changeAmount = 0
                ),
                sales = listOf(
                    ThriftSale(
                        transactionId = "txn-$index", itemId = itemId, itemName = "Topi", size = "M", category = "",
                        quantity = 1, sellPrice = 15_000, totalPrice = 15_000, timestamp = 1_000L + index
                    )
                )
            )
        }

        assertEquals(8, dao.getItemById(itemId)?.quantity)
        assertEquals(2, dao.getAllTransactions().first().size)
        assertEquals(listOf(itemId, itemId), dao.getAllSales().first().map { it.itemId })
    }
}
