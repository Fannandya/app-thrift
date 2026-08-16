package com.mamay.cobain.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class ItemCategory(
    val id: Int = 0,
    val name: String
)