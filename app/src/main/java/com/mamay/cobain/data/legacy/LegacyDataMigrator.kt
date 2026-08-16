package com.mamay.cobain.data.legacy

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.mamay.cobain.data.AppDatabase
import com.mamay.cobain.data.entity.ItemCategory
import com.mamay.cobain.data.entity.ItemSize
import com.mamay.cobain.data.entity.ThriftItem
import com.mamay.cobain.data.entity.ThriftSale
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * One-time import of the pre-Room JSON files (app version 1.0) into the Room
 * database, run once on startup. Runs inside a single DB transaction so a failure
 * partway through leaves the database untouched and the legacy files intact for a
 * retry on the next launch, instead of importing half the user's data.
 */
class LegacyDataMigrator(
    private val context: Context,
    private val database: AppDatabase
) {
    private val itemsFile get() = File(context.filesDir, "thrift_items.json")
    private val categoriesFile get() = File(context.filesDir, "thrift_categories.json")
    private val sizesFile get() = File(context.filesDir, "thrift_sizes.json")
    private val salesFile get() = File(context.filesDir, "thrift_sales.json")

    suspend fun migrateIfNeeded() {
        val legacyFiles = listOf(itemsFile, categoriesFile, sizesFile, salesFile).filter { it.exists() }
        if (legacyFiles.isEmpty()) return

        try {
            database.withTransaction {
                val dao = database.thriftItemDao()

                val legacyCategories = readList(categoriesFile, LegacyItemCategory.serializer())
                val categoryIdByName = mutableMapOf<String, Int>()
                for (legacy in legacyCategories) {
                    val newId = dao.insertCategory(ItemCategory(id = legacy.id, name = legacy.name)).toInt()
                    categoryIdByName[legacy.name] = if (legacy.id != 0) legacy.id else newId
                }

                val legacySizes = readList(sizesFile, LegacyItemSize.serializer())
                val sizeIdByName = mutableMapOf<String, Int>()
                for (legacy in legacySizes) {
                    val newId = dao.insertSize(ItemSize(id = legacy.id, name = legacy.name)).toInt()
                    sizeIdByName[legacy.name] = if (legacy.id != 0) legacy.id else newId
                }

                val legacyItems = readList(itemsFile, LegacyThriftItem.serializer())
                val newItemIdByOldId = mutableMapOf<Int, Int>()
                for (legacy in legacyItems) {
                    val newId = dao.insertItem(
                        ThriftItem(
                            id = legacy.id,
                            name = legacy.name,
                            sizeId = sizeIdByName[legacy.size],
                            categoryId = categoryIdByName[legacy.category],
                            quantity = legacy.quantity,
                            buyPrice = legacy.buyPrice,
                            sellPrice = legacy.sellPrice,
                            isSold = legacy.isSold
                        )
                    ).toInt()
                    newItemIdByOldId[legacy.id] = if (legacy.id != 0) legacy.id else newId
                }

                val legacySales = readList(salesFile, LegacyThriftSale.serializer())
                for (legacy in legacySales) {
                    dao.insertSale(
                        ThriftSale(
                            id = legacy.id,
                            transactionId = "legacy-${legacy.id}",
                            itemId = newItemIdByOldId[legacy.itemId],
                            itemName = legacy.itemName,
                            size = legacy.size,
                            category = legacy.category,
                            quantity = legacy.quantity,
                            sellPrice = legacy.sellPrice,
                            totalPrice = legacy.totalPrice,
                            timestamp = legacy.timestamp
                        )
                    )
                }
            }

            // Renamed, not deleted: the raw data stays recoverable if the import
            // above turns out to have mapped something wrong.
            legacyFiles.forEach { file ->
                file.renameTo(File(file.parentFile, "${file.name}.migrated.bak"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Legacy data migration failed, will retry on next launch", e)
        }
    }

    private fun <T> readList(file: File, serializer: kotlinx.serialization.KSerializer<T>): List<T> {
        if (!file.exists()) return emptyList()
        return try {
            Json.decodeFromString(ListSerializer(serializer), file.readText())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse legacy file ${file.name}", e)
            emptyList()
        }
    }

    private companion object {
        const val TAG = "LegacyDataMigrator"
    }
}
