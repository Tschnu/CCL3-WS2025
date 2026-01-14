package com.example.myapplication.db.converters

import androidx.room.TypeConverter
import com.example.myapplication.domain.BloodFlow

class DailyLogConverter {

    @TypeConverter
    fun bloodFlowToInt(bloodFlow: BloodFlow): Int {
        return bloodFlow.ordinal
    }

    @TypeConverter
    fun intToBloodFlow(value: Int): BloodFlow {
        return BloodFlow.entries[value]
    }
}
