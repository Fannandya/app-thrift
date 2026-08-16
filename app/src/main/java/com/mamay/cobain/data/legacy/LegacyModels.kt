package com.mamay.cobain.data.legacy

import kotlinx.serialization.Serializable

/**
 * Mirrors the pre-Room JSON file shapes exactly (app version 1.0 wrote these to
 * context.filesDir). Kept separate from the Room entities in data.entity, which now
 * use categoryId/sizeId instead of free-text category/size, so this migration layer
 * is the only place that still needs to know the old, denormalized shape.
 */
@Serializable
data class LegacyItemCategory(
    val id: Int = 0,
    val name: String
)

@Serializable
data class LegacyItemSize(
    val id: Int = 0,
    val name: String
)

@Serializable
data class LegacyThriftItem(
    val id: Int = 0,
    val name: String,
    val size: String,
    val category: String = "",
    val quantity: Int = 1,
    val buyPrice: Int,
    val sellPrice: Int,
    val isSold: Boolean = false
)

@Serializable
data class LegacyThriftSale(
    val id: Int = 0,
    val itemId: Int,
    val itemName: String,
    val size: String,
    val category: String = "",
    val quantity: Int,
    val sellPrice: Int,
    val totalPrice: Int,
    val timestamp: Long
)
