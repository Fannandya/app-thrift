package com.mamay.cobain.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mamay.cobain.data.dao.StoreProfileDao
import com.mamay.cobain.data.dao.ThriftItemDao
import com.mamay.cobain.data.entity.Discount
import com.mamay.cobain.data.entity.DiscountItem
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.SaleTransaction
import com.mamay.cobain.data.entity.StoreProfile
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale

@Database(
    entities = [
        ThriftItem::class,
        ItemCategory::class,
        ItemAttribute::class,
        ItemAttributeOption::class,
        ItemAttributeValue::class,
        Discount::class,
        DiscountItem::class,
        ThriftSale::class,
        StoreProfile::class,
        SaleTransaction::class
    ],
    version = 5,
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

        /**
         * The big one. Four features land on the schema together:
         *
         * - Fitur B/D: store_profile gains `itemTerm` (the shop's word for a stock
         *   item) and `lowStockThreshold`.
         * - Fitur A/D/F: sales gains `buyPrice` (cost snapshot for gross profit),
         *   `attributesSummary` (text snapshot of the item's attribute values), and
         *   `originalSellPrice` + `discountPercent` (the per-item discount applied).
         *   All backfilled from existing rows.
         * - Fitur A: the hardcoded "size" concept becomes a user-defined attribute
         *   system. The former `sizes` table is folded into one seeded attribute
         *   named "Ukuran"; every item's size value moves to `item_attribute_values`;
         *   then `items.sizeId` (and its FK + index) is removed by recreating the
         *   table. `sizes` is dropped afterwards.
         * - Fitur F: `discounts` + `discount_items` tables, and `items` gains
         *   `defaultDiscountId` (added inside the same table recreation).
         *
         * Ordering matters: `discounts` is created before `items` is recreated
         * (its new FK points there), and size values are copied into
         * `item_attribute_values` before `sizes` / the old `items` table are gone.
         * Room runs migrations with foreign-key enforcement off and checks
         * `foreign_key_check` afterwards, so dropping/recreating `items` mid-way does
         * not cascade-clear the child rows just inserted.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // --- Fitur B/D: shop-configurable item term + low-stock threshold
                db.execSQL("ALTER TABLE `store_profile` ADD COLUMN `itemTerm` TEXT NOT NULL DEFAULT 'Barang'")
                db.execSQL("ALTER TABLE `store_profile` ADD COLUMN `lowStockThreshold` INTEGER NOT NULL DEFAULT 2")

                // --- Fitur A/D/F: new snapshot columns on sales, backfilled
                db.execSQL("ALTER TABLE `sales` ADD COLUMN `buyPrice` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `sales` ADD COLUMN `attributesSummary` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `sales` ADD COLUMN `originalSellPrice` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `sales` ADD COLUMN `discountPercent` INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE `sales` SET `buyPrice` = " +
                        "COALESCE((SELECT `i`.`buyPrice` FROM `items` `i` WHERE `i`.`id` = `sales`.`itemId`), 0)"
                )
                db.execSQL("UPDATE `sales` SET `attributesSummary` = `size`")
                db.execSQL("UPDATE `sales` SET `originalSellPrice` = `sellPrice`")

                // --- Fitur F: discount tables (before items_new: its FK references discounts)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `discounts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `label` TEXT NOT NULL,
                        `percent` INTEGER NOT NULL,
                        `startMillis` INTEGER NOT NULL,
                        `endMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `discount_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `discountId` INTEGER NOT NULL,
                        `itemId` INTEGER NOT NULL,
                        FOREIGN KEY(`discountId`) REFERENCES `discounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`itemId`) REFERENCES `items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_discount_items_discountId` ON `discount_items` (`discountId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_discount_items_itemId` ON `discount_items` (`itemId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_discount_items_discountId_itemId` " +
                        "ON `discount_items` (`discountId`, `itemId`)"
                )

                // --- Fitur A: custom attribute tables
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_attributes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `mode` TEXT NOT NULL,
                        `required` INTEGER NOT NULL,
                        `displayOrder` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_attribute_options` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `attributeId` INTEGER NOT NULL,
                        `value` TEXT NOT NULL,
                        `displayOrder` INTEGER NOT NULL,
                        FOREIGN KEY(`attributeId`) REFERENCES `item_attributes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_item_attribute_options_attributeId` " +
                        "ON `item_attribute_options` (`attributeId`)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_attribute_values` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `itemId` INTEGER NOT NULL,
                        `attributeId` INTEGER NOT NULL,
                        `value` TEXT NOT NULL,
                        FOREIGN KEY(`itemId`) REFERENCES `items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`attributeId`) REFERENCES `item_attributes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_item_attribute_values_itemId` " +
                        "ON `item_attribute_values` (`itemId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_item_attribute_values_attributeId` " +
                        "ON `item_attribute_values` (`attributeId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_item_attribute_values_itemId_attributeId` " +
                        "ON `item_attribute_values` (`itemId`, `attributeId`)"
                )

                // --- Fitur A: fold `sizes` into a seeded "Ukuran" attribute
                db.execSQL(
                    "INSERT INTO `item_attributes` (`name`, `mode`, `required`, `displayOrder`) " +
                        "VALUES ('Ukuran', 'LIST', 0, 0)"
                )
                db.execSQL(
                    """
                    INSERT INTO `item_attribute_options` (`attributeId`, `value`, `displayOrder`)
                    SELECT (SELECT `id` FROM `item_attributes` WHERE `name` = 'Ukuran'), `name`, 0 FROM `sizes`
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `item_attribute_values` (`itemId`, `attributeId`, `value`)
                    SELECT `i`.`id`, (SELECT `id` FROM `item_attributes` WHERE `name` = 'Ukuran'), `s`.`name`
                    FROM `items` `i` JOIN `sizes` `s` ON `s`.`id` = `i`.`sizeId`
                    WHERE `i`.`sizeId` IS NOT NULL
                    """.trimIndent()
                )

                // --- Fitur A/F: recreate `items` without `sizeId`, with `defaultDiscountId`
                db.execSQL(
                    """
                    CREATE TABLE `items_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `categoryId` INTEGER,
                        `quantity` INTEGER NOT NULL,
                        `buyPrice` INTEGER NOT NULL,
                        `sellPrice` INTEGER NOT NULL,
                        `isSold` INTEGER NOT NULL,
                        `defaultDiscountId` INTEGER,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(`defaultDiscountId`) REFERENCES `discounts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `items_new` (`id`, `name`, `categoryId`, `quantity`, `buyPrice`, `sellPrice`, `isSold`)
                    SELECT `id`, `name`, `categoryId`, `quantity`, `buyPrice`, `sellPrice`, `isSold` FROM `items`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `items`")
                db.execSQL("ALTER TABLE `items_new` RENAME TO `items`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_categoryId` ON `items` (`categoryId`)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_items_defaultDiscountId` ON `items` (`defaultDiscountId`)"
                )

                // --- Fitur A: size concept fully replaced by the Ukuran attribute
                db.execSQL("DROP TABLE `sizes`")
            }
        }
    }
}
