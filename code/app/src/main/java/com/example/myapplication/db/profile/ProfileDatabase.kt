package com.example.myapplication.db.profile

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.db.dailyEntry.DailyEntryDao
import com.example.myapplication.db.dailyEntry.DailyEntryEntity

@Database(
    entities = [ProfileEntity::class], // this is your "profile" entity
    version = 2,
    exportSchema = false
)
abstract class ProfileDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
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
                )// ADD THIS LINE
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}