package com.mamay.cobain.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutCalculatorTest {

    @Test
    fun `no discount leaves the subtotal untouched`() {
        val totals = calculateCheckoutTotals(150_000, DiscountType.NONE, 0, 200_000)

        assertEquals(0, totals.discountAmount)
        assertEquals(150_000, totals.total)
        assertEquals(50_000, totals.changeAmount)
        assertTrue(totals.isPaidEnough)
    }

    @Test
    fun `percent discount truncates instead of rounding up`() {
        // 10% of 170.505 is 17.050,5; Rupiah has no cents, and truncating keeps the
        // shop from handing out a Rupiah it never agreed to.
        val totals = calculateCheckoutTotals(170_505, DiscountType.PERCENT, 10, 200_000)

        assertEquals(17_050, totals.discountAmount)
        assertEquals(153_455, totals.total)
    }

    @Test
    fun `amount discount can never exceed the subtotal`() {
        val totals = calculateCheckoutTotals(50_000, DiscountType.AMOUNT, 80_000, 0)

        assertEquals(50_000, totals.discountAmount)
        assertEquals(0, totals.total)
        assertTrue(totals.isPaidEnough)
    }

    @Test
    fun `percent above one hundred is clamped`() {
        val totals = calculateCheckoutTotals(50_000, DiscountType.PERCENT, 250, 0)

        assertEquals(50_000, totals.discountAmount)
        assertEquals(0, totals.total)
    }

    @Test
    fun `underpaying reports no change and blocks the sale`() {
        val totals = calculateCheckoutTotals(100_000, DiscountType.NONE, 0, 60_000)

        assertEquals(0, totals.changeAmount)
        assertFalse(totals.isPaidEnough)
    }

    @Test
    fun `negative inputs are treated as zero instead of producing negative money`() {
        val totals = calculateCheckoutTotals(-5_000, DiscountType.AMOUNT, -1_000, -2_000)

        assertEquals(0, totals.subtotal)
        assertEquals(0, totals.discountAmount)
        assertEquals(0, totals.total)
        assertEquals(0, totals.changeAmount)
    }

    @Test
    fun `percent discount on a large bill does not overflow`() {
        // 25 juta x 100 sudah melewati batas Int, jadi rumus naif a * p / 100 akan
        // menghasilkan angka negatif di sini.
        val totals = calculateCheckoutTotals(25_000_000, DiscountType.PERCENT, 50, 25_000_000)

        assertEquals(12_500_000, totals.discountAmount)
        assertEquals(12_500_000, totals.total)
    }
}
