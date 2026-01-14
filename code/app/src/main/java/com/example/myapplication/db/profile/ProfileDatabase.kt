package com.example.myapplication.db.profile

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.db.dailyEntry.DailyEntryDao
import com.example.myapplication.db.dailyEntry.DailyEntryEntity

@Database(
    entities = [DailyEntryEntity::class], // this is your "profile" entity
    version = 1,
    exportSchema = false
)
abstract class ProfileDatabase : RoomDatabase() {

    abstract fun profileDao(): DailyEntryDao
    // ^ If you have a separate ProfileDao, change this to ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: ProfileDatabase? = null

        fun getDatabase(context: Context): ProfileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProfileDatabase::class.java,
                    "profile_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}