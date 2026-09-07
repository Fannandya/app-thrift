package com.mamay.cobain.data.entity

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
    val receiptFooter: String = ""
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
