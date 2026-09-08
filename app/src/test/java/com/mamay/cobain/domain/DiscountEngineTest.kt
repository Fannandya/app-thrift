package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.DiscountItem
import com.mamay.cobain.data.entity.ThriftItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscountEngineTest {

    private fun item(id: Int, price: Int, defaultDiscountId: Int? = null) =
        ThriftItem(id = id, name = "x", categoryId = null, quantity = 1, buyPrice = 0, sellPrice = price, defaultDiscountId = defaultDiscountId)

    private fun discount(id: Int, percent: Int, start: Long = 0, end: Long = 1_000) =
        Discount(id = id, percent = percent, startMillis = start, endMillis = end)

    @Test
    fun `applyPercent rounds to the nearest rupiah and clamps`() {
        assertEquals(20_000, applyPercent(20_000, 0))
        assertEquals(20_000, applyPercent(20_000, -5))
        assertEquals(15_000, applyPercent(20_000, 25))
        assertEquals(0, applyPercent(20_000, 100))
        assertEquals(0, applyPercent(20_000, 150))
        // 9_999 * 0.85 = 8_499.15 -> 8_499
        assertEquals(8_499, applyPercent(9_999, 15))
    }

    @Test
    fun `isActive treats start as inclusive and end as exclusive`() {
        val d = discount(1, 10, start = 100, end = 200)
        assertFalse(isActive(d, 99))
        assertTrue(isActive(d, 100))
        assertTrue(isActive(d, 199))
        assertFalse(isActive(d, 200))
    }

    @Test
    fun `activeDiscountsFor filters by link and window, strongest first`() {
        val discounts = listOf(
            discount(1, 10, start = 0, end = 1_000),
            discount(2, 30, start = 0, end = 1_000),
            discount(3, 50, start = 5_000, end = 6_000) // not yet active
        )
        val links = listOf(
            DiscountItem(id = 1, discountId = 1, itemId = 7),
            DiscountItem(id = 2, discountId = 2, itemId = 7),
            DiscountItem(id = 3, discountId = 3, itemId = 7),
            DiscountItem(id = 4, discountId = 1, itemId = 8)
        )
        val active = activeDiscountsFor(itemId = 7, discounts = discounts, links = links, now = 500)
        assertEquals(listOf(2, 1), active.map { it.id })
    }

    @Test
    fun `indexActiveDiscounts groups active discounts per item, strongest first`() {
        val discounts = listOf(
            discount(1, 10, start = 0, end = 1_000),
            discount(2, 30, start = 0, end = 1_000),
            discount(3, 50, start = 5_000, end = 6_000) // not yet active
        )
        val links = listOf(
            DiscountItem(id = 1, discountId = 1, itemId = 7),
            DiscountItem(id = 2, discountId = 2, itemId = 7),
            DiscountItem(id = 3, discountId = 3, itemId = 7),
            DiscountItem(id = 4, discountId = 1, itemId = 8)
        )
        val index = indexActiveDiscounts(discounts, links, now = 500)

        assertEquals(listOf(2, 1), index[7]?.map { it.id })
        assertEquals(listOf(1), index[8]?.map { it.id })
        assertNull(index[9])
    }

    @Test
    fun `indexActiveDiscounts is empty when nothing is active`() {
        val discounts = listOf(discount(1, 10, start = 10_000, end = 20_000))
        val links = listOf(DiscountItem(id = 1, discountId = 1, itemId = 7))
        assertTrue(indexActiveDiscounts(discounts, links, now = 0).isEmpty())
    }

    @Test
    fun `effectiveDiscount honours override, then default, then the single option`() {
        val a = discount(1, 10)
        val b = discount(2, 20)

        assertEquals(b, effectiveDiscount(item(1, 10_000), listOf(a, b), overrideId = 2))
        assertEquals(a, effectiveDiscount(item(1, 10_000, defaultDiscountId = 1), listOf(a, b), overrideId = null))
        assertEquals(a, effectiveDiscount(item(1, 10_000), listOf(a), overrideId = null))
        assertNull(effectiveDiscount(item(1, 10_000), listOf(a, b), overrideId = null))
    }

    @Test
    fun `isDiscountChoiceRequired only when multiple active and nothing picked`() {
        val a = discount(1, 10)
        val b = discount(2, 20)
        assertTrue(isDiscountChoiceRequired(item(1, 10_000), listOf(a, b), overrideId = null))
        assertFalse(isDiscountChoiceRequired(item(1, 10_000), listOf(a, b), overrideId = 1))
        assertFalse(isDiscountChoiceRequired(item(1, 10_000), listOf(a), overrideId = null))
    }

    @Test
    fun `discountedLine snapshots the original, final and percent`() {
        val line = discountedLine(item(1, 20_000), discount(1, 25))
        assertEquals(20_000, line.unitOriginal)
        assertEquals(15_000, line.unitFinal)
        assertEquals(25, line.percent)
        assertEquals(5_000, line.perUnitSaving)

        val none = discountedLine(item(1, 20_000), null)
        assertEquals(20_000, none.unitFinal)
        assertEquals(0, none.percent)
    }
}
