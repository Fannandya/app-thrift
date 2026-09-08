package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class ExcelSheet { INVENTORY, SALES_BY_ITEM, TRANSACTION_RECAP, PERIOD_SUMMARY }

data class ExcelExportRequest(
    val sheets: Set<ExcelSheet>,
    /** null/null => "Semua" (no time bound). */
    val rangeStartMillis: Long?,
    val rangeEndMillis: Long?
)

/** (sellPrice - buyPrice) * qty for one sold line. */
fun lineProfit(sale: ThriftSale): Long = (sale.sellPrice.toLong() - sale.buyPrice) * sale.quantity

data class PeriodTotals(
    val transactionCount: Int,
    val revenue: Long,
    val cost: Long,
    val grossProfit: Long,
    val itemDiscount: Long,
    val averageTransaction: Long
)

fun periodTotals(
    transactionsInRange: List<SaleTransaction>,
    salesInRange: List<ThriftSale>
): PeriodTotals {
    val revenue = transactionsInRange.sumOf { it.total.toLong() }
    val cost = costOfGoodsSold(salesInRange)
    val count = transactionsInRange.size
    return PeriodTotals(
        transactionCount = count,
        revenue = revenue,
        cost = cost,
        grossProfit = revenue - cost,
        itemDiscount = itemDiscountGiven(salesInRange),
        averageTransaction = if (count == 0) 0 else revenue / count
    )
}

/**
 * Writes an .xlsx workbook to [out]. Pure and parameterised on time zone so it can
 * be asserted in JVM tests, like [buildReceiptText]. The caller owns [out] and is
 * responsible for closing it.
 */
fun buildExcelReport(
    out: OutputStream,
    request: ExcelExportRequest,
    itemTerm: String,
    attributes: List<ItemAttribute>,
    attributeValues: List<ItemAttributeValue>,
    items: List<ThriftItem>,
    categories: List<ItemCategory>,
    sales: List<ThriftSale>,
    transactions: List<SaleTransaction>,
    now: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).apply { this.timeZone = timeZone }
    val dayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { this.timeZone = timeZone }

    fun inRange(ts: Long): Boolean =
        (request.rangeStartMillis == null || ts >= request.rangeStartMillis) &&
            (request.rangeEndMillis == null || ts < request.rangeEndMillis)

    val salesInRange = sales.filter { inRange(it.timestamp) }
    val transactionsInRange = transactions.filter { inRange(it.timestamp) }
    val categoryNameById = categories.associate { it.id to it.name }
    val valuesByItem = attributeValues.groupBy { it.itemId }
        .mapValues { entry -> entry.value.associate { it.attributeId to it.value } }
    val orderedAttributes = attributes.sortedBy { it.displayOrder }
    val rangeLabel = when {
        request.rangeStartMillis == null && request.rangeEndMillis == null -> "Semua data"
        else -> {
            val start = request.rangeStartMillis?.let { dayFormat.format(Date(it)) } ?: "awal"
            val end = request.rangeEndMillis?.let { dayFormat.format(Date(it - 1)) } ?: "kini"
            "$start - $end"
        }
    }

    val wb = Workbook(out, "cobain", "1.0")

    fun header(ws: Worksheet, row: Int, values: List<String>) {
        values.forEachIndexed { col, v ->
            ws.value(row, col, v)
            ws.style(row, col).bold().set()
        }
    }

    if (ExcelSheet.INVENTORY in request.sheets) {
        val ws = wb.newWorksheet("Inventaris")
        val headers = listOf("Nama $itemTerm") + orderedAttributes.map { it.name } +
            listOf("Kategori", "Jumlah", "Harga Beli", "Harga Jual", "Status")
        header(ws, 0, headers)
        items.forEachIndexed { index, item ->
            val row = index + 1
            var col = 0
            ws.value(row, col++, item.name)
            orderedAttributes.forEach { attr ->
                ws.value(row, col++, valuesByItem[item.id]?.get(attr.id).orEmpty())
            }
            ws.value(row, col++, categoryNameById[item.categoryId].orEmpty())
            ws.value(row, col++, item.quantity)
            ws.value(row, col++, item.buyPrice)
            ws.value(row, col++, item.sellPrice)
            ws.value(row, col, if (item.isSold) "Terjual" else "Tersedia")
        }
    }

    if (ExcelSheet.SALES_BY_ITEM in request.sheets) {
        val ws = wb.newWorksheet("Penjualan per Item")
        header(
            ws, 0,
            listOf(
                "Tanggal", "ID Transaksi", "Nama", "Atribut", "Kategori", "Qty",
                "Harga Jual", "Total", "Harga Beli", "Harga Asli", "Diskon %", "Potongan", "Laba"
            )
        )
        salesInRange.forEachIndexed { index, sale ->
            val row = index + 1
            ws.value(row, 0, dateFormat.format(Date(sale.timestamp)))
            ws.value(row, 1, sale.transactionId)
            ws.value(row, 2, sale.itemName)
            ws.value(row, 3, sale.attributesSummary.ifBlank { sale.size })
            ws.value(row, 4, sale.category)
            ws.value(row, 5, sale.quantity)
            ws.value(row, 6, sale.sellPrice)
            ws.value(row, 7, sale.totalPrice)
            ws.value(row, 8, sale.buyPrice)
            ws.value(row, 9, sale.originalSellPrice)
            ws.value(row, 10, sale.discountPercent)
            ws.value(row, 11, (sale.originalSellPrice.toLong() - sale.sellPrice) * sale.quantity)
            ws.value(row, 12, lineProfit(sale))
        }
    }

    if (ExcelSheet.TRANSACTION_RECAP in request.sheets) {
        val ws = wb.newWorksheet("Rekap Transaksi")
        header(
            ws, 0,
            listOf("ID", "Tanggal", "Subtotal", "Diskon", "Total", "Bayar", "Kembali", "Jml Item")
        )
        val itemCountByTx = salesInRange.groupBy { it.transactionId }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
        transactionsInRange.forEachIndexed { index, tx ->
            val row = index + 1
            ws.value(row, 0, tx.id)
            ws.value(row, 1, dateFormat.format(Date(tx.timestamp)))
            ws.value(row, 2, tx.subtotal)
            ws.value(row, 3, tx.discountAmount)
            ws.value(row, 4, tx.total)
            ws.value(row, 5, tx.paidAmount)
            ws.value(row, 6, tx.changeAmount)
            ws.value(row, 7, itemCountByTx[tx.id] ?: 0)
        }
    }

    if (ExcelSheet.PERIOD_SUMMARY in request.sheets) {
        val ws = wb.newWorksheet("Ringkasan")
        val totals = periodTotals(transactionsInRange, salesInRange)
        val rows = listOf(
            "Rentang" to rangeLabel,
            "Jumlah Transaksi" to totals.transactionCount.toString(),
            "Total Pendapatan" to totals.revenue.toString(),
            "Total Modal (HPP)" to totals.cost.toString(),
            "Laba Kotor" to totals.grossProfit.toString(),
            "Total Diskon Barang" to totals.itemDiscount.toString(),
            "Rata-rata Nilai Transaksi" to totals.averageTransaction.toString()
        )
        rows.forEachIndexed { index, (label, value) ->
            ws.value(index, 0, label)
            ws.style(index, 0).bold().set()
            ws.value(index, 1, value)
        }
        var row = rows.size + 1
        ws.value(row, 0, "Barang Terlaris")
        ws.style(row, 0).bold().set()
        row++
        topSellingItems(salesInRange).forEach { top ->
            ws.value(row, 0, top.name)
            ws.value(row, 1, "${top.quantity} terjual")
            ws.value(row, 2, top.revenue)
            row++
        }
    }

    wb.finish()
}
