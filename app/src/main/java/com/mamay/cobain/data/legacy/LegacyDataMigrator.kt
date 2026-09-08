package com.mamay.cobain.data.legacy

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.mamay.cobain.data.AppDatabase
import com.mamay.cobain.data.entity.AttributeMode
import com.mamay.cobain.data.entity.ItemAttribute
import com.mamay.cobain.data.entity.ItemAttributeOption
import com.mamay.cobain.data.entity.ItemAttributeValue
import com.mamay.cobain.data.entity.ItemCategory
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
 *
 * The old data had a free-text "size" per item. Since v5 there is no `sizes` table:
 * this importer recreates the same seeded "Ukuran" attribute the v4->v5 migration
 * would have produced, and moves every legacy size string into
 * `item_attribute_values`.
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
                val legacyItems = readList(itemsFile, LegacyThriftItem.serializer())
                val legacySales = readList(salesFile, LegacyThriftSale.serializer())

                // Every non-blank size string that appears anywhere in the legacy data.
                val sizeNames = buildSet {
                    legacySizes.forEach { if (it.name.isNotBlank()) add(it.name) }
                    legacyItems.forEach { if (it.size.isNotBlank()) add(it.size) }
                    legacySales.forEach { if (it.size.isNotBlank()) add(it.size) }
                }

                var sizeAttributeId = 0
                if (sizeNames.isNotEmpty()) {
                    sizeAttributeId = (dao.findAttributeByName(SIZE_ATTRIBUTE_NAME)
                        ?: run {
                            val id = dao.insertAttribute(
                                ItemAttribute(
                                    name = SIZE_ATTRIBUTE_NAME,
                                    mode = AttributeMode.LIST.name,
                                    required = false,
                                    displayOrder = dao.nextAttributeDisplayOrder()
                                )
                            ).toInt()
                            ItemAttribute(id = id, name = SIZE_ATTRIBUTE_NAME, mode = AttributeMode.LIST.name)
                        }).id
                    sizeNames.forEachIndexed { index, name ->
                        dao.insertAttributeOption(
                            ItemAttributeOption(attributeId = sizeAttributeId, value = name, displayOrder = index)
                        )
                    }
                }

                val newItemIdByOldId = mutableMapOf<Int, Int>()
                for (legacy in legacyItems) {
                    val newId = dao.insertItem(
                        ThriftItem(
                            id = legacy.id,
                            name = legacy.name,
                            categoryId = categoryIdByName[legacy.category],
                            quantity = legacy.quantity,
                            buyPrice = legacy.buyPrice,
                            sellPrice = legacy.sellPrice,
                            isSold = legacy.isSold
                        )
                    ).toInt()
                    val resolvedId = if (legacy.id != 0) legacy.id else newId
                    newItemIdByOldId[legacy.id] = resolvedId
                    if (legacy.size.isNotBlank() && sizeAttributeId != 0) {
                        dao.insertAttributeValues(
                            listOf(
                                ItemAttributeValue(
                                    itemId = resolvedId,
                                    attributeId = sizeAttributeId,
                                    value = legacy.size
                                )
                            )
                        )
                    }
                }

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
                            timestamp = legacy.timestamp,
                            buyPrice = 0,
                            attributesSummary = legacy.size,
                            originalSellPrice = legacy.sellPrice,
                            discountPercent = 0
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
        const val SIZE_ATTRIBUTE_NAME = "Ukuran"
    }
}
