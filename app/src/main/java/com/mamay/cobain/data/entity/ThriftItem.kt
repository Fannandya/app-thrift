package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * categoryId/sizeId are nullable and ON DELETE SET NULL: deleting a category or size
 * must not silently orphan or corrupt an item, it just clears the reference.
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
            entity = ItemSize::class,
            parentColumns = ["id"],
            childColumns = ["sizeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("sizeId")]
)
data class ThriftItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val sizeId: Int?,
    val categoryId: Int?,
    val quantity: Int = 1,
    val buyPrice: Int,
    val sellPrice: Int,
    val isSold: Boolean = false
)
