package com.example.myapplication.db.dailyEntry

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.db.converters.DailyLogConverter

@Database(
    entities = [DailyEntryEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DailyLogConverter::class)
abstract class DailyEntryDatabase : RoomDatabase() {

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
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
