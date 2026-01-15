package com.example.myapplication.db.dailyEntry

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dailyEntry")
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val date: Long,

    val painCategory: Int,
    val energyCategory: Int,
    val moodCategory: Int,
    val bloodflowCategory: Int,
    val journalText: String
)