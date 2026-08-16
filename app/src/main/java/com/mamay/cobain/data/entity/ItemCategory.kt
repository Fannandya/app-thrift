package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class ItemCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
