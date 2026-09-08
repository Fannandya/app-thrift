package com.mamay.cobain

import android.content.Context
import androidx.room.Room
import com.mamay.cobain.data.AppDatabase
import com.mamay.cobain.data.legacy.LegacyDataMigrator
import com.mamay.cobain.data.repository.RoomThriftItemRepository
import com.mamay.cobain.data.repository.ThriftItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Small hand-rolled dependency graph: this app has one repository and no swappable
 * environments, so a full DI framework (Hilt) would add build complexity without
 * buying anything a single lazily-built container doesn't already give us.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    ).addMigrations(
        AppDatabase.MIGRATION_1_2,
        AppDatabase.MIGRATION_2_3,
        AppDatabase.MIGRATION_3_4,
        AppDatabase.MIGRATION_4_5
    ).build()

    val thriftItemRepository: ThriftItemRepository by lazy {
        RoomThriftItemRepository(database.thriftItemDao(), database.storeProfileDao())
    }

    val legacyDataMigrator: LegacyDataMigrator by lazy {
        LegacyDataMigrator(appContext, database)
    }

    /**
     * Copies the database into the app's external files dir so the shop owner can
     * hand the file to someone or stash it somewhere safe.
     *
     * Room runs SQLite in WAL mode, so the newest transactions can still live in
     * cobain.db-wal and not in cobain.db itself. Copying the main file alone would
     * silently hand back a backup missing today's sales, so the WAL is checkpointed
     * into the main database first. suspend + Dispatchers.IO because this is file
     * I/O and must never run on the main thread.
     */
    suspend fun exportDatabase(): File? = withContext(Dispatchers.IO) {
        val dbFile = appContext.getDatabasePath(AppDatabase.DATABASE_NAME)
        if (!dbFile.exists()) return@withContext null

        database.openHelper.writableDatabase
            .query("PRAGMA wal_checkpoint(TRUNCATE)")
            .use { it.moveToFirst() }

        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val exportDir = File(appContext.getExternalFilesDir(null), "database_exports")
        exportDir.mkdirs()
        val exportFile = File(exportDir, "cobain-backup-$stamp.db")
        dbFile.copyTo(exportFile, overwrite = true)
        exportFile
    }

    /**
     * Wipes every table (keeps the schema/version). Room's InvalidationTracker then
     * makes every observing Flow re-emit empty, so the whole UI drops to a
     * fresh-install state on its own. Like [exportDatabase], this is DB
     * infrastructure, not ViewModel state.
     */
    suspend fun resetStore() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }
}
