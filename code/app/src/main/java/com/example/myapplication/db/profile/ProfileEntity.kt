package com.example.myapplication.db.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    val id: Int = 1,                 // always 1 profile row
    val name: String = "there",
    val flowerPicture: Int = 0,      // 0..4 for your 5 preset flowers
    val cycleLength: Int = 28,       // for calendar calculations
    val periodLength: Int = 5        // optional but useful
)
