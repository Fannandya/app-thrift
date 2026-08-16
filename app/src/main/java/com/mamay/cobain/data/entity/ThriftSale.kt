package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * itemName/size/category are a snapshot at the time of sale, kept as plain text on
 * purpose: sale history must stay accurate even after the item/category/size it
 * referenced is renamed or deleted. itemId is only a soft link for drill-down and is
 * cleared (not cascaded) if the item is deleted, so the historical record survives.
 */
@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ThriftItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("itemId")]
)
data class ThriftSale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val transactionId: String,
    val itemId: Int?,
    val itemName: String,
    val size: String,
    val category: String = "",
    val quantity: Int,
    val sellPrice: Int,
    val totalPrice: Int,
    val timestamp: Long
)
