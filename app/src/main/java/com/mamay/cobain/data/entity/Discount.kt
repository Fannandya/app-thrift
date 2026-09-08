package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A percentage discount that runs for a fixed window and is linked to specific
 * items through [DiscountItem]. When such an item is sold within the window its
 * unit price is cut by [percent] (see [com.mamay.cobain.domain.DiscountEngine]).
 *
 * [startMillis] is 00:00 in the shop's zone on the first day (inclusive).
 * [endMillis] is 00:00 on the day AFTER the last day, so the active test is
 * `startMillis <= now < endMillis` and the end date itself is inclusive.
 *
 * A discount past its window is kept, not deleted: the shop can extend or re-run
 * it, and "Kelola Diskon" offers to clean up expired ones that are still linked.
 */
@Entity(tableName = "discounts")
data class Discount(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String = "",
    val percent: Int,
    val startMillis: Long,
    val endMillis: Long
)
