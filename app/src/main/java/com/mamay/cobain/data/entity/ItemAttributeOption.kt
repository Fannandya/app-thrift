package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One allowed value for a [AttributeMode.LIST] attribute (e.g. "M" under "Ukuran").
 *
 * Deleting an option does NOT touch [ItemAttributeValue]: the value there is a
 * copied string, so an item that was "M" keeps reading "M" after the option is
 * removed - matching the text-snapshot philosophy of [ThriftSale].
 */
@Entity(
    tableName = "item_attribute_options",
    foreignKeys = [
        ForeignKey(
            entity = ItemAttribute::class,
            parentColumns = ["id"],
            childColumns = ["attributeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("attributeId")]
)
data class ItemAttributeOption(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val attributeId: Int,
    val value: String,
    val displayOrder: Int = 0
)
