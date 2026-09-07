package com.mamay.cobain.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mamay.cobain.data.dao.StoreProfileDao
import com.mamay.cobain.data.dao.ThriftItemDao
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.StoreProfile
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale

@Database(
    entities = [
        ThriftItem::class,
        ItemCategory::class,
        ItemSize::class,
        ThriftSale::class,
        StoreProfile::class,
        SaleTransaction::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun thriftItemDao(): ThriftItemDao

    abstract fun storeProfileDao(): StoreProfileDao

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

        /**
         * Adds the shop's own identity to the database. Seeded with blank strings, not
         * a placeholder shop name: the UI can then tell "belum diisi" apart from a shop
         * genuinely named that, and prompt the owner to fill it in.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `store_profile` (
                        `id` INTEGER NOT NULL,
                        `storeName` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `receiptFooter` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO `store_profile` " +
                        "(`id`, `storeName`, `address`, `phone`, `receiptFooter`) VALUES (1, '', '', '', '')"
                )
            }
        }

        /**
         * Every checkout gets a header row carrying the money that the line items
         * cannot: discount, cash tendered, change. Pre-existing checkouts predate both
         * features, so they are backfilled as "paid exactly, no discount" - inventing a
         * paid amount would put fiction into the shop's records.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sale_transactions` (
                        `id` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `subtotal` INTEGER NOT NULL,
                        `discountType` TEXT NOT NULL,
                        `discountValue` INTEGER NOT NULL,
                        `discountAmount` INTEGER NOT NULL,
                        `total` INTEGER NOT NULL,
                        `paidAmount` INTEGER NOT NULL,
                        `changeAmount` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_transactionId` ON `sales` (`transactionId`)")
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `sale_transactions`
                        (`id`, `timestamp`, `subtotal`, `discountType`, `discountValue`,
                         `discountAmount`, `total`, `paidAmount`, `changeAmount`)
                    SELECT `transactionId`, MAX(`timestamp`), SUM(`totalPrice`), 'NONE', 0,
                           0, SUM(`totalPrice`), SUM(`totalPrice`), 0
                    FROM `sales`
                    GROUP BY `transactionId`
                    """.trimIndent()
                )
            }
        }
    }
}
