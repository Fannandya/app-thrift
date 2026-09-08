package com.mamay.cobain.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * itemName/size/category/attributesSummary are a snapshot at the time of sale, kept
 * as plain text on purpose: sale history must stay accurate even after the
 * item/category/attribute it referenced is renamed or deleted. itemId is only a
 * soft link for drill-down and is cleared (not cascaded) if the item is deleted,
 * so the historical record survives.
 *
 * New columns (all defaulted, appended after `timestamp` so positional constructor
 * calls keep compiling):
 * - buyPrice: cost per unit at sale time, for accurate gross-profit reporting.
 * - attributesSummary: all non-blank attribute values joined " . " in display
 *   order. `size` is kept as a legacy column, still written with the "Ukuran"
 *   value; readers prefer attributesSummary and fall back to size.
 * - originalSellPrice / discountPercent: the per-item discount that was applied.
 *   sellPrice/totalPrice already hold the FINAL (discounted) figures, so all
 *   existing subtotal/checkout/profit math is unchanged.
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
    indices = [Index("itemId"), Index("transactionId")]
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
    val timestamp: Long,
    @ColumnInfo(defaultValue = "0")
    val buyPrice: Int = 0,
    @ColumnInfo(defaultValue = "''")
    val attributesSummary: String = "",
    @ColumnInfo(defaultValue = "0")
    val originalSellPrice: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val discountPercent: Int = 0
)
