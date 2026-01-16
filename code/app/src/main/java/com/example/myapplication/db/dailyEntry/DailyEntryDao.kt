package com.example.myapplication.db.dailyEntry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEntry(entry: DailyEntryEntity)

    @Update
    suspend fun updateEntry(entry: DailyEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: DailyEntryEntity)

    @Query("SELECT * FROM dailyEntry ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DailyEntryEntity>>

    @Query("SELECT * FROM dailyEntry WHERE date = :date LIMIT 1")
    fun getEntryByDate(date: Long): Flow<DailyEntryEntity?>

    @Query("SELECT * FROM dailyEntry WHERE date BETWEEN :startDate AND :endDate")
    fun getEntriesBetween(startDate: Long, endDate: Long): Flow<List<DailyEntryEntity>>

    @Query("SELECT * FROM dailyEntry ORDER BY date ASC")
    fun observeAllEntries(): kotlinx.coroutines.flow.Flow<List<DailyEntryEntity>>


    @Query("SELECT * FROM dailyEntry Where journalText IS NOT NULL AND journalText != '' ORDER BY date DESC")
    fun getJournalEntries(): Flow<List<DailyEntryEntity>>
}