package com.mamay.cobain.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class ThriftItem(
    val id: Int = 0,
    val name: String,
    val size: String,
    val buyPrice: Int,
    val sellPrice: Int,
    val isSold: Boolean = false
)
