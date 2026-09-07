package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Header for one checkout; its line items are the ThriftSale rows sharing this id.
 *
 * Money is a snapshot, never recomputed: a receipt reprinted next month must show
 * the discount and change that actually happened, even if prices changed since.
 * discountType is stored as the DiscountType enum's name (plain TEXT) so the schema
 * stays readable in a raw SQLite browser and needs no Room TypeConverter.
 */
@Entity(tableName = "sale_transactions")
data class SaleTransaction(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val subtotal: Int,
    val discountType: String,
    val discountValue: Int,
    val discountAmount: Int,
    val total: Int,
    val paidAmount: Int,
    val changeAmount: Int
)
