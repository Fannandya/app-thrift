package com.mamay.cobain.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sizes")
data class ItemSize(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
