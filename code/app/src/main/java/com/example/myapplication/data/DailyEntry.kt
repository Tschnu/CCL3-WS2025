package com.example.myapplication.data

data class DailyEntry(
    val date: Long,
    val painCategory: Int,
    val energyCategory: Int,
    val moodCategory: Int,
    val bloodflowCategory: Int
)
