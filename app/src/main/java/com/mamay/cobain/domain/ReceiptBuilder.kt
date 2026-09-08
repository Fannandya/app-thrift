package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.StoreProfile
import com.mamay.cobain.data.entity.ThriftSale
import com.mamay.cobain.util.formatRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Thermal-printer convention, and still readable when the text is pasted into chat. */
private const val RECEIPT_WIDTH = 32

/**
 * Renders one completed sale as plain text.
 *
 * Pure and parameterised on the time zone so the output can be asserted exactly in
 * tests; the date pattern is numeric and pinned to Locale.US so the same sale never
 * renders differently because of a phone's language setting - only the zone, which
 * genuinely is the shop's own.
 *
 * Every part of the shop's identity is optional: a shop that has not filled in its
 * address must not get a blank line where the address would be.
 */
fun buildReceiptText(
    profile: StoreProfile,
    transaction: SaleTransaction,
    lines: List<ThriftSale>,
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).apply { this.timeZone = timeZone }
    val out = StringBuilder()

    fun rule(char: Char) = out.appendLine(char.toString().repeat(RECEIPT_WIDTH))
    fun centered(text: String) {
        if (text.isBlank()) return
        val pad = ((RECEIPT_WIDTH - text.length) / 2).coerceAtLeast(0)
        out.appendLine(" ".repeat(pad) + text)
    }

    rule('=')
    centered(profile.storeName)
    centered(profile.address)
    centered(profile.phone)
    rule('=')

    out.appendLine(padRow("No", transaction.id.take(8)))
    out.appendLine(padRow("Tgl", dateFormat.format(Date(transaction.timestamp))))
    rule('-')

    for (line in lines) {
        val extra = line.attributesSummary.ifBlank { line.size }
        val label = if (extra.isBlank()) line.itemName else "${line.itemName} ($extra)"
        out.appendLine(label)
        if (line.discountPercent > 0 && line.originalSellPrice > line.sellPrice) {
            out.appendLine(
                padRow("  ${line.quantity} x ${formatRupiah(line.originalSellPrice)}", "")
            )
            out.appendLine(
                padRow(
                    "  Diskon ${line.discountPercent}%",
                    "-${formatRupiah((line.originalSellPrice - line.sellPrice) * line.quantity)}"
                )
            )
        }
        out.appendLine(
            padRow("  ${line.quantity} x ${formatRupiah(line.sellPrice)}", formatRupiah(line.totalPrice))
        )
    }

    rule('-')
    val itemDiscountTotal = lines.sumOf { (it.originalSellPrice - it.sellPrice).coerceAtLeast(0) * it.quantity }
    if (itemDiscountTotal > 0) {
        out.appendLine(padRow("Diskon Barang", "-${formatRupiah(itemDiscountTotal)}"))
    }
    out.appendLine(padRow("Subtotal", formatRupiah(transaction.subtotal)))
    if (transaction.discountAmount > 0) {
        val label = if (transaction.discountType == DiscountType.PERCENT.name) {
            "Diskon (${transaction.discountValue}%)"
        } else {
            "Diskon"
        }
        out.appendLine(padRow(label, "-${formatRupiah(transaction.discountAmount)}"))
    }
    out.appendLine(padRow("TOTAL", formatRupiah(transaction.total)))
    out.appendLine(padRow("Tunai", formatRupiah(transaction.paidAmount)))
    out.appendLine(padRow("Kembali", formatRupiah(transaction.changeAmount)))
    rule('=')
    centered(profile.receiptFooter)

    return out.toString().trimEnd()
}

/** Label on the left, amount flushed to the right within [RECEIPT_WIDTH] columns. */
private fun padRow(left: String, right: String): String {
    val gap = (RECEIPT_WIDTH - left.length - right.length).coerceAtLeast(1)
    return left + " ".repeat(gap) + right
}
