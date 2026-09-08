package com.mamay.cobain.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table (id is always 1). The shop's identity has to be editable by the
 * owner - it appears on every receipt and screen header - so it lives in the
 * database rather than in string resources, which are frozen at build time.
 */
@Entity(tableName = "store_profile")
data class StoreProfile(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val storeName: String = "",
    val address: String = "",
    val phone: String = "",
    val receiptFooter: String = "",
    /** Word for a stock item ("Barang", "Produk", "Sepatu"), woven into labels and titles. */
    @ColumnInfo(defaultValue = "'Barang'")
    val itemTerm: String = DEFAULT_ITEM_TERM,
    /** An unsold item with quantity at or below this shows up in the Dashboard "Stok Menipis" list. */
    @ColumnInfo(defaultValue = "2")
    val lowStockThreshold: Int = 2
) {
    companion object {
        const val SINGLETON_ID = 1
        const val DEFAULT_ITEM_TERM = "Barang"
    }
}
