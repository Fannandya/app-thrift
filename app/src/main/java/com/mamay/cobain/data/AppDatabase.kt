package com.mamay.cobain.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mamay.cobain.data.dao.ThriftItemDao
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale

@Database(
    entities = [ThriftItem::class, ItemCategory::class, ItemSize::class, ThriftSale::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun thriftItemDao(): ThriftItemDao

    companion object {
        const val DATABASE_NAME = "cobain.db"

        /**
         * Groups every line item of one checkout under a shared transactionId so the
         * dashboard can show "3 barang - Rp..." as one transaction instead of three.
         * Pre-existing rows get a unique 'legacy-<id>' id instead of a blank string,
         * so they keep counting as separate transactions (each was already a
         * single-item checkout before this feature existed).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sales ADD COLUMN transactionId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE sales SET transactionId = 'legacy-' || id WHERE transactionId = ''")
            }
        }
    }
}
