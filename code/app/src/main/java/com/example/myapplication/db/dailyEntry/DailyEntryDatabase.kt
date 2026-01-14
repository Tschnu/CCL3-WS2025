package com.example.myapplication.db.dailyEntry

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailyEntryEntity::class],
    version = 1,
    exportSchema = false
)

abstract class DailyEntryDatabase :  RoomDatabase() {
    abstract fun dailyEntryDao(): DailyEntryDao

    companion object {

        @Volatile
        private var INSTANCE: DailyEntryDatabase? = null

        fun getDatabase(context: Context): DailyEntryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DailyEntryDatabase::class.java,
                    "daily_entry_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}