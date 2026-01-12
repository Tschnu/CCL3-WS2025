package com.example.myapplication.data

import com.example.myapplication.db.DailyEntryDao
import com.example.myapplication.db.DailyEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DailyEntryRepository(
    private val dailyEntryDao: DailyEntryDao
) {

    val dailyEntries: Flow<List<DailyEntry>> =
        dailyEntryDao.getAllEntries().map { entities ->
            entities.map { entity ->
                DailyEntry(
                    date = entity.date,
                    painCategory = entity.painCategory,
                    energyCategory = entity.energyCategory,
                    moodCategory = entity.moodCategory,
                    bloodflowCategory = entity.bloodflowCategory
                )
            }
        }
    fun getEntryByDate(date: Long)=
        dailyEntryDao.getEntryByDate(date).map {entity ->
            entity?.let {
                DailyEntry(
                    date = entity.date,
                    painCategory = entity.painCategory,
                    energyCategory = entity.energyCategory,
                    moodCategory = entity.moodCategory,
                    bloodflowCategory = entity.bloodflowCategory
                )
            }
        }


    suspend fun insertEntry(entity: DailyEntryEntity) {
        dailyEntryDao.insertEntry(
            DailyEntryEntity(
                date = entity.date,
                painCategory = entity.painCategory,
                energyCategory = entity.energyCategory,
                moodCategory = entity.moodCategory,
                bloodflowCategory = entity.bloodflowCategory
            )
        )
    }


    suspend fun deleteEntry(entity: DailyEntryEntity) {
        dailyEntryDao.deleteEntry(
            DailyEntryEntity(
                date = entity.date,
                painCategory = entity.painCategory,
                energyCategory = entity.energyCategory,
                moodCategory = entity.moodCategory,
                bloodflowCategory = entity.bloodflowCategory
            )
        )
    }





}
