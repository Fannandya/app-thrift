package com.mamay.cobain.data.entity

/**
 * How an [ItemAttribute] is filled in when adding/editing an item.
 *
 * Stored as its [name] in a TEXT column (no Room TypeConverter), mirroring how
 * [SaleTransaction.discountType] persists [com.mamay.cobain.domain.DiscountType].
 */
enum class AttributeMode {
    /** User picks from a managed list of [ItemAttributeOption] values. */
    LIST,

    /** User types a free-form value. */
    FREE_TEXT
}
