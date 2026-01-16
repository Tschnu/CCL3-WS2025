package com.example.myapplication.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object PeriodForecast {

    // fallback if we can’t learn a pattern yet (starts heavy)
    private val fallbackPattern = listOf(3, 3, 2, 2, 1)

    /**
     * entriesByDate: LocalDate -> bloodflow (0 none, 1 light, 2 medium, 3 heavy)
     * Predicts bleeding days only within rangeStart.rangeEnd], and only after "today".
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
     *
     * Rules:
     * - period day = bloodflow > 0
     * - period start = bleeding today AND not bleeding yesterday
     *
     * Defaults:
     * - if cycle can't be computed (not enough starts) -> avgCycleDays = 28
     * - if period length can't be computed -> avgPeriodDays = fallbackPattern.size (5)
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

        // Period lengths (consecutive bleeding days after each start)
        val periodLengths = periodStarts.map { start ->
            var len = 0
            var day = start
            while ((filtered[day] ?: 0) > 0) {
                len++
                day = day.plusDays(1)
                if (len >= 15) break // safety cap
            }
            len
        }.filter { it > 0 }

        val avgPeriodDays = if (periodLengths.isNotEmpty()) {
            periodLengths.average().roundToInt()
        } else {
            fallbackPattern.size
        }

        // Cycle lengths (difference between starts) - use last 3 cycles (or maxCycles)
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
}
