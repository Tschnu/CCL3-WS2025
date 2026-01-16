package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.db.dailyEntry.DailyEntryDatabase
import com.example.myapplication.db.dailyEntry.DailyEntryEntity
import com.example.myapplication.domain.PeriodForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class EntryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DailyEntryDatabase.getDatabase(application).dailyEntryDao()

    // ----------------------------
    // Chart entries
    // ----------------------------
    private val _entriesForChart = MutableStateFlow<List<DailyEntryEntity>>(emptyList())
    val entriesForChart: StateFlow<List<DailyEntryEntity>> = _entriesForChart

    // ----------------------------
    // Daily input state
    // ----------------------------
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

    // ----------------------------
    // Calendar bloodflow
    // ----------------------------
    private val _bloodflowByDate = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val bloodflowByDate: StateFlow<Map<Long, Int>> = _bloodflowByDate

    private val _predictedBloodflowByDate = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val predictedBloodflowByDate: StateFlow<Map<Long, Int>> = _predictedBloodflowByDate

    // ----------------------------
    // Period statistics
    // ----------------------------
    private val _periodStats = MutableStateFlow(
        PeriodForecast.PeriodStats(
            avgCycleDays = 0,
            avgPeriodDays = 0,
            cyclesCount = 0,
            periodsCount = 0
        )
    )
    val periodStats: StateFlow<PeriodForecast.PeriodStats> = _periodStats

    init {
        // Auto-recalculate stats whenever DB changes
        viewModelScope.launch {
            dao.observeAllEntries().collect { list ->
                val localDateMap = list.associate { entity ->
                    Instant.ofEpochMilli(entity.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate() to entity.bloodflowCategory
                }
                _periodStats.value = PeriodForecast.calculatePeriodStats(localDateMap)
            }
        }
    }

    // ----------------------------
    // Journal list
    // ----------------------------
    val journalEntries: StateFlow<List<DailyEntryEntity>> =
        dao.getJournalEntries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // ----------------------------
    // All entries
    // ----------------------------
    private val _allEntries = MutableStateFlow<List<DailyEntryEntity>>(emptyList())
    val allEntries: StateFlow<List<DailyEntryEntity>> = _allEntries

    init {
        viewModelScope.launch {
            dao.getAllEntries().collect { list ->
                _allEntries.value = list
            }
        }
    }

    // ----------------------------
    // Mutators
    // ----------------------------
    fun setPainCategory(value: Int) { _painCategory.value = value }
    fun setEnergyCategory(value: Int) { _energyCategory.value = value }
    fun setMoodCategory(value: Int) { _moodCategory.value = value }
    fun setJournalText(text: String) { _journalText.value = text }
    fun setBloodflowCategory(value: Int) { _bloodflowCategory.value = value }

    fun deleteEntry(entry: DailyEntryEntity) {
        viewModelScope.launch {
            dao.deleteEntry(entry)
        }
    }

    // ----------------------------
    // Load single day
    // ----------------------------
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

    // ----------------------------
    // Calendar prediction
    // ----------------------------
    fun loadBloodflowForRange(start: Long, end: Long) {
        viewModelScope.launch {
            dao.getEntriesBetween(start, end).collect { list ->
                _bloodflowByDate.value = list.associate { it.date to it.bloodflowCategory }

                val localDateMap = list.associate {
                    Instant.ofEpochMilli(it.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate() to it.bloodflowCategory
                }

                val predicted = PeriodForecast.predictFutureFlowInRange(
                    entriesByDate = localDateMap,
                    rangeStart = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate(),
                    rangeEnd = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate()
                )

                _predictedBloodflowByDate.value = predicted.mapKeys {
                    it.key.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }
        }
    }

    // ----------------------------
    // Save entry
    // ----------------------------
    fun saveEntry() {
        if (currentDate == 0L) return

        viewModelScope.launch {
            dao.insertEntry(
                DailyEntryEntity(
                    date = currentDate,
                    painCategory = painCategory.value,
                    energyCategory = energyCategory.value,
                    moodCategory = moodCategory.value,
                    bloodflowCategory = bloodflowCategory.value,
                    journalText = journalText.value
                )
            )
        }
    }

    // ----------------------------
    // EXISTING: last N days (KEEP)
    // ----------------------------
    fun loadEntriesForChart(daysBack: Int = 30) {
        val end = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val start = LocalDate.now()
            .minusDays(daysBack.toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModelScope.launch {
            dao.getEntriesBetween(start, end).collect {
                _entriesForChart.value = it
            }
        }
    }

    // ----------------------------
    // NEW: monthly chart loading
    // ----------------------------
    fun loadEntriesForMonth(month: YearMonth) {
        val start = month
            .atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val end = month
            .atEndOfMonth()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModelScope.launch {
            dao.getEntriesBetween(start, end).collect {
                _entriesForChart.value = it
            }
        }
    }

    fun deleteCurrentEntry() {
        if (currentDate == 0L) return

        viewModelScope.launch {
            dao.getEntryByDate(currentDate).collect { entry ->
                entry?.let {
                    dao.deleteEntry(it)
                }
            }
        }
    }

}
