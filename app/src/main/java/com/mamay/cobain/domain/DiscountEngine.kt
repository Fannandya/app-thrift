package com.mamay.cobain.domain

import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.DiscountItem
import com.mamay.cobain.data.entity.ThriftItem

/**
 * Pure logic for the per-item discount feature, free of Android/Room so the
 * rounding and window edge cases are covered by fast JVM tests.
 *
 * A discount is a percentage that runs for a fixed window and is linked to
 * specific items. The FINAL price is `round(price * (100 - percent) / 100)`,
 * clamped so it never exceeds the original or drops below zero.
 */

/** Discounted unit price. `percent <= 0` is a no-op; `percent` is clamped to 0..100. */
fun applyPercent(price: Int, percent: Int): Int {
    val safePercent = percent.coerceIn(0, 100)
    if (safePercent == 0) return price
    val discounted = Math.round(price * (100 - safePercent) / 100.0).toInt()
    return discounted.coerceIn(0, price)
}

/** Active on [now] when `startMillis <= now < endMillis` (end date is inclusive). */
fun isActive(discount: Discount, now: Long): Boolean =
    now >= discount.startMillis && now < discount.endMillis

/**
 * Every discount active on [now] that is linked to [itemId], strongest first
 * (highest percent, then most recently created).
 */
fun activeDiscountsFor(
    itemId: Int,
    discounts: List<Discount>,
    links: List<DiscountItem>,
    now: Long
): List<Discount> {
    val linkedIds = links.filter { it.itemId == itemId }.mapTo(HashSet()) { it.discountId }
    return discounts
        .filter { it.id in linkedIds && isActive(it, now) }
        .sortedWith(compareByDescending<Discount> { it.percent }.thenByDescending { it.id })
}

/**
 * One-pass index: itemId -> its active discounts (strongest first). Build this once
 * per screen instead of calling [activeDiscountsFor] (which re-filters `links`) for
 * every visible row.
 */
fun indexActiveDiscounts(
    discounts: List<Discount>,
    links: List<DiscountItem>,
    now: Long
): Map<Int, List<Discount>> {
    val activeById = discounts.filter { isActive(it, now) }.associateBy { it.id }
    if (activeById.isEmpty()) return emptyMap()
    val byItem = HashMap<Int, MutableList<Discount>>()
    for (link in links) {
        val discount = activeById[link.discountId] ?: continue
        byItem.getOrPut(link.itemId) { mutableListOf() }.add(discount)
    }
    return byItem.mapValues { (_, list) ->
        list.sortedWith(compareByDescending<Discount> { it.percent }.thenByDescending { it.id })
    }
}

/**
 * The discount that actually applies to a line:
 * - a cashier's [overrideId] wins if it is among the active ones,
 * - otherwise the item's [ThriftItem.defaultDiscountId] if it is active,
 * - otherwise the single active discount if there is exactly one,
 * - otherwise null: the UI must force a choice rather than sell at an arbitrary rate.
 */
fun effectiveDiscount(
    item: ThriftItem,
    active: List<Discount>,
    overrideId: Int?
): Discount? {
    if (active.isEmpty()) return null
    overrideId?.let { id -> active.firstOrNull { it.id == id }?.let { return it } }
    item.defaultDiscountId?.let { id -> active.firstOrNull { it.id == id }?.let { return it } }
    return active.singleOrNull()
}

/** True when a line has more than one active discount and none has been picked. */
fun isDiscountChoiceRequired(item: ThriftItem, active: List<Discount>, overrideId: Int?): Boolean =
    active.size > 1 && effectiveDiscount(item, active, overrideId) == null

data class DiscountedLine(
    val unitOriginal: Int,
    val unitFinal: Int,
    val percent: Int
) {
    val perUnitSaving: Int get() = unitOriginal - unitFinal
}

fun discountedLine(item: ThriftItem, effective: Discount?): DiscountedLine {
    val percent = effective?.percent?.coerceIn(0, 100) ?: 0
    return DiscountedLine(
        unitOriginal = item.sellPrice,
        unitFinal = applyPercent(item.sellPrice, percent),
        percent = percent
    )
}
