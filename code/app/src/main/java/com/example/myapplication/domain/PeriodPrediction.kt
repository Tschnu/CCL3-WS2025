package com.example.myapplication.domain

import com.example.myapplication.db.dailyEntry.DailyEntryEntity
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object PeriodForecast {

    // fallback if we can’t learn a pattern yet (starts heavy)
    private val fallbackPattern = listOf(3, 3, 2, 2, 1)

    /**
     * entriesByDate: LocalDate -> bloodflow (0 none, 1 light, 2 medium, 3 heavy)
     * Predicts bleeding days only within rangeStart..rangeEnd], and only after "today".
     *
     * IMPORTANT: returns EMPTY map if user has never logged a period start.
     */
    fun predictFutureFlowInRange(
        entriesByDate: Map<LocalDate, Int>,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        today: LocalDate = LocalDate.now(),
        defaultCycleDays: Int = 28,
        maxCycles: Int = 3
    ): Map<LocalDate, Int> {

        if (entriesByDate.isEmpty()) return emptyMap()

        // Sort dates
        val sortedDays = entriesByDate.keys.sorted()

        // Find period starts: bleeding today, not bleeding yesterday
        val periodStarts = mutableListOf<LocalDate>()
        for (d in sortedDays) {
            val bleedToday = (entriesByDate[d] ?: 0) > 0
            if (!bleedToday) continue
            val bleedYesterday = (entriesByDate[d.minusDays(1)] ?: 0) > 0
            if (!bleedYesterday) periodStarts.add(d)
        }

        // RULE 1: no prediction until first period was input
        val lastStart = periodStarts.lastOrNull() ?: return emptyMap()

        // Determine cycle length
        val diffs = periodStarts
            .zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it in 15..60 }

        val cycleDays = if (diffs.isNotEmpty()) {
            val lastDiffs = diffs.takeLast(maxCycles)
            lastDiffs.average().roundToInt().coerceIn(15, 60)
        } else {
            defaultCycleDays
        }

        // Learn the user's last period "pattern"
        val learnedPattern = buildList {
            var d = lastStart
            while (true) {
                val v = entriesByDate[d] ?: 0
                if (v <= 0) break
                add(v)
                d = d.plusDays(1)
                if (size >= 10) break // safety cap
            }
        }

        val pattern = if (learnedPattern.isNotEmpty()) learnedPattern else fallbackPattern

        // Generate future periods
        val result = mutableMapOf<LocalDate, Int>()
        var nextStart = lastStart.plusDays(cycleDays.toLong())

        while (!nextStart.isAfter(rangeEnd)) {
            pattern.forEachIndexed { i, flow ->
                val day = nextStart.plusDays(i.toLong())
                if (day.isAfter(today) && !day.isAfter(rangeEnd) && !day.isBefore(rangeStart)) {
                    result[day] = flow
                }
            }
            nextStart = nextStart.plusDays(cycleDays.toLong())
        }

        return result
    }

    // ------------------------------------------------------------------------
    // 🧮 PERIOD STATISTICS (used by StatisticsPage)
    // ------------------------------------------------------------------------

    data class PeriodStats(
        val avgCycleDays: Int,     // ALWAYS has a value (defaults to 28)
        val avgPeriodDays: Int,    // ALWAYS has a value (defaults to fallbackPattern.size)
        val cyclesCount: Int,
        val periodsCount: Int
    )

    /**
     * Calculates average cycle length and average period length using ONLY the last 3 months.
     */
    fun calculatePeriodStats(
        entriesByDate: Map<LocalDate, Int>,
        today: LocalDate = LocalDate.now(),
        monthsBack: Long = 3,
        defaultCycleDays: Int = 28,
        maxCycles: Int = 3
    ): PeriodStats {

        val windowStart = today.minusMonths(monthsBack)

        // Only keep data from the last 3 months (up to today)
        val filtered = entriesByDate.filterKeys { d ->
            !d.isBefore(windowStart) && !d.isAfter(today)
        }

        if (filtered.isEmpty()) {
            return PeriodStats(
                avgCycleDays = defaultCycleDays,
                avgPeriodDays = fallbackPattern.size,
                cyclesCount = 0,
                periodsCount = 0
            )
        }

        val sortedDays = filtered.keys.sorted()

        // Detect period starts
        val periodStarts = mutableListOf<LocalDate>()
        for (d in sortedDays) {
            val bleedToday = (filtered[d] ?: 0) > 0
            if (!bleedToday) continue
            val bleedYesterday = (filtered[d.minusDays(1)] ?: 0) > 0
            if (!bleedYesterday) periodStarts.add(d)
        }

        // Period lengths
        val periodLengths = periodStarts.map { start ->
            var len = 0
            var day = start
            while ((filtered[day] ?: 0) > 0) {
                len++
                day = day.plusDays(1)
                if (len >= 15) break
            }
            len
        }.filter { it > 0 }

        val avgPeriodDays = if (periodLengths.isNotEmpty()) {
            periodLengths.average().roundToInt()
        } else {
            fallbackPattern.size
        }

        // Cycle diffs
        val cycleDiffs = periodStarts
            .zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it in 15..60 }
            .takeLast(maxCycles)

        val avgCycleDays = if (cycleDiffs.isNotEmpty()) {
            cycleDiffs.average().roundToInt()
        } else {
            defaultCycleDays
        }

        return PeriodStats(
            avgCycleDays = avgCycleDays,
            avgPeriodDays = avgPeriodDays,
            cyclesCount = cycleDiffs.size,
            periodsCount = periodStarts.size
        )
    }

    // ------------------------------------------------------------------------
    // 🔮 PREDICTIONS: NEXT 3 MONTHS (daily values) from last 3 months averages
    // ------------------------------------------------------------------------

    data class MonthlyPrediction(
        val month: YearMonth,
        val painByDay: List<Float>,
        val moodByDay: List<Float>,
        val energyByDay: List<Float>,
        val bloodflowByDay: List<Float>
    )

    /**
     * Predict next [monthsAhead] months (default 3).
     * For each day-of-month index, we average values from the last 3 months:
     * baseMonth, baseMonth-1, baseMonth-2.
     * Missing -> 0.
     */
    fun predictNextMonthsFromLast3Months(
        allEntries: List<DailyEntryEntity>,
        baseMonth: YearMonth,
        monthsAhead: Int = 3,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<MonthlyPrediction> {

        val historyMonths = setOf(
            baseMonth,
            baseMonth.minusMonths(1),
            baseMonth.minusMonths(2)
        )

        val historyEntries = allEntries.filter { e ->
            val d = Instant.ofEpochMilli(e.date).atZone(zoneId).toLocalDate()
            YearMonth.from(d) in historyMonths
        }

        fun avgForDayIndex(dayIndex: Int, selector: (DailyEntryEntity) -> Int): Float {
            val values = historyEntries.mapNotNull { e ->
                val d = Instant.ofEpochMilli(e.date).atZone(zoneId).toLocalDate()
                val idx = d.dayOfMonth - 1
                if (idx == dayIndex) selector(e) else null
            }
            return if (values.isEmpty()) 0f else values.average().toFloat()
        }

        return (1..monthsAhead).map { offset ->
            val month = baseMonth.plusMonths(offset.toLong())
            val daysInMonth = month.lengthOfMonth()

            MonthlyPrediction(
                month = month,
                painByDay = List(daysInMonth) { i -> avgForDayIndex(i) { it.painCategory } },
                moodByDay = List(daysInMonth) { i -> avgForDayIndex(i) { it.moodCategory } },
                energyByDay = List(daysInMonth) { i -> avgForDayIndex(i) { it.energyCategory } },
                bloodflowByDay = List(daysInMonth) { i -> avgForDayIndex(i) { it.bloodflowCategory } }
            )
        }
    }
}
