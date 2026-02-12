package com.timeleft.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for CRUD operations on user-created countdown events.
 *
 * [getAllCustomDates] returns a reactive [Flow] so the UI recomposes
 * automatically when the underlying table changes.
 */
@Dao
interface CustomDateDao {

    /** Observes all events, sorted by end date (soonest first). */
    @Query("SELECT * FROM custom_dates ORDER BY endDate ASC")
    fun getAllCustomDates(): Flow<List<CustomDateEntity>>

    @Query("SELECT * FROM custom_dates WHERE id = :id")
    suspend fun getCustomDateById(id: Int): CustomDateEntity?

    /** Inserts or replaces an event, returning the new row ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomDate(customDate: CustomDateEntity): Long

    @Update
    suspend fun updateCustomDate(customDate: CustomDateEntity)

    @Delete
    suspend fun deleteCustomDate(customDate: CustomDateEntity)

    @Query("DELETE FROM custom_dates WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM custom_dates")
    suspend fun getCount(): Int
}
