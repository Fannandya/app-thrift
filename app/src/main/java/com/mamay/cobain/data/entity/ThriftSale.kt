package com.mamay.cobain.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class ThriftSale(
    val id: Int = 0,
    val itemId: Int,
    val itemName: String,
    val size: String,
    val category: String = "",
    val quantity: Int,
    val sellPrice: Int,
    val totalPrice: Int,
    val timestamp: Long
)