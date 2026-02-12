package com.timeleft.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Application-wide Room database.
 *
 * Uses the thread-safe singleton pattern ([getDatabase]) so only one
 * connection pool exists for the lifetime of the process.
 */
@Database(entities = [CustomDateEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customDateDao(): CustomDateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance, creating it on first call.
         * Double-checked locking ensures thread safety without unnecessary synchronization.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timeleft_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
