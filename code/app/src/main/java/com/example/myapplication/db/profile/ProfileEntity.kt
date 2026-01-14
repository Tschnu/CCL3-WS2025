package com.example.myapplication.db.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val name: String,
    val flowerPicture: Int,
)
