package com.example.myapplication.domain

data class DailyEntry(
    val date: Long,
    val painCategory: Int,
    val energyCategory: Int,
    val moodCategory: Int,
    val bloodflowCategory: Int,
    val journalText: String
)
