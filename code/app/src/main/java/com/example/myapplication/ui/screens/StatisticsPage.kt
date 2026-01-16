package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.db.dailyEntry.DailyEntryEntity
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.viewModel.EntryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun StatisticsPage(navController: NavController) {
    val entryViewModel: EntryViewModel = viewModel()
    val stats = entryViewModel.periodStats.collectAsState().value

    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(selectedMonth) {
        entryViewModel.loadEntriesForMonth(selectedMonth)
    }

    val chartEntries = entryViewModel.entriesForChart.collectAsState().value

    var showBloodflow by remember { mutableStateOf(true) }
    var showPain by remember { mutableStateOf(true) }
    var showMood by remember { mutableStateOf(true) }
    var showEnergy by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ---- TITLE ----
                Text(
                    text = "Analyze",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ---- JOURNAL BUTTON ----
                Button(
                    onClick = { navController.navigate(Screen.Journal.route) },
                    colors = ButtonDefaults.buttonColors(containerColor = Brown),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Journal Entries",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = Softsoftyellow
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.journal),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                                .size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Brown, thickness = 2.dp)

                // ---- AVERAGES ----
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Averages in days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Brown
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Brown, thickness = 2.dp)

                Spacer(modifier = Modifier.height(18.dp))
                StatRow("Cycle length", "${stats.avgCycleDays}")
                Spacer(modifier = Modifier.height(14.dp))
                StatRow("Average Period", "${stats.avgPeriodDays}")

                Spacer(modifier = Modifier.height(24.dp))

                // ---- MONTH SELECTOR (SOFT & PRETTY) ----
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Brown.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "‹",
                            color = Brown,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clickable { selectedMonth = selectedMonth.minusMonths(1) }
                        )

                        Text(
                            text = selectedMonth.month.name.lowercase()
                                .replaceFirstChar { it.uppercase() } +
                                    " ${selectedMonth.year}",
                            fontWeight = FontWeight.SemiBold,
                            color = Brown
                        )

                        Text(
                            text = "›",
                            color = Brown,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clickable { selectedMonth = selectedMonth.plusMonths(1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---- FILTER CHIPS ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LineToggleChip("Blood", Color.Red, showBloodflow) { showBloodflow = !showBloodflow }
                    LineToggleChip("Pain", Color.Yellow, showPain) { showPain = !showPain }
                    LineToggleChip("Mood", Color.Blue, showMood) { showMood = !showMood }
                    LineToggleChip("Energy", Color.Green, showEnergy) { showEnergy = !showEnergy }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---- CHART ----
                DailyMetricsChart(
                    entries = chartEntries,
                    month = selectedMonth,
                    showBloodflow = showBloodflow,
                    showPain = showPain,
                    showMood = showMood,
                    showEnergy = showEnergy
                )
            }
        }
    }
}

/* ----------------- SMALL COMPOSABLES ----------------- */

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), color = Brown)

        Surface(color = Brown, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LineToggleChip(
    label: String,
    color: Color,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = if (enabled) color else color.copy(alpha = 0.3f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = MaterialTheme.typography.labelMedium.fontSize
        )
    }
}

/* ----------------- CHART ----------------- */

@Composable
private fun DailyMetricsChart(
    entries: List<DailyEntryEntity>,
    month: YearMonth,
    showBloodflow: Boolean,
    showPain: Boolean,
    showMood: Boolean,
    showEnergy: Boolean
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDay = month.atDay(1)
    val days = (0 until daysInMonth).map { firstDay.plusDays(it.toLong()) }

    val byDate = entries.associateBy {
        Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    val pain = days.map { (byDate[it]?.painCategory ?: 0).toFloat() }
    val mood = days.map { (byDate[it]?.moodCategory ?: 0).toFloat() }
    val energy = days.map { (byDate[it]?.energyCategory ?: 0).toFloat() }
    val flow = days.map { (byDate[it]?.bloodflowCategory ?: 0).toFloat() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val left = 50f
        val right = size.width - 50f
        val top = 20f
        val bottom = size.height - 30f

        fun x(i: Int) = left + (right - left) * i / (days.size - 1)
        fun yLeft(v: Float) = bottom - (v / 5f) * (bottom - top)
        fun yRight(v: Float) = bottom - (v / 3f) * (bottom - top)

        // axes
        drawLine(Color.Black, Offset(left, top), Offset(left, bottom), 3f)
        drawLine(Color.Black, Offset(right, top), Offset(right, bottom), 3f)
        drawLine(Color.Black, Offset(left, bottom), Offset(right, bottom), 3f)

        fun drawSeries(values: List<Float>, color: Color, rightAxis: Boolean) {
            val path = Path()
            values.forEachIndexed { i, v ->
                val px = x(i)
                val py = if (rightAxis) yRight(v) else yLeft(v)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(path, color, style = Stroke(5f, cap = StrokeCap.Round))
        }

        // Y-axis labels
        for (i in 0..5) {
            val y = yLeft(i.toFloat())
            drawLine(Color.Black, Offset(left - 6f, y), Offset(left, y), 2f)
            drawContext.canvas.nativeCanvas.drawText(
                i.toString(),
                left - 18f,
                y + 6f,
                android.graphics.Paint().apply {
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        for (i in 0..3) {
            val y = yRight(i.toFloat())
            drawLine(Color.Black, Offset(right, y), Offset(right + 6f, y), 2f)
            drawContext.canvas.nativeCanvas.drawText(
                i.toString(),
                right + 18f,
                y + 6f,
                android.graphics.Paint().apply {
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.LEFT
                }
            )
        }

        if (showBloodflow) drawSeries(flow, Color.Red, true)
        if (showPain) drawSeries(pain, Color.Yellow, false)
        if (showMood) drawSeries(mood, Color.Blue, false)
        if (showEnergy) drawSeries(energy, Color.Green, false)
    }
}
