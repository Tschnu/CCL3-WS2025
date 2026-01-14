package com.example.myapplication.db.profile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.db.dailyEntry.DailyEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DailyEntryEntity)

    @Update
    suspend fun updateProfile(profile: DailyEntryEntity)

    @Delete
    suspend fun deleteProfile(profile: DailyEntryEntity)

    // Get all profiles (in case you allow more than one)
    @Query("SELECT * FROM profile")
    fun getAllProfiles(): Flow<List<DailyEntryEntity>>

    // Get a single profile (most common use case)
    @Query("SELECT * FROM profile LIMIT 1")
    fun getProfile(): Flow<DailyEntryEntity?>
}


@Query("DELETE FROM profile")
suspend fun clearProfile()
