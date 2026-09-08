package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class ExcelReportBuilderTest {

    private val category = ItemCategory(id = 1, name = "Atasan")
    private val attribute = ItemAttribute(id = 1, name = "Ukuran", mode = "LIST", displayOrder = 0)
    private val items = listOf(
        ThriftItem(id = 1, name = "Kaos", categoryId = 1, quantity = 3, buyPrice = 8_000, sellPrice = 20_000),
        ThriftItem(id = 2, name = "Jaket", categoryId = 1, quantity = 1, buyPrice = 40_000, sellPrice = 90_000, isSold = true)
    )
    private val attributeValues = listOf(ItemAttributeValue(id = 1, itemId = 1, attributeId = 1, value = "M"))
    private val sales = listOf(
        ThriftSale(
            id = 1, transactionId = "tx1", itemId = 1, itemName = "Kaos", size = "M", category = "Atasan",
            quantity = 2, sellPrice = 16_000, totalPrice = 32_000, timestamp = 1_000L,
            buyPrice = 8_000, attributesSummary = "M", originalSellPrice = 20_000, discountPercent = 20
        )
    )
    private val transactions = listOf(
        SaleTransaction(
            id = "tx1", timestamp = 1_000L, subtotal = 32_000, discountType = "NONE", discountValue = 0,
            discountAmount = 0, total = 32_000, paidAmount = 32_000, changeAmount = 0
        )
    )

    private fun report(sheets: Set<ExcelSheet>): ByteArray {
        val out = ByteArrayOutputStream()
        buildExcelReport(
            out = out,
            request = ExcelExportRequest(sheets, rangeStartMillis = null, rangeEndMillis = null),
            itemTerm = "Barang",
            attributes = listOf(attribute),
            attributeValues = attributeValues,
            items = items,
            categories = listOf(category),
            sales = sales,
            transactions = transactions
        )
        return out.toByteArray()
    }

    @Test
    fun `output is a non-empty zip (xlsx) container`() {
        val bytes = report(ExcelSheet.entries.toSet())
        assertTrue(bytes.size > 100)
        // PK - local file header magic of every zip / xlsx.
        assertEquals(0x50, bytes[0].toInt() and 0xFF)
        assertEquals(0x4B, bytes[1].toInt() and 0xFF)
    }

    @Test
    fun `more selected sheets produce a larger workbook`() {
        val one = report(setOf(ExcelSheet.INVENTORY)).size
        val all = report(ExcelSheet.entries.toSet()).size
        assertTrue(all > one)
    }

    @Test
    fun `lineProfit uses the discounted sell price`() {
        assertEquals((16_000L - 8_000) * 2, lineProfit(sales.first()))
    }

    @Test
    fun `periodTotals rolls up revenue, cost, profit and discount`() {
        val totals = periodTotals(transactions, sales)
        assertEquals(1, totals.transactionCount)
        assertEquals(32_000L, totals.revenue)
        assertEquals(16_000L, totals.cost)
        assertEquals(16_000L, totals.grossProfit)
        assertEquals((20_000L - 16_000) * 2, totals.itemDiscount)
        assertEquals(32_000L, totals.averageTransaction)
    }
}
