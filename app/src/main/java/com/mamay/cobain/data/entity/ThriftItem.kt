package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * categoryId is nullable and ON DELETE SET NULL: deleting a category must not
 * silently orphan or corrupt an item, it just clears the reference.
 *
 * Item attributes beyond category (Ukuran, Warna, ...) live in
 * [ItemAttributeValue], not as columns here.
 *
 * defaultDiscountId is the discount the admin picked for this item when several
 * linked discounts overlap in time; ON DELETE SET NULL so removing that discount
 * just clears the preference. The cashier can still override it per cart.
 */
@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = ItemCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Discount::class,
            parentColumns = ["id"],
            childColumns = ["defaultDiscountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("defaultDiscountId")]
)
data class ThriftItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int?,
    val quantity: Int = 1,
    val buyPrice: Int,
    val sellPrice: Int,
    val isSold: Boolean = false,
    val defaultDiscountId: Int? = null
)
