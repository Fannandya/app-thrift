package com.mamay.cobain.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mamay.cobain.data.entity.StoreProfile
import kotlinx.coroutines.flow.Flow

/**
 * Kept apart from ThriftItemDao: the shop profile never takes part in the checkout
 * transaction, so it has no reason to share a DAO with items, sales, and stock.
 */
@Dao
interface StoreProfileDao {

    @Query("SELECT * FROM store_profile WHERE id = :id")
    fun observeProfile(id: Int = StoreProfile.SINGLETON_ID): Flow<StoreProfile?>

    /** REPLACE is the intent here: there is exactly one row and saving overwrites it. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: StoreProfile)
}
