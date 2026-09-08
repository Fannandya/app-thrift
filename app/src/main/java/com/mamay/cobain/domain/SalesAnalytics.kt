package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** One day's takings, ready to plot or list. */
data class DailySales(
    val dayStartMillis: Long,
    val label: String,
    val total: Long
)

/**
 * Groups transactions into [days] consecutive daily buckets ending today, oldest
 * first, zero-filling days with no sales so a chart keeps an even x-axis instead of
 * silently collapsing quiet days.
 *
 * Buckets are advanced with Calendar.add(DAY_OF_YEAR) rather than by adding
 * 86_400_000 milliseconds: on a day that changes offset (DST) a "day" is not 24
 * hours, and fixed-width arithmetic would drift the boundaries.
 *
 * The time zone is a parameter, not TimeZone.getDefault(), because a shop's day ends
 * at midnight in the shop's own zone - and because it makes the boundaries testable.
 *
 * Uses transaction.total (after discount), never the sum of line prices, so the
 * chart and the dashboard's revenue figure can never disagree.
 */
fun dailySalesSeries(
    transactions: List<SaleTransaction>,
    days: Int,
    now: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
): List<DailySales> {
    if (days <= 0) return emptyList()

    val labelFormat = SimpleDateFormat("dd/MM", Locale.US).apply { this.timeZone = timeZone }

    // Awal hari ini, lalu mundur (days - 1) hari untuk mendapat awal jendela.
    val cursor = Calendar.getInstance(timeZone).apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -(days - 1))
    }

    val buckets = ArrayList<DailySales>(days)
    val boundaries = ArrayList<Long>(days + 1)
    repeat(days) {
        val start = cursor.timeInMillis
        boundaries += start
        buckets += DailySales(start, labelFormat.format(Date(start)), 0L)
        cursor.add(Calendar.DAY_OF_YEAR, 1)
    }
    boundaries += cursor.timeInMillis

    val totals = LongArray(days)
    for (transaction in transactions) {
        if (transaction.timestamp < boundaries.first() || transaction.timestamp >= boundaries.last()) continue
        // Cari bucket lewat batas yang sudah dihitung, bukan lewat pembagian, supaya
        // hari dengan panjang tidak 24 jam tetap masuk ke hari kalendernya sendiri.
        val index = boundaries.indexOfLast { it <= transaction.timestamp }.coerceIn(0, days - 1)
        totals[index] += transaction.total.toLong()
    }

    return buckets.mapIndexed { index, bucket -> bucket.copy(total = totals[index]) }
}

// --- Dashboard aggregates -------------------------------------------------------
//
// All take lists the caller has already filtered to the selected time range, so the
// same Semua / 7 Hari / 30 Hari chip that drives the chart drives these too.

/** Cost of everything sold in range, from the per-line cost snapshot on [ThriftSale]. */
fun costOfGoodsSold(salesInRange: List<ThriftSale>): Long =
    salesInRange.sumOf { it.buyPrice.toLong() * it.quantity }

/**
 * Revenue after discount (transaction totals, consistent with [dailySalesSeries])
 * minus the cost of goods sold.
 */
fun grossProfit(transactionsInRange: List<SaleTransaction>, salesInRange: List<ThriftSale>): Long =
    transactionsInRange.sumOf { it.total.toLong() } - costOfGoodsSold(salesInRange)

/** Total per-item discount handed out in range: (original - final) * qty per line. */
fun itemDiscountGiven(salesInRange: List<ThriftSale>): Long =
    salesInRange.sumOf { (it.originalSellPrice.toLong() - it.sellPrice) * it.quantity }

/**
 * Percentage change of [current] against [previous]. Returns null when [previous]
 * is not positive - there is no baseline to compare against (e.g. the "Semua"
 * range, or the shop's first week).
 */
fun revenueDelta(current: Long, previous: Long): Double? =
    if (previous <= 0L) null else (current - previous) * 100.0 / previous

data class TransactionStats(val count: Int, val average: Long)

fun transactionStats(transactionsInRange: List<SaleTransaction>): TransactionStats {
    if (transactionsInRange.isEmpty()) return TransactionStats(0, 0)
    val total = transactionsInRange.sumOf { it.total.toLong() }
    return TransactionStats(transactionsInRange.size, total / transactionsInRange.size)
}

data class TopSellingItem(val name: String, val quantity: Int, val revenue: Long)

fun topSellingItems(salesInRange: List<ThriftSale>, limit: Int = 5): List<TopSellingItem> =
    salesInRange
        .groupBy { it.itemName }
        .map { (name, lines) ->
            TopSellingItem(name, lines.sumOf { it.quantity }, lines.sumOf { it.totalPrice.toLong() })
        }
        .sortedWith(compareByDescending<TopSellingItem> { it.quantity }.thenByDescending { it.revenue })
        .take(limit)

data class LowStockReport(val items: List<ThriftItem>, val outOfStockCount: Int)

/** Unsold items at or below [threshold]; [outOfStockCount] is how many of those are at zero. */
fun lowStock(items: List<ThriftItem>, threshold: Int): LowStockReport {
    val low = items
        .filter { !it.isSold && it.quantity <= threshold }
        .sortedBy { it.quantity }
    return LowStockReport(low, low.count { it.quantity <= 0 })
}
