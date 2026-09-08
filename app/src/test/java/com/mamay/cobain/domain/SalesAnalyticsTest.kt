package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SalesAnalyticsTest {

    private val jakarta = TimeZone.getTimeZone("Asia/Jakarta")

    /** 10 September 2026, 10:00 waktu Jakarta. */
    private val now = Calendar.getInstance(jakarta).apply {
        clear()
        set(2026, Calendar.SEPTEMBER, 10, 10, 0, 0)
    }.timeInMillis

    private fun txAt(year: Int, month: Int, day: Int, hour: Int, minute: Int, total: Int): SaleTransaction {
        val millis = Calendar.getInstance(jakarta).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }.timeInMillis
        return SaleTransaction(
            id = "tx-$year$month$day$hour$minute", timestamp = millis, subtotal = total,
            discountType = "NONE", discountValue = 0, discountAmount = 0,
            total = total, paidAmount = total, changeAmount = 0
        )
    }

    @Test
    fun `always returns exactly the requested number of buckets even with no sales`() {
        val series = dailySalesSeries(emptyList(), days = 7, now = now, timeZone = jakarta)

        assertEquals(7, series.size)
        assertTrue(series.all { it.total == 0L })
    }

    @Test
    fun `buckets run oldest first and end on today`() {
        val series = dailySalesSeries(emptyList(), days = 7, now = now, timeZone = jakarta)

        assertEquals(listOf("04/09", "05/09", "06/09", "07/09", "08/09", "09/09", "10/09"), series.map { it.label })
    }

    @Test
    fun `two sales on the same calendar day land in one bucket`() {
        val series = dailySalesSeries(
            listOf(
                txAt(2026, Calendar.SEPTEMBER, 9, 8, 0, 50_000),
                txAt(2026, Calendar.SEPTEMBER, 9, 20, 0, 30_000)
            ),
            days = 7, now = now, timeZone = jakarta
        )

        assertEquals(80_000L, series.single { it.label == "09/09" }.total)
    }

    @Test
    fun `sales older than the window are excluded`() {
        val series = dailySalesSeries(
            listOf(txAt(2026, Calendar.SEPTEMBER, 1, 12, 0, 999_000)),
            days = 7, now = now, timeZone = jakarta
        )

        assertTrue(series.all { it.total == 0L })
    }

    @Test
    fun `2359 and 0001 fall into different buckets in the given zone`() {
        // Inilah alasan zona waktunya diinjeksi, bukan diambil dari sistem: batas
        // hari sebuah toko adalah tengah malam waktu toko, bukan waktu UTC.
        val series = dailySalesSeries(
            listOf(
                txAt(2026, Calendar.SEPTEMBER, 8, 23, 59, 10_000),
                txAt(2026, Calendar.SEPTEMBER, 9, 0, 1, 20_000)
            ),
            days = 7, now = now, timeZone = jakarta
        )

        assertEquals(10_000L, series.single { it.label == "08/09" }.total)
        assertEquals(20_000L, series.single { it.label == "09/09" }.total)
    }

    @Test
    fun `uses the transaction total so discounts are not counted as revenue`() {
        val discounted = txAt(2026, Calendar.SEPTEMBER, 10, 9, 0, 100_000)
            .copy(subtotal = 100_000, discountAmount = 40_000, total = 60_000)

        val series = dailySalesSeries(listOf(discounted), days = 7, now = now, timeZone = jakarta)

        assertEquals(60_000L, series.single { it.label == "10/09" }.total)
    }

    // --- Dashboard aggregates -------------------------------------------------

    private fun sale(name: String, qty: Int, buy: Int, sell: Int, original: Int = sell): ThriftSale =
        ThriftSale(
            transactionId = "t", itemId = null, itemName = name, size = "", category = "",
            quantity = qty, sellPrice = sell, totalPrice = sell * qty, timestamp = now,
            buyPrice = buy, originalSellPrice = original,
            discountPercent = if (original > sell) 100 - sell * 100 / original else 0
        )

    @Test
    fun `grossProfit is post-discount revenue minus cost of goods sold`() {
        val txs = listOf(txAt(2026, Calendar.SEPTEMBER, 10, 9, 0, 100_000).copy(total = 90_000))
        val sales = listOf(sale("Kaos", qty = 2, buy = 10_000, sell = 20_000))

        assertEquals(20_000L, costOfGoodsSold(sales))
        assertEquals(70_000L, grossProfit(txs, sales))
    }

    @Test
    fun `transactionStats returns zero for an empty range and integer-divides otherwise`() {
        assertEquals(TransactionStats(0, 0), transactionStats(emptyList()))

        val txs = listOf(
            txAt(2026, Calendar.SEPTEMBER, 10, 9, 0, 0).copy(total = 10_000),
            txAt(2026, Calendar.SEPTEMBER, 10, 10, 0, 0).copy(total = 25_000)
        )
        assertEquals(TransactionStats(2, 17_500), transactionStats(txs))
    }

    @Test
    fun `topSellingItems ranks by quantity then revenue and honours the limit`() {
        val sales = listOf(
            sale("A", qty = 5, buy = 0, sell = 1_000),
            sale("B", qty = 5, buy = 0, sell = 3_000),
            sale("C", qty = 9, buy = 0, sell = 1_000)
        )
        val top = topSellingItems(sales, limit = 2)
        assertEquals(listOf("C", "B"), top.map { it.name })
    }

    @Test
    fun `itemDiscountGiven totals the per-line saving`() {
        val sales = listOf(
            sale("Kaos", qty = 2, buy = 0, sell = 8_000, original = 10_000),
            sale("Topi", qty = 1, buy = 0, sell = 5_000)
        )
        assertEquals(4_000L, itemDiscountGiven(sales))
    }

    @Test
    fun `revenueDelta is percent change, or null without a positive baseline`() {
        assertEquals(25.0, revenueDelta(current = 125_000, previous = 100_000)!!, 0.001)
        assertEquals(-20.0, revenueDelta(current = 80_000, previous = 100_000)!!, 0.001)
        assertNull(revenueDelta(current = 50_000, previous = 0))
        assertNull(revenueDelta(current = 50_000, previous = -10))
    }

    @Test
    fun `lowStock excludes sold items, respects the boundary and counts zeroes`() {
        val items = listOf(
            ThriftItem(id = 1, name = "A", categoryId = null, quantity = 0, buyPrice = 0, sellPrice = 0),
            ThriftItem(id = 2, name = "B", categoryId = null, quantity = 2, buyPrice = 0, sellPrice = 0),
            ThriftItem(id = 3, name = "C", categoryId = null, quantity = 3, buyPrice = 0, sellPrice = 0),
            ThriftItem(id = 4, name = "D", categoryId = null, quantity = 1, buyPrice = 0, sellPrice = 0, isSold = true)
        )
        val report = lowStock(items, threshold = 2)
        assertEquals(listOf("A", "B"), report.items.map { it.name })
        assertEquals(1, report.outOfStockCount)
    }
}
