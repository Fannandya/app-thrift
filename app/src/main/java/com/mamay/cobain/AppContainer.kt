package com.mamay.cobain

import android.content.Context
import androidx.room.Room
import com.mamay.cobain.data.AppDatabase
import com.mamay.cobain.data.legacy.LegacyDataMigrator
import com.mamay.cobain.data.repository.RoomThriftItemRepository
import com.mamay.cobain.data.repository.ThriftItemRepository

/**
 * Small hand-rolled dependency graph: this app has one repository and no swappable
 * environments, so a full DI framework (Hilt) would add build complexity without
 * buying anything a single lazily-built container doesn't already give us.
 */
class AppContainer(context: Context) {
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    ).addMigrations(AppDatabase.MIGRATION_1_2).build()

    val thriftItemRepository: ThriftItemRepository by lazy {
        RoomThriftItemRepository(database.thriftItemDao())
    }

    val legacyDataMigrator: LegacyDataMigrator by lazy {
        LegacyDataMigrator(context.applicationContext, database)
    }
}
