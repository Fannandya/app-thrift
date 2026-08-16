package com.mamay.cobain.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mamay.cobain.data.dao.ThriftItemDao
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale

@Database(
    entities = [ThriftItem::class, ItemCategory::class, ItemSize::class, ThriftSale::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun thriftItemDao(): ThriftItemDao

    companion object {
        const val DATABASE_NAME = "cobain.db"
    }
}
