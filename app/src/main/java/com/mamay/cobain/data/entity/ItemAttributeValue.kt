package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The value one item holds for one attribute. For a [AttributeMode.LIST] attribute
 * this is the chosen option's text copied in; for [AttributeMode.FREE_TEXT] it is
 * the raw text.
 *
 * CASCADE on both parents: a live item's attribute values are meaningless once the
 * item or the attribute definition is gone. Historical protection stays on
 * [ThriftSale] as text snapshots, exactly as `category`/`size` already work.
 */
@Entity(
    tableName = "item_attribute_values",
    foreignKeys = [
        ForeignKey(
            entity = ThriftItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemAttribute::class,
            parentColumns = ["id"],
            childColumns = ["attributeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"),
        Index("attributeId"),
        Index(value = ["itemId", "attributeId"], unique = true)
    ]
)
data class ItemAttributeValue(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemId: Int,
    val attributeId: Int,
    val value: String
)
