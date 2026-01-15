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

        // Learn the user's last period "pattern":
        // from lastStart forward until bleeding stops (0), collecting daily flow values (1..3)
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

        // Now generate repeated future periods until rangeEnd
        val result = mutableMapOf<LocalDate, Int>()

        var nextStart = lastStart.plusDays(cycleDays.toLong())

        while (!nextStart.isAfter(rangeEnd)) {
            // add pattern days
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
}
