package com.example.myapplication.domain
// this guy is basically the calculator for our period prediction stuff
import com.example.myapplication.db.dailyEntry.DailyEntryEntity
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object PeriodForecast {

    private val fallbackPattern = listOf(3, 3, 2, 2, 1) // if smth goes wrong or no input it takes this base pattern of bleeding

    // basic blood flow prediction calculator
    fun predictFutureFlowInRange(
        entriesByDate: Map<LocalDate, Int>,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        today: LocalDate = LocalDate.now(),
        defaultCycleDays: Int = 28, // fallback for period length in case there isn't enough input
        maxCycles: Int = 3 // how many cycle our calculator guy takes for predictions
    ): Map<LocalDate, Int> {

        if (entriesByDate.isEmpty()) return emptyMap()
        val windowStart = today.minusMonths(3) // here it tells i dont care about any other cycles than the last 3

        val recent = entriesByDate
            .filterKeys { d -> !d.isBefore(windowStart) && !d.isAfter(today) }

        if (recent.isEmpty()) return emptyMap()

        val sortedDays = recent.keys.sorted() // here is our little sorting guy who makes sure our dates are in the right order


        // Detects period start of existing periods
        val periodStarts = mutableListOf<LocalDate>()
        for (d in sortedDays) {
            val bleedToday = (entriesByDate[d] ?: 0) > 0
            if (!bleedToday) continue
            val bleedYesterday = (entriesByDate[d.minusDays(1)] ?: 0) > 0 // if yesterday no bleeding but today yes must mean there is a start
            val isToday = d == today // not use today for calculation because today could still be very likely changed due to mistake from user

            if (!bleedYesterday && !isToday) {
                periodStarts.add(d) // -> rule for what means period stated //TODO ask why help
            }
        }
        if (periodStarts.isEmpty()) return emptyMap() // if there is nothing of input there can be predictions duh

        val lastStart = periodStarts.last() // anchor point for prediction next one

        // zips period starts together which are like next to each other to find out cycle length. filters out weird lenghts as well tho
        val diffs = periodStarts
            .zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it in 15..60 }
            .takeLast(maxCycles)

        val cycleDays = if (diffs.isNotEmpty()) {
            diffs.average().roundToInt().coerceIn(21, 40) // we basically discriminate against people with off periods here but that makes it better for forgetful people
        } else {
            defaultCycleDays
        }


        // learns bleeding patterns?
        val flowBuckets = mutableListOf<MutableList<Int>>() // this makes buckets for each bleeding cycle day to sort stuff into

        fun ensureSize(i: Int) {
            while (flowBuckets.size <= i) {
                flowBuckets.add(mutableListOf()) // makes more buckets if not enough
            }
        }

        for (start in periodStarts) {
            var d = start // d is current day we ar at
            var idx = 0 // what period day it is starting from 0 which means cycle start

            while ((recent[d] ?: 0) > 0) { // that my is responsible for keeping the cycle going TODO make him do that in ui
                ensureSize(idx)
                flowBuckets[idx].add(recent[d] ?: 0) // stores bleeding into correct bucket

                d = d.plusDays(1) // here it adds to period index
                idx++
                if (idx >= 10) break
            }
        }

        val averagedPattern = flowBuckets.map { bucket ->
            bucket.average().roundToInt().coerceIn(0, 3)
        } // forces bucket to become actual numbers of averages of all buckets

        val pattern = if (averagedPattern.isNotEmpty()) { // here hopefully i now knows what it does
            averagedPattern
        } else {
            fallbackPattern
        }

        // TODO this was suggested why?
        //val pattern = averagedPattern.ifEmpty { fallbackPattern }


        // here we predict the future
        val result = mutableMapOf<LocalDate, Int>() // list of dates and flow
        var nextStart = lastStart.plusDays(cycleDays.toLong()) // adding stuff together to get next start date

        while (!nextStart.isAfter(rangeEnd)) {
            pattern.forEachIndexed { i, flow ->
                val day = nextStart.plusDays(i.toLong())
                if (
                    day.isAfter(today) && // TODO mabe we do need predictions atm that turn into cycle then
                    !day.isAfter(rangeEnd) &&
                    !day.isBefore(rangeStart)
                ) {
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

    data class PeriodStats(         //TODO shouldnt this be a seperate file can w ejust ahve dataclasses in here?
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
    // 🔮 PREDICTIONS: NEXT 3 MONTHS (daily values)
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

        val windowStart = today.minusMonths(monthsBack)

        val entriesByDate: Map<LocalDate, DailyEntryEntity> = allEntries
            .map { e -> e to Instant.ofEpochMilli(e.date).atZone(zoneId).toLocalDate() }
            .filter { (_, d) -> !d.isBefore(windowStart) && !d.isAfter(today) }
            .associate { (e, d) -> d to e }

        if (entriesByDate.isEmpty()) return emptyList()

        val sortedDates = entriesByDate.keys.sorted()

        // Detect period starts: bleeding today, not bleeding yesterday
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

        // Collect values by cycle-day index (profile)
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

                    // ✅ IMPORTANT: ignore "not logged" values (0) so averages don't invent data
                    if (e.moodCategory in 1..5) moodBuckets[idx].add(e.moodCategory.toFloat())
                    if (e.energyCategory in 1..5) energyBuckets[idx].add(e.energyCategory.toFloat())
                    if (e.painCategory in 1..5) painBuckets[idx].add(e.painCategory.toFloat())
                    if (e.bloodflowCategory in 0..3) flowBuckets[idx].add(e.bloodflowCategory.toFloat())
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

        // ✅ cycle length from real period start diffs
        val diffs = starts
            .zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it in 15..60 }

        val cycleLen = (if (diffs.isNotEmpty()) diffs.average().roundToInt() else 28)
            .coerceIn(21, 40)

        // ✅ how many days at the start are bleeding?
        val periodLen = run {
            var len = 0
            while (len < cycleProfile.size && cycleProfile[len].flow > 0f) len++
            len.coerceIn(3, 10)
        }

        // ✅ baseline day for non-period days
        val nonPeriod = cycleProfile.filter { it.flow <= 0f }
        val baseline = if (nonPeriod.isNotEmpty()) {
            CycleDay(
                mood = nonPeriod.map { it.mood }.average().toFloat(),
                energy = nonPeriod.map { it.energy }.average().toFloat(),
                pain = nonPeriod.map { it.pain }.average().toFloat(),
                flow = 0f
            )
        } else {
            // ✅ if there's basically no data, don't invent pain/mood/energy
            CycleDay(mood = 0f, energy = 0f, pain = 0f, flow = 0f)
        }

        // ✅ build a FULL cycle without repeating bleeding
        val effectiveProfile: List<CycleDay> = List(cycleLen) { i ->
            when {
                i < cycleProfile.size -> {
                    val p = cycleProfile[i]
                    // force NO bleeding after periodLen
                    if (i >= periodLen) p.copy(flow = 0f) else p
                }
                else -> baseline
            }
        }

        val lastStart = starts.last()

        fun clampMood(v: Float) = v.coerceIn(0f, 5f)
        fun clampEnergy(v: Float) = v.coerceIn(0f, 5f)
        fun clampPain(v: Float) = v.coerceIn(0f, 5f)
        fun clampFlow(v: Float) = v.coerceIn(0f, 3f)

        fun cycleIndexForDate(date: LocalDate): Int {
            val daysSinceStart = ChronoUnit.DAYS.between(lastStart, date).toInt()
            return ((daysSinceStart % cycleLen) + cycleLen) % cycleLen
        }

        // Build next 3 months using cycle-day averages
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


    fun predictOvulationDays(
        periodStarts: List<LocalDate>,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        defaultCycleDays: Int = 28
    ): Set<LocalDate> {
        if (periodStarts.isEmpty()) return emptySet()

        val diffs = periodStarts
            .zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it in 21..40 }

        val cycleLength = (if (diffs.isNotEmpty()) diffs.average().roundToInt() else defaultCycleDays)
            .coerceIn(21, 40)

        val ovulationOffset = cycleLength - 14

        // Anchor at last known start
        var start = periodStarts.last()

        // ✅ Walk BACK until we’re safely before the rangeStart
        while (start.isAfter(rangeStart.minusDays(cycleLength.toLong()))) {
            start = start.minusDays(cycleLength.toLong())
        }

        val result = mutableSetOf<LocalDate>()

        // ✅ Walk FORWARD through the whole range
        while (!start.isAfter(rangeEnd)) {
            val ovu = start.plusDays(ovulationOffset.toLong())
            if (!ovu.isBefore(rangeStart) && !ovu.isAfter(rangeEnd)) {
                result.add(ovu)
            }
            start = start.plusDays(cycleLength.toLong())
        }

        return result
    }

}
