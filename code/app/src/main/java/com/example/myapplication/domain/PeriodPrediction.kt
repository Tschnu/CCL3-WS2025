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

    fun predictNextMonthsFromLast3Months(
        allEntries: List<DailyEntryEntity>,
        baseMonth: YearMonth,
        monthsAhead: Int = 3,
        zoneId: ZoneId = ZoneId.systemDefault(),
        today: LocalDate = LocalDate.now(),
        monthsBack: Long = 3
    ): List<MonthlyPrediction> {

        // --- 1) Build date -> entry map for last 3 months (up to today) ---
        val windowStart = today.minusMonths(monthsBack)

        val entriesByDate: Map<LocalDate, DailyEntryEntity> = allEntries
            .map { e -> e to Instant.ofEpochMilli(e.date).atZone(zoneId).toLocalDate() }
            .filter { (_, d) -> !d.isBefore(windowStart) && !d.isAfter(today) }
            .associate { (e, d) -> d to e }

        if (entriesByDate.isEmpty()) return emptyList()

        val sortedDates = entriesByDate.keys.sorted()

        // --- 2) Detect period starts: bleeding today, not bleeding yesterday ---
        val periodStarts = mutableListOf<LocalDate>()
        for (d in sortedDates) {
            val bleedToday = (entriesByDate[d]?.bloodflowCategory ?: 0) > 0
            if (!bleedToday) continue
            val bleedYesterday = (entriesByDate[d.minusDays(1)]?.bloodflowCategory ?: 0) > 0
            if (!bleedYesterday) periodStarts.add(d)
        }

        if (periodStarts.size < 2) return emptyList()

        // last 3 cycles max (4 starts)
        val starts = periodStarts.takeLast(4)
        val cycles: List<Pair<LocalDate, LocalDate>> = starts.zip(starts.drop(1))

        // --- 3) Collect values by cycle-day index (profile) ---
        val moodBuckets = mutableListOf<MutableList<Float>>()
        val energyBuckets = mutableListOf<MutableList<Float>>()
        val painBuckets = mutableListOf<MutableList<Float>>()
        val flowBuckets = mutableListOf<MutableList<Float>>()

        fun ensureSize(i: Int) {
            while (moodBuckets.size <= i) {
                moodBuckets.add(mutableListOf())
                energyBuckets.add(mutableListOf())
                painBuckets.add(mutableListOf())
                flowBuckets.add(mutableListOf())
            }
        }

        for ((start, end) in cycles) {
            var d = start
            var idx = 0
            while (d.isBefore(end)) {
                val e = entriesByDate[d]
                if (e != null) {
                    ensureSize(idx)
                    moodBuckets[idx].add(e.moodCategory.toFloat())
                    energyBuckets[idx].add(e.energyCategory.toFloat())
                    painBuckets[idx].add(e.painCategory.toFloat())
                    flowBuckets[idx].add(e.bloodflowCategory.toFloat())
                }
                d = d.plusDays(1)
                idx++
                if (idx > 60) break
            }
        }

        fun avg(list: List<Float>): Float = if (list.isEmpty()) 0f else list.average().toFloat()

        val cycleProfile: List<CycleDay> = moodBuckets.indices.map { i ->
            CycleDay(
                mood = avg(moodBuckets[i]),
                energy = avg(energyBuckets[i]),
                pain = avg(painBuckets[i]),
                flow = avg(flowBuckets[i])
            )
        }

        if (cycleProfile.isEmpty()) return emptyList()

        // ✅ IMPORTANT FIX: use REAL cycle length (diffs) not profile size
        val diffs = starts
            .zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it in 15..60 }

        val cycleLen = (if (diffs.isNotEmpty()) diffs.average().roundToInt() else cycleProfile.size)
            .coerceIn(21, 40)

        // build a profile exactly cycleLen long (so it doesn't repeat too fast)
        val effectiveProfile: List<CycleDay> =
            if (cycleProfile.size >= cycleLen) {
                cycleProfile.take(cycleLen)
            } else {
                val extended = mutableListOf<CycleDay>()
                extended.addAll(cycleProfile)
                while (extended.size < cycleLen) {
                    extended.add(cycleProfile[extended.size % cycleProfile.size])
                }
                extended
            }

        val lastStart = starts.last()

        fun clampMood(v: Float) = v.coerceIn(1f, 5f)
        fun clampEnergy(v: Float) = v.coerceIn(1f, 5f)
        fun clampPain(v: Float) = v.coerceIn(1f, 5f)
        fun clampFlow(v: Float) = v.coerceIn(0f, 3f)

        fun cycleIndexForDate(date: LocalDate): Int {
            val daysSinceStart = ChronoUnit.DAYS.between(lastStart, date).toInt()
            val mod = ((daysSinceStart % cycleLen) + cycleLen) % cycleLen
            return mod
        }

        // --- 4) Build next 3 months using cycle-day averages ---
        return (1..monthsAhead).map { offset ->
            val month = baseMonth.plusMonths(offset.toLong())
            val daysInMonth = month.lengthOfMonth()

            val pain = MutableList(daysInMonth) { 0f }
            val mood = MutableList(daysInMonth) { 0f }
            val energy = MutableList(daysInMonth) { 0f }
            val flow = MutableList(daysInMonth) { 0f }

            for (day in 1..daysInMonth) {
                val date = month.atDay(day)
                val idx = cycleIndexForDate(date)
                val p = effectiveProfile[idx]

                pain[day - 1] = clampPain(p.pain)
                mood[day - 1] = clampMood(p.mood)
                energy[day - 1] = clampEnergy(p.energy)
                flow[day - 1] = clampFlow(p.flow)
            }

            MonthlyPrediction(
                month = month,
                painByDay = pain,
                moodByDay = mood,
                energyByDay = energy,
                bloodflowByDay = flow
            )
        }
    }

    private data class CycleDay(
        val mood: Float,
        val energy: Float,
        val pain: Float,
        val flow: Float
    )
}
