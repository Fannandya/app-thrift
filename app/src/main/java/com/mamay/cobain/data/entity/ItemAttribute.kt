package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-defined field an item can carry (Ukuran, Warna, Merek, ...). Replaces the
 * old hardcoded "size" concept: the migration to v5 seeds one row named "Ukuran"
 * from the former `sizes` table so existing data is preserved.
 *
 * "Kategori" is deliberately NOT modelled here - it stays a first-class column on
 * [ThriftItem] because filtering and reporting lean on it everywhere.
 *
 * [mode] holds an [AttributeMode] name. [required] is per-attribute so the shop
 * owner decides which fields block a save. [displayOrder] fixes the order the
 * inputs, columns and chips appear in.
 */
@Entity(tableName = "item_attributes")
data class ItemAttribute(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val mode: String,
    val required: Boolean = false,
    val displayOrder: Int = 0
)
