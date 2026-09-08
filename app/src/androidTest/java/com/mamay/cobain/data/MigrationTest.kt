package com.mamay.cobain.data

import android.content.ContentValues
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test.db"

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate4To5_emptyDatabaseValidates() {
        helper.createDatabase(TEST_DB, 4).close()
        helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5).close()
    }

    @Test
    fun migrate4To5_preservesDataAndSeedsTheUkuranAttribute() {
        helper.createDatabase(TEST_DB, 4).apply {
            insert("categories", 0, ContentValues().apply { put("id", 1); put("name", "Atasan") })
            insert("sizes", 0, ContentValues().apply { put("id", 1); put("name", "M") })
            insert(
                "items", 0,
                ContentValues().apply {
                    put("id", 10); put("name", "Kaos"); put("sizeId", 1); put("categoryId", 1)
                    put("quantity", 3); put("buyPrice", 8000); put("sellPrice", 20000); put("isSold", 0)
                }
            )
            insert(
                "sales", 0,
                ContentValues().apply {
                    put("id", 100); put("transactionId", "tx1"); put("itemId", 10); put("itemName", "Kaos")
                    put("size", "M"); put("category", "Atasan"); put("quantity", 1); put("sellPrice", 20000)
                    put("totalPrice", 20000); put("timestamp", 1000L)
                }
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        // items no longer carries sizeId
        db.query("PRAGMA table_info(`items`)").use { c ->
            val cols = generateSequence { if (c.moveToNext()) c.getString(1) else null }.toList()
            assertFalse("sizeId" in cols)
            assertTrue("defaultDiscountId" in cols)
        }

        db.query("SELECT id FROM item_attributes WHERE name = 'Ukuran'").use { c ->
            assertTrue(c.moveToFirst())
        }
        db.query("SELECT value FROM item_attribute_options WHERE value = 'M'").use { c ->
            assertTrue(c.moveToFirst())
        }
        db.query("SELECT value FROM item_attribute_values WHERE itemId = 10").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals("M", c.getString(0))
        }
        db.query("SELECT buyPrice, attributesSummary, originalSellPrice, discountPercent FROM sales WHERE id = 100").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(8000, c.getInt(0))
            assertEquals("M", c.getString(1))
            assertEquals(20000, c.getInt(2))
            assertEquals(0, c.getInt(3))
        }
        db.query("SELECT itemTerm, lowStockThreshold FROM store_profile").use { c ->
            // store_profile has no seeded row on a v4 DB created by Room's own schema,
            // so this only asserts the columns exist and are queryable.
            if (c.moveToFirst()) {
                assertEquals("Barang", c.getString(0))
                assertEquals(2, c.getInt(1))
            }
        }
        db.query("PRAGMA foreign_key_check").use { c ->
            assertFalse("foreign_key_check reported violations", c.moveToFirst())
        }
        db.close()
    }
}
