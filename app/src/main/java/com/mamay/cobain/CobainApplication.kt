package com.mamay.cobain

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CobainApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            try {
                container.legacyDataMigrator.migrateIfNeeded()
            } catch (e: Exception) {
                Log.e("CobainApplication", "Startup legacy migration failed", e)
            }
        }
    }
}
