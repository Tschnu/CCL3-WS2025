package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.myapplication.domain.PeriodForecast
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.BlueDark
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.ui.theme.GreenDark
import com.example.myapplication.ui.theme.RedDark
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.ui.theme.YellowDark
import com.example.myapplication.viewModel.EntryViewModel
import com.example.myapplication.viewModel.ProfileViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import com.example.myapplication.ui.theme.MoodBrightBlue
import com.example.myapplication.ui.theme.MoodBrightGreen
import com.example.myapplication.ui.theme.MoodDarkBlue
import com.example.myapplication.ui.theme.MoodDarkGreen
import com.example.myapplication.ui.theme.MoodYellow


@Composable
fun StatisticsPage(navController: NavController) {

    // ---------- DRAWER ----------
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val profileVm: ProfileViewModel = viewModel()
    val profile = profileVm.profile.collectAsState().value

    // ---------- YOUR EXISTING VMs ----------
    val realVm: EntryViewModel = viewModel(key = "realVm")
    val predVm: EntryViewModel = viewModel(key = "predVm")

    val stats = realVm.periodStats.collectAsState().value

    // ---------------- REAL GRAPH STATE ----------------
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(selectedMonth) {
        realVm.loadEntriesForMonth(selectedMonth)
    }

    val chartEntries = realVm.entriesForChart.collectAsState().value

    // ---------------- PREDICTION GRAPH STATE ----------------
    val currentMonth = remember { YearMonth.now() }
    val lastMonth = remember { YearMonth.now().minusMonths(1) }

    LaunchedEffect(Unit) {
        predVm.loadEntriesForMonth(lastMonth)
        predVm.setPredictionBaseMonth(currentMonth)
    }

    val predicted = predVm.predictedMonths.collectAsState().value
    var predIndex by remember { mutableStateOf(0) } // 0..2
    val predMonth = predicted.getOrNull(predIndex)

    // Filters shared by both charts
    var showBloodflow by remember { mutableStateOf(true) }
    var showPain by remember { mutableStateOf(true) }
    var showMood by remember { mutableStateOf(true) }
    var showEnergy by remember { mutableStateOf(true) }

    // ---------- DRAWER UI STATE ----------
    var isEditingName by remember { mutableStateOf(false) }
    var isChoosingFlower by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    val userName = profile.name
    val cycleLen = profile.cycleLength
    val periodLen = profile.periodLength
    val flowerIndex = profile.flowerPicture

    // placeholders until you have real flowers
    val flowerDrawables = listOf(
        R.drawable.flower_1,
        R.drawable.flower_2,
        R.drawable.flower_3,
        R.drawable.flower_4,
        R.drawable.flower_5
    )
    val currentFlowerRes = flowerDrawables.getOrElse(flowerIndex.coerceIn(0, 4)) { flowerDrawables.first() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White,
                drawerShape = RectangleShape
            )
            {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = "Settings of",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Brown
                    )

                    Spacer(Modifier.height(8.dp))

                    // header row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = currentFlowerRes),
                            contentDescription = "Profile flower",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Brown,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = Brown, thickness = 2.dp)
                    Spacer(Modifier.height(18.dp))

                    // ---------------- NAME (click pen to edit) ----------------
                    Text(
                        text = "Personal Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = Brown
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Name:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Brown
                    )


                    if (!isEditingName) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(userName, style = MaterialTheme.typography.titleMedium, color = Brown)

                            IconButton(onClick = {
                                isEditingName = true
                                nameInput = userName
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = Brown)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Your name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    profileVm.setName(nameInput.trim().ifBlank { "Your Name" })
                                    isEditingName = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Brown),
                                modifier = Modifier.weight(1f)
                            ) { Text("Save", color = Softsoftyellow) }

                            OutlinedButton(
                                onClick = { isEditingName = false },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ---------------- FLOWER (click pen to expand) ----------------
                    Text(
                        text = "Profile picture",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Brown
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Image(
//                                painter = painterResource(id = currentFlowerRes),
//                                contentDescription = "Current flower",
//                                modifier = Modifier.size(36.dp)
//                            )
//                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Choose flower image",
                                style = MaterialTheme.typography.titleMedium,
                                color = Brown
                            )
                        }

                        IconButton(onClick = { isChoosingFlower = !isChoosingFlower }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit flower", tint = Brown)
                        }
                    }

                    if (isChoosingFlower) {
                        Spacer(Modifier.height(10.dp))

                        // 3 per row + 2 per row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (i in 0..2) {
                                FlowerChoice(
                                    resId = flowerDrawables[i],
                                    selected = (i == flowerIndex),
                                    onClick = { profileVm.setFlowerPicture(i) }
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (i in 3..4) {
                                FlowerChoice(
                                    resId = flowerDrawables[i],
                                    selected = (i == flowerIndex),
                                    onClick = { profileVm.setFlowerPicture(i) }
                                )
                            }
                        }
                    }

//                    Spacer(Modifier.height(20.dp))

                    val entryVm: EntryViewModel = viewModel()


                    Spacer(Modifier.height(18.dp))

                    Spacer(Modifier.height(18.dp))
                    HorizontalDivider(color = Brown, thickness = 2.dp)
                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Here will be date realted settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = Brown
                    )

                    Button(onClick = { entryVm.deleteAllData() }) {
                        Text(
                            "Delete all data",
                            color= Softsoftyellow)

                    }


                    // ---------------- CYCLE / PERIOD (UI only for now) ----------------
//                    Text(
//                        text = "Cycle length: $cycleLen days",
//                        style = MaterialTheme.typography.titleLarge,
//                        color = Brown
//                    )
//                    Spacer(Modifier.height(6.dp))
//
//                    Slider(
//                        value = cycleLen.toFloat(),
//                        onValueChange = { profileVm.setCycleLength(it.toInt()) },
//                        valueRange = 15f..60f,
//                        steps = 60 - 15 - 1
//                    )
//
//                    Spacer(Modifier.height(16.dp))
//
//                    Text(
//                        text = "Period length: $periodLen days",
//                        style = MaterialTheme.typography.titleLarge,
//                        color = Brown
//                    )
//                    Spacer(Modifier.height(6.dp))
//
//                    Slider(
//                        value = periodLen.toFloat(),
//                        onValueChange = { profileVm.setPeriodLength(it.toInt()) },
//                        valueRange = 1f..15f,
//                        steps = 15 - 1 - 1
//                    )

                    Spacer(Modifier.height(18.dp))


                }
            }
        }
    ) {

        // ---------- PAGE CONTENT ----------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ✅ HEADER (NO BACKGROUND)
                // ✅ HEADER (NO BACKGROUND)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Analyze",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open settings",
                            tint = Brown
                        )
                    }
                }


                Spacer(modifier = Modifier.height(18.dp))

                //CARD: Journal Button + Averages only
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Softsoftyellow)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {



                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Brown, thickness = 2.dp)

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Averages in days",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Brown
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = Brown, thickness = 2.dp)

                        Spacer(modifier = Modifier.height(18.dp))
                        StatRow("Cycle length", "${stats.avgCycleDays}")
                        Spacer(modifier = Modifier.height(14.dp))
                        StatRow("Average Period", "${stats.avgPeriodDays}")


                        Spacer(modifier = Modifier.height(16.dp))

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


                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Divider(
                    color = Brown,
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        //.fillMaxWidth(0.9f)
                )


                Text(
                    text = "Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Brown
                )
                Divider(
                    color = Brown,
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        //.fillMaxWidth(0.9f)
                )


                // EVERYTHING BELOW = NO BACKGROUND
                Spacer(modifier = Modifier.height(16.dp))

                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters:",
                        color = Brown,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    LineToggleChip("Blood", RedDark, showBloodflow) {
                        showBloodflow = !showBloodflow
                    }
                    LineToggleChip("Pain", YellowDark, showPain) {
                        showPain = !showPain
                    }
                    LineToggleChip("Mood", BlueDark, showMood) {
                        showMood = !showMood
                    }
                    LineToggleChip("Energy", GreenDark, showEnergy) {
                        showEnergy = !showEnergy
                    }
                }


                Spacer(modifier = Modifier.height(24.dp))

                // Month selector (no card background)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Softsoftyellow
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "‹",
                            color = Brown,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )  { selectedMonth = selectedMonth.minusMonths(1) }
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
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )  { selectedMonth = selectedMonth.plusMonths(1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))



                // Real chart (no background)
                DailyMetricsChart(
                    entries = chartEntries,
                    month = selectedMonth,
                    showBloodflow = showBloodflow,
                    showPain = showPain,
                    showMood = showMood,
                    showEnergy = showEnergy
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Brown, thickness = 2.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Prediction (next 3 months)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Brown,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                PredictionMonthSelector(
                    predictedMonths = predicted,
                    predIndex = predIndex,
                    onPrev = { if (predIndex > 0) predIndex-- },
                    onNext = { if (predIndex < 2) predIndex++ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PredictedMonthChart(
                    prediction = predMonth,
                    showBloodflow = showBloodflow,
                    showPain = showPain,
                    showMood = showMood,
                    showEnergy = showEnergy
                )


                Spacer(modifier = Modifier.height(20.dp))

                MoodDonutChart(
                    entries = chartEntries,
                    month = selectedMonth
                )

            }
        }
    }}

        @Composable
private fun FlowerChoice(
    resId: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        tonalElevation = if (selected) 4.dp else 0.dp,
        color = if (selected) Brown.copy(alpha = 0.15f) else Color.Transparent,
        modifier = Modifier
            .size(54.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Flower option",
                modifier = Modifier.size(40.dp)
            )
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

/* ----------------- REAL CHART (MONTH) ----------------- */

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
    val mood = days.map {
        val raw = byDate[it]?.moodCategory ?: 0
        if(raw in 1..5)(6-raw).toFloat() else 0f
    }
    val energy = days.map { (byDate[it]?.energyCategory ?: 0).toFloat() }
    val flow = days.map { (byDate[it]?.bloodflowCategory ?: 0).toFloat() }

    MultiAxisLineChart(
        valuesPain = pain,
        valuesMood = mood,
        valuesEnergy = energy,
        valuesFlow = flow,
        showBloodflow = showBloodflow,
        showPain = showPain,
        showMood = showMood,
        showEnergy = showEnergy
    )
}

@Composable
private fun PredictionMonthSelector(
    predictedMonths: List<PeriodForecast.MonthlyPrediction>,
    predIndex: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val title = predictedMonths.getOrNull(predIndex)?.let { mp ->
        mp.month.month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${mp.month.year}"
    } ?: "Loading..."

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Softsoftyellow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                color = if (predIndex == 0) Brown.copy(alpha = 0.3f) else Brown,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable(
                        enabled = predIndex != 0,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onPrev()
                    }
            )

            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = Brown
            )

            Text(
                text = "›",
                color = if (predIndex == 2) Brown.copy(alpha = 0.3f) else Brown,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable(
                        enabled = predIndex != 2,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onNext()
                    }
            )

        }
    }
}

@Composable
fun MoodDonutChart(
    entries: List<DailyEntryEntity>,
    month: YearMonth,
    zoneId: ZoneId = ZoneId.systemDefault(),
    modifier: Modifier = Modifier
) {
    // Keep only entries from this month + only "logged" mood values 1..5
    val moodValues = entries
        .filter {
            val d = Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
            YearMonth.from(d) == month
        }
        .mapNotNull { e -> e.moodCategory.takeIf { it in 0..4 } }

    val total = moodValues.size

    // counts[0] -> mood 1, counts[4] -> mood 5
    val counts = IntArray(5)
    for (m in moodValues) counts[m]++

    val perc = FloatArray(5) { i ->
        if (total == 0) 0f else counts[i].toFloat() / total.toFloat()
    }

    // Choose any colors you like (just keep them distinct)
    val colors = listOf(
        MoodDarkBlue,
        MoodBrightBlue,
        MoodYellow,
        MoodDarkGreen,
        MoodBrightGreen
    )

    val moodLabels = listOf(
        "Awful",        // 0
        "Bad",          // 1
        "Okay",         // 2
        "Happy",        // 3
        "Very happy"    // 4
    )

    val moodIcons = listOf(
        R.drawable.awful,       // 0
        R.drawable.bad,         // 1
        R.drawable.okay,        // 2
        R.drawable.happy,       // 3
        R.drawable.veryhappy    // 4
    )


    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mood distribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(220.dp)
            ) {
                val stroke = Stroke(width = 34f, cap = StrokeCap.Round)

                // donut bounds (so it stays a circle)
                val diameter = size.minDimension
                val topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                if (total == 0) {
                    // draw “empty” ring
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                } else {
                    var startAngle = -90f
                    for (i in 0..4) {
                        val sweep = perc[i] * 360f
                        if (sweep > 0f) {
                            drawArc(
                                color = colors[i],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = stroke
                            )
                            startAngle += sweep
                        }
                    }
                }
            }

            // center label
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (total == 0) "No mood\nlogged" else "$total days",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                if (total != 0) {
                    Text(
                        text = "this month",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Legend
        for (i in 0..4) {
            val percentText = if (total == 0) "0%" else "${(perc[i] * 100).toInt()}%"
            LegendRow(
                label = moodLabels[i],
                iconRes = moodIcons[i],
                rightText = "${counts[i]}  ($percentText)"
            )
        }
    }
}

@Composable
private fun LegendRow(label: String, iconRes: Int, rightText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f))
        Text(rightText, fontWeight = FontWeight.Medium)
    }
}


@Composable
private fun PredictedMonthChart(
    prediction: PeriodForecast.MonthlyPrediction?,
    showBloodflow: Boolean,
    showPain: Boolean,
    showMood: Boolean,
    showEnergy: Boolean
) {
    val pain = prediction?.painByDay ?: List(30) { 0f }
    val mood = prediction?.moodByDay ?: List(30) { 0f }
    val energy = prediction?.energyByDay ?: List(30) { 0f }
    val flow = prediction?.bloodflowByDay ?: List(30) { 0f }

    MultiAxisLineChart(
        valuesPain = pain,
        valuesMood = mood,
        valuesEnergy = energy,
        valuesFlow = flow,
        showBloodflow = showBloodflow,
        showPain = showPain,
        showMood = showMood,
        showEnergy = showEnergy
    )
}

@Composable
private fun MultiAxisLineChart(
    valuesPain: List<Float>,
    valuesMood: List<Float>,
    valuesEnergy: List<Float>,
    valuesFlow: List<Float>,
    showBloodflow: Boolean,
    showPain: Boolean,
    showMood: Boolean,
    showEnergy: Boolean
) {
    val count = listOf(valuesPain.size, valuesMood.size, valuesEnergy.size, valuesFlow.size).minOrNull() ?: 0
    if (count <= 1) return

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val left = 50f
        val right = size.width - 50f
        val top = 20f
        val bottom = size.height - 30f

        val leftMax = 4f
        val rightMax = 3f

        fun x(i: Int) = left + (right - left) * i / (count - 1)

        fun yLeft(v: Float) =
            bottom - (v.coerceIn(0f, leftMax) / leftMax) * (bottom - top)

        fun yRight(v: Float) =
            bottom - (v.coerceIn(0f, rightMax) / rightMax) * (bottom - top)


        drawLine(Color.Black, Offset(left, top), Offset(left, bottom), 3f)
        drawLine(Color.Black, Offset(right, top), Offset(right, bottom), 3f)
        drawLine(Color.Black, Offset(left, bottom), Offset(right, bottom), 3f)

        for (i in 0..leftMax.toInt()) {   // 0..4
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

        for (i in 0..rightMax.toInt()) {  // 0..3
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


        fun drawSeries(values: List<Float>, color: Color, rightAxis: Boolean) {
            val path = Path()
            for (i in 0 until count) {
                val px = x(i)
                val py = if (rightAxis) yRight(values[i]) else yLeft(values[i])
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }

            drawPath(path, color, style = Stroke(5f, cap = StrokeCap.Round))

            for (i in 0 until count) {
                val px = x(i)
                val py = if (rightAxis) yRight(values[i]) else yLeft(values[i])
                drawCircle(color = color, radius = 6f, center = Offset(px, py))
            }
        }

        if (showBloodflow) drawSeries(valuesFlow, RedDark, true)
        if (showPain) drawSeries(valuesPain, YellowDark, false)
        if (showMood) drawSeries(valuesMood, BlueDark, false)
        if (showEnergy) drawSeries(valuesEnergy, GreenDark, false)
    }
}
