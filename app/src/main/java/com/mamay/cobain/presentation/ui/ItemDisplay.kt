package com.mamay.cobain.presentation.ui

import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.DiscountItem
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.domain.activeDiscountsFor
import com.mamay.cobain.domain.effectiveDiscount

/**
 * Small view-side helpers shared by the inventory, cashier and detail screens for
 * turning the flat attribute/discount lists into per-item display data. Kept out of
 * the composables so they stay declarative.
 *
 * The `*ForItem` variants scan the full list once and are fine for a single item
 * (ItemDetailScreen). Browse screens showing many rows must build a
 * `Map<itemId, ...>` ONCE (see `valuesByItem` / `indexActiveDiscounts`) and use the
 * `*From` variants below, or they re-scan the whole table per visible row per frame.
 */

/** attributeId -> value for one item. */
fun attributeValuesForItem(itemId: Int, all: List<ItemAttributeValue>): Map<Int, String> =
    all.filter { it.itemId == itemId }.associate { it.attributeId to it.value }

/** All non-blank attribute values for an item, in display order, joined " . ". */
fun attributesTextForItem(
    itemId: Int,
    attributes: List<ItemAttribute>,
    values: List<ItemAttributeValue>
): String = attributesTextFrom(attributeValuesForItem(itemId, values), attributes)

/** Same, from a pre-built `attributeId -> value` map (no per-row list scan). */
fun attributesTextFrom(
    valuesForItem: Map<Int, String>,
    attributes: List<ItemAttribute>
): String = attributes
    .sortedBy { it.displayOrder }
    .mapNotNull { attr -> valuesForItem[attr.id]?.takeIf { it.isNotBlank() } }
    .joinToString(" · ")

/**
 * The discount to show for an item on browse screens: the item's default if active,
 * otherwise the strongest active one. Returns null when nothing applies.
 */
fun displayDiscountForItem(
    item: ThriftItem,
    discounts: List<Discount>,
    links: List<DiscountItem>,
    now: Long
): Discount? = displayDiscountFrom(item, activeDiscountsFor(item.id, discounts, links, now))

/** Same, from a pre-built list of the item's active discounts (from `indexActiveDiscounts`). */
fun displayDiscountFrom(item: ThriftItem, active: List<Discount>): Discount? =
    effectiveDiscount(item, active, overrideId = null) ?: active.firstOrNull()
