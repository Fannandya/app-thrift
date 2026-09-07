package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.SaleTransaction
import org.junit.Assert.assertEquals
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
}
