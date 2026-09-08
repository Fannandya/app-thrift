package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Links a [Discount] to one [ThriftItem]. CASCADE on both sides: a link has no
 * meaning once either end is gone. Which discount actually applies when several
 * overlap is decided by [ThriftItem.defaultDiscountId] (set by the admin) or a
 * per-cart override chosen by the cashier.
 */
@Entity(
    tableName = "discount_items",
    foreignKeys = [
        ForeignKey(
            entity = Discount::class,
            parentColumns = ["id"],
            childColumns = ["discountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ThriftItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("discountId"),
        Index("itemId"),
        Index(value = ["discountId", "itemId"], unique = true)
    ]
)
data class DiscountItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val discountId: Int,
    val itemId: Int
)
