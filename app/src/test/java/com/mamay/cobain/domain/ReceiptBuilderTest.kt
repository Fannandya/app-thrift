package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.StoreProfile
import com.mamay.cobain.data.entity.ThriftSale
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class ReceiptBuilderTest {

    private val jakarta = TimeZone.getTimeZone("Asia/Jakarta")

    private val profile = StoreProfile(
        storeName = "Toko Berkah",
        address = "Jl. Merdeka 12",
        phone = "0812-3456",
        receiptFooter = "Terima kasih!"
    )

    private val transaction = SaleTransaction(
        id = "4f3a1b2c-dead-beef-0000-111122223333",
        timestamp = 1_757_000_000_000L,
        subtotal = 50_000,
        discountType = DiscountType.PERCENT.name,
        discountValue = 10,
        discountAmount = 5_000,
        total = 45_000,
        paidAmount = 50_000,
        changeAmount = 5_000
    )

    private val lines = listOf(
        ThriftSale(
            transactionId = transaction.id, itemId = 1, itemName = "Kemeja Flanel",
            size = "M", category = "Atasan", quantity = 2, sellPrice = 10_000,
            totalPrice = 20_000, timestamp = transaction.timestamp
        ),
        ThriftSale(
            transactionId = transaction.id, itemId = 2, itemName = "Jaket Denim",
            size = "L", category = "Atasan", quantity = 1, sellPrice = 30_000,
            totalPrice = 30_000, timestamp = transaction.timestamp
        )
    )

    @Test
    fun `receipt carries the shop identity, the lines, and the money`() {
        val receipt = buildReceiptText(profile, transaction, lines, jakarta)

        assertTrue(receipt.contains("Toko Berkah"))
        assertTrue(receipt.contains("Jl. Merdeka 12"))
        assertTrue(receipt.contains("0812-3456"))
        assertTrue(receipt.contains("Kemeja Flanel"))
        assertTrue(receipt.contains("Jaket Denim"))
        assertTrue(receipt.contains("2 x Rp10.000"))
        assertTrue(receipt.contains("Rp20.000"))
        assertTrue(receipt.contains("Diskon (10%)"))
        assertTrue(receipt.contains("-Rp5.000"))
        assertTrue(receipt.contains("TOTAL"))
        assertTrue(receipt.contains("Rp45.000"))
        assertTrue(receipt.contains("Tunai"))
        assertTrue(receipt.contains("Kembali"))
        assertTrue(receipt.contains("Terima kasih!"))
    }

    @Test
    fun `receipt shows the transaction number in a form a buyer can read back`() {
        val receipt = buildReceiptText(profile, transaction, lines, jakarta)

        assertTrue(receipt.contains("4f3a1b2c"))
        // UUID penuh tidak berguna bagi pembeli dan merusak lebar struk.
        assertFalse(receipt.contains(transaction.id))
    }

    @Test
    fun `receipt date is rendered in the given time zone`() {
        val receipt = buildReceiptText(profile, transaction, lines, jakarta)
        val utc = buildReceiptText(profile, transaction, lines, TimeZone.getTimeZone("UTC"))

        assertTrue(receipt.contains("04/09/2025 22:33"))
        assertTrue(utc.contains("04/09/2025 15:33"))
    }

    @Test
    fun `a blank profile leaves no dangling empty lines or nulls`() {
        val receipt = buildReceiptText(StoreProfile(), transaction, lines, jakarta)

        assertFalse(receipt.contains("null"))
        // Tidak ada baris kosong beruntun dari alamat/telepon/footer yang belum diisi.
        assertFalse(receipt.contains("\n\n\n"))
        assertTrue(receipt.contains("Kemeja Flanel"))
    }

    @Test
    fun `line shows the joined attribute summary in parentheses`() {
        val withAttrs = lines.map { it.copy(attributesSummary = "Merah · M") }
        val receipt = buildReceiptText(profile, transaction, withAttrs, jakarta)

        assertTrue(receipt.contains("(Merah · M)"))
    }

    @Test
    fun `per-item discount prints the original price, the cut, and a total`() {
        val discounted = listOf(
            lines[0].copy(originalSellPrice = 12_500, sellPrice = 10_000, discountPercent = 20)
        )
        val receipt = buildReceiptText(profile, transaction, discounted, jakarta)

        assertTrue(receipt.contains("Rp12.500"))
        assertTrue(receipt.contains("Diskon 20%"))
        assertTrue(receipt.contains("Diskon Barang"))
    }

    @Test
    fun `no discount line is printed when there is no discount`() {
        val noDiscount = transaction.copy(
            discountType = DiscountType.NONE.name,
            discountValue = 0,
            discountAmount = 0,
            total = 50_000,
            changeAmount = 0
        )

        val receipt = buildReceiptText(profile, noDiscount, lines, jakarta)

        assertFalse(receipt.contains("Diskon"))
    }
}
