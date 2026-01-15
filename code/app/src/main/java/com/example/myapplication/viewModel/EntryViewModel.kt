package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.db.dailyEntry.DailyEntryDatabase
import com.example.myapplication.db.dailyEntry.DailyEntryEntity
import com.example.myapplication.domain.PeriodForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class EntryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DailyEntryDatabase.getDatabase(application).dailyEntryDao()

    private val _painCategory = MutableStateFlow(0)
    val painCategory: StateFlow<Int> = _painCategory

    private val _energyCategory = MutableStateFlow(0)
    val energyCategory: StateFlow<Int> = _energyCategory

    private val _moodCategory = MutableStateFlow(0)
    val moodCategory: StateFlow<Int> = _moodCategory

    private val _journalText = MutableStateFlow("")
    val journalText: StateFlow<String> = _journalText

    private val _bloodflowCategory = MutableStateFlow(0)
    val bloodflowCategory: StateFlow<Int> = _bloodflowCategory

    private var currentDate: Long = 0L

    // REAL bloodflow (from DB) for calendar
    private val _bloodflowByDate = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val bloodflowByDate: StateFlow<Map<Long, Int>> = _bloodflowByDate

    // PREDICTED bloodflow (virtual, NOT in DB) for calendar
    private val _predictedBloodflowByDate = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val predictedBloodflowByDate: StateFlow<Map<Long, Int>> = _predictedBloodflowByDate

    fun setPainCategory(value: Int) { _painCategory.value = value }
    fun setEnergyCategory(value: Int) { _energyCategory.value = value }
    fun setMoodCategory(value: Int) { _moodCategory.value = value }
    fun setJournalText(text: String) { _journalText.value = text }
    fun setBloodflowCategory(value: Int) { _bloodflowCategory.value = value }

    fun loadEntryForDate(date: LocalDate) {
        currentDate = date
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModelScope.launch {
            dao.getEntryByDate(currentDate).collect { entity ->
                entity?.let {
                    _painCategory.value = it.painCategory
                    _energyCategory.value = it.energyCategory
                    _moodCategory.value = it.moodCategory
                    _bloodflowCategory.value = it.bloodflowCategory
                    _journalText.value = it.journalText

                }
            }
        }
    }

    fun loadBloodflowForRange(start: Long, end: Long) {
        viewModelScope.launch {
            dao.getEntriesBetween(start, end).collect { list ->

                // ✅ REAL calendar map (Long -> Int)
                _bloodflowByDate.value = list.associate { it.date to it.bloodflowCategory }

                val localDateMap: Map<LocalDate, Int> = list.associate { entity ->
                    val ld = Instant.ofEpochMilli(entity.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    ld to entity.bloodflowCategory
                }

                val rangeStartLocal = Instant.ofEpochMilli(start)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val rangeEndLocal = Instant.ofEpochMilli(end)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val predictedLocalDateMap = PeriodForecast.predictFutureFlowInRange(
                    entriesByDate = localDateMap,
                    rangeStart = rangeStartLocal,
                    rangeEnd = rangeEndLocal
                )

                _predictedBloodflowByDate.value = predictedLocalDateMap.mapKeys { (localDate, _) ->
                    localDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                }
            }
        }
    }

    fun saveEntry() {
        if (currentDate == 0L) return

        val entry = DailyEntryEntity(
            date = currentDate,
            painCategory = painCategory.value,
            energyCategory = energyCategory.value,
            moodCategory = moodCategory.value,
            bloodflowCategory = bloodflowCategory.value,
            journalText = journalText.value
        )

        viewModelScope.launch {
            dao.insertEntry(entry)
        }
    }
}
