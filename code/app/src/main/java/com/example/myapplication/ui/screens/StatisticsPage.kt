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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.myapplication.ui.theme.MoodBrightBlue
import com.example.myapplication.ui.theme.MoodBrightGreen
import com.example.myapplication.ui.theme.MoodDarkBlue
import com.example.myapplication.ui.theme.MoodDarkGreen
import com.example.myapplication.ui.theme.MoodYellow
import com.example.myapplication.ui.theme.RedDark
import com.example.myapplication.ui.theme.RedLight
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.ui.theme.YellowDark
import com.example.myapplication.ui.theme.YellowStrong
import com.example.myapplication.viewModel.EntryViewModel
import com.example.myapplication.viewModel.ProfileViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.cos
import kotlin.math.sin

/* =========================================================
   MARKERS (accessibility): circle/square/triangle/pentagon
   ========================================================= */

private enum class MarkerShape { CIRCLE, TRIANGLE, SQUARE, PENTAGON }

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMarker(
    center: Offset,
    size: Float,
    color: Color,
    shape: MarkerShape
) {
    when (shape) {
        MarkerShape.CIRCLE -> drawCircle(color = color, radius = size / 2f, center = center)

        MarkerShape.SQUARE -> {
            val half = size / 2f
            drawRect(
                color = color,
                topLeft = Offset(center.x - half, center.y - half),
                size = Size(size, size)
            )
        }

        MarkerShape.TRIANGLE -> {
            val half = size / 2f
            val path = Path().apply {
                moveTo(center.x, center.y - half)
                lineTo(center.x - half, center.y + half)
                lineTo(center.x + half, center.y + half)
                close()
            }
            drawPath(path = path, color = color)
        }

        MarkerShape.PENTAGON -> {
            val path = regularPolygonPath(center = center, radius = size / 2f, sides = 5)
            drawPath(path = path, color = color)
        }
    }
}

private fun regularPolygonPath(center: Offset, radius: Float, sides: Int): Path {
    val path = Path()
    if (sides < 3) return path

    val startAngleDeg = -90f
    fun degToRad(deg: Float) = (deg * Math.PI / 180.0).toFloat()

    for (i in 0 until sides) {
        val angle = degToRad(startAngleDeg + i * (360f / sides))
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/* =========================================================
   PAGE
   ========================================================= */

@Composable
fun StatisticsPage(navController: NavController) {



    // ---------- DRAWER ----------
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val profileVm: ProfileViewModel = viewModel()
    val profile = profileVm.profile.collectAsState().value

    // ---------- VMs ----------
    val realVm: EntryViewModel = viewModel(key = "realVm")
    val predVm: EntryViewModel = viewModel(key = "predVm")

    val stats = realVm.periodStats.collectAsState().value

    // ---------------- REAL GRAPH STATE ----------------
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val thisMonth = remember { YearMonth.now() }

    LaunchedEffect(selectedMonth) {
        realVm.loadEntriesForMonth(selectedMonth)
    }


    val ovulationDays = predVm.ovulationDays.collectAsState().value

    val chartEntries = realVm.entriesForChart.collectAsState().value
    val isLoadingMonth = realVm.isLoadingMonth.collectAsState().value

    var showDeleteAllDialog by remember { mutableStateOf(false) }

    var stableMonth by remember { mutableStateOf(selectedMonth) }
    var stableEntries by remember { mutableStateOf(chartEntries) }

    var wasLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoadingMonth, chartEntries, selectedMonth) {
        // only commit the new data when loading just finished (true -> false)
        if (wasLoading && !isLoadingMonth) {
            stableMonth = selectedMonth
            stableEntries = chartEntries
        }
        wasLoading = isLoadingMonth
    }




    LaunchedEffect(selectedMonth) {
        predVm.setPredictionBaseMonth(selectedMonth) // optional, depends on your logic

        val start = selectedMonth
            .minusMonths(6)
            .atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val end = selectedMonth
            .plusMonths(3)
            .atEndOfMonth()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        predVm.loadBloodflowForRange(start, end)
    }







    // ---------------- PREDICTION GRAPH STATE ----------------
    val currentMonth = remember { YearMonth.now() }
    val lastMonth = remember { YearMonth.now().minusMonths(1) }



    LaunchedEffect(Unit) {
        predVm.loadEntriesForMonth(lastMonth)
        predVm.setPredictionBaseMonth(currentMonth)

        val start = currentMonth
            .atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val end = currentMonth
            .plusMonths(3)
            .atEndOfMonth()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        predVm.loadBloodflowForRange(start, end)
    }

    val predicted = predVm.predictedMonths.collectAsState().value
    var predIndex by remember { mutableStateOf(0) } // 0..2
    val predMonth = predicted.getOrNull(predIndex)
    val currentYM = remember { YearMonth.now() }

    LaunchedEffect(predicted) {
        val idx = predicted.indexOfFirst { it.month == currentYM }
        if (idx >= 0) predIndex = idx
    }

    // Filters shared by both charts
    var showBloodflow by remember { mutableStateOf(true) }
    var showPain by remember { mutableStateOf(true) }
    var showMood by remember { mutableStateOf(false) }
    var showEnergy by remember { mutableStateOf(false) }

    // ---------- DRAWER UI STATE ----------
    var isEditingName by remember { mutableStateOf(false) }
    var isChoosingFlower by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    val userName = profile.name
    val flowerIndex = profile.flowerPicture

    // ✅ NEW: pending selection (does NOT auto-save)
    var pendingFlowerIndex by remember { mutableStateOf(flowerIndex) }
    LaunchedEffect(flowerIndex) { pendingFlowerIndex = flowerIndex }

    val flowerDrawables = listOf(
        R.drawable.flower_1,
        R.drawable.flower_2,
        R.drawable.flower_3,
        R.drawable.flower_4,
        R.drawable.flower_5
    )
    val currentFlowerRes =
        flowerDrawables.getOrElse(flowerIndex.coerceIn(0, 4)) { flowerDrawables.first() }

    // ---------- INFO POPUPS ----------
    var showInfoCurrent by remember { mutableStateOf(false) }
    var showInfoPred by remember { mutableStateOf(false) }
    var showInfoMood by remember { mutableStateOf(false) }

    if (showInfoCurrent) {
        AlertDialog(
            onDismissRequest = { showInfoCurrent = false },
            confirmButton = {
                Button(
                    onClick = { showInfoCurrent = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Brown)
                ) { Text("OK", color = Softsoftyellow) }
            },
            title = { Text("Current Month Graph") },
            text = {
                Text(
                    "This graph shows your daily values for the selected month.\n\n" +
                            "• Blood = flow intensity\n" +
                            "• Pain / Mood / Energy = your daily categories\n\n" +
                            "Use the filter chips to hide/show lines."
                )
            }
        )
    }

    if (showInfoPred) {
        AlertDialog(
            onDismissRequest = { showInfoPred = false },
            confirmButton = {
                Button(
                    onClick = { showInfoPred = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Brown)
                ) { Text("OK", color = Softsoftyellow) }
            },
            title = { Text("Predictions Graph") },
            text = {
                Text(
                    "These are estimated values based on your previous logged data.\n\n" +
                            "If you don’t have enough entries yet, predictions can’t be generated.\n" +
                            "Log more days to unlock prediction charts.\n\n"+
                            "• Blood = flow intensity\n" +
                            "• Pain / Mood / Energy = your daily categories\n\n" +
                            "Use the filter chips to hide/show lines."
                )
            }
        )
    }

    if (showInfoMood) {
        AlertDialog(
            onDismissRequest = { showInfoMood = false },
            confirmButton = {
                Button(
                    onClick = { showInfoMood = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Brown)
                ) { Text("OK", color = Softsoftyellow) }
            },
            title = { Text("Mood Distribution") },
            text = {
                Text(
                    "This donut shows how often each mood was logged in this current month.\n\n" +
                            "More logged days = more accurate distribution."
                )
            }
        )
    }

    // ✅ Drawer opens from the RIGHT (RTL trick), but content stays LTR
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {

                // Drawer content should be LTR
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

                    ModalDrawerSheet(
                        modifier = Modifier.width(280.dp),
                        drawerContainerColor = Color.White,
                        drawerShape = RectangleShape
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {

                            Text(
                                text = "Settings of",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Brown
                            )

                            Spacer(Modifier.height(8.dp))

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

                            Text(
                                text = "Personal Settings",
                                style = MaterialTheme.typography.titleMedium,
                                color = Brown
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(text = "Name:", style = MaterialTheme.typography.bodyLarge, color = Brown)

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

                            Text(text = "Profile picture", style = MaterialTheme.typography.bodyLarge, color = Brown)
                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Choose flower image",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Brown
                                )
                                IconButton(onClick = {
                                    isChoosingFlower = !isChoosingFlower
                                    pendingFlowerIndex = flowerIndex // reset when opening
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit flower", tint = Brown)
                                }
                            }

                            if (isChoosingFlower) {
                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (i in 0..2) {
                                        FlowerChoice(
                                            resId = flowerDrawables[i],
                                            selected = (i == pendingFlowerIndex),
                                            onClick = { pendingFlowerIndex = i } // ✅ no auto-save
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
                                            selected = (i == pendingFlowerIndex),
                                            onClick = { pendingFlowerIndex = i } // ✅ no auto-save
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            profileVm.setFlowerPicture(pendingFlowerIndex) // ✅ commit
                                            isChoosingFlower = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Brown),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Save", color = Softsoftyellow) }

                                    OutlinedButton(
                                        onClick = {
                                            pendingFlowerIndex = flowerIndex // ✅ revert
                                            isChoosingFlower = false
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Cancel") }
                                }
                            }



                            Spacer(Modifier.height(18.dp))
                            HorizontalDivider(color = Brown, thickness = 2.dp)
                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = "Data Settings",
                                style = MaterialTheme.typography.titleMedium,
                                color = Brown
                            )

                            Button(
                                onClick = { showDeleteAllDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RedLight.copy(alpha = 0.5f))
                            ) {
                                Text("Delete all data", color = Softsoftyellow)
                            }


                            Spacer(Modifier.height(18.dp))
                            HorizontalDivider(color = Brown, thickness = 2.dp)
                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = "Version: 2.0",
                                style = MaterialTheme.typography.titleMedium,
                                color = Brown
                            )
                        }
                    }
                }
                if (showDeleteAllDialog) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        AlertDialog(
                            onDismissRequest = { showDeleteAllDialog = false },
                            containerColor = Softsoftyellow,
                            shape = RoundedCornerShape(20.dp),

                            title = {
                                Text(
                                    text = "Delete all data?",
                                    color = Brown,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },

                            text = {
                                Text(
                                    text = "This will permanently delete all your entries.\nThis action can’t be undone.",
                                    color = Brown,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },

                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        realVm.deleteAllData()
                                        showDeleteAllDialog = false
                                    }
                                ) {
                                    Text("Delete", color = RedLight)
                                }
                            },

                            dismissButton = {
                                TextButton(onClick = { showDeleteAllDialog = false }) {
                                    Text("Cancel", color = Brown)
                                }
                            }
                        )
                    }
                }

            }
        ) {
            // ✅ Page content back to normal LTR
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

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

                                Button(
                                    onClick = { navController.navigate(Screen.Journal.route) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brown),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "View Journal Entries",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            color = Softsoftyellow,
                                            modifier = Modifier.align(Alignment.Center)
                                        )

                                        Icon(
                                            painter = painterResource(id = R.drawable.journal),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 12.dp)
                                                .size(24.dp),
                                            tint = Softsoftyellow
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = Brown, thickness = 2.dp)

                        Text(
                            text = "Analysis Cycle",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Brown
                        )

                        Divider(color = Brown, thickness = 2.dp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Filters:",
                            color = Brown,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LineToggleChip("Blood", RedDark, MarkerShape.CIRCLE, showBloodflow) {
                                showBloodflow = !showBloodflow
                            }
                            LineToggleChip("Pain", YellowDark, MarkerShape.SQUARE, showPain) {
                                showPain = !showPain
                            }
                            LineToggleChip("Mood", BlueDark, MarkerShape.TRIANGLE, showMood) {
                                showMood = !showMood
                            }
                            LineToggleChip("Energy", GreenDark, MarkerShape.PENTAGON, showEnergy) {
                                showEnergy = !showEnergy
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ===== Current Month header + info =====
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Month",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Brown
                            )
                            IconButton(onClick = { showInfoCurrent = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = Brown)
                            }
                        }

                        Divider(color = Brown, thickness = 2.dp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(shape = RoundedCornerShape(20.dp), color = Softsoftyellow) {
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
                                        ) { selectedMonth = selectedMonth.minusMonths(1) }
                                )

                                Text(
                                    text = selectedMonth.month.name.lowercase().replaceFirstChar { it.uppercase() } +
                                            " ${selectedMonth.year}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Brown
                                )

                                Text(
                                    text = "›",
                                    color = if (selectedMonth >= thisMonth) Brown.copy(alpha = 0.3f) else Brown,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .clickable(
                                            enabled = selectedMonth < thisMonth,
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { selectedMonth = selectedMonth.plusMonths(1) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))


//                        Text(
//                            text = "Ovulation days: ${ovulationDays.size} -> " +
//                                    ovulationDays.filter { YearMonth.from(it) == selectedMonth }
//                                        .joinToString { it.dayOfMonth.toString() },
//                            color = Brown
//                        )


                        DailyMetricsChart(
                            entries = stableEntries,
                            month = stableMonth,
                            ovulationDays = ovulationDays
                                .filter { YearMonth.from(it) == stableMonth }
                                .map { it.dayOfMonth }
                                .toSet(),
                            showBloodflow = showBloodflow,
                            showPain = showPain,
                            showMood = showMood,
                            showEnergy = showEnergy
                        )


                        Spacer(modifier = Modifier.height(24.dp))

                        // ===== Predictions header + info =====
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Predictions",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Brown
                            )
                            IconButton(onClick = { showInfoPred = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = Brown)
                            }
                        }

                        Divider(color = Brown, thickness = 2.dp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(12.dp))

                        if (predicted.isNotEmpty()) {
                            PredictionMonthSelector(
                                predictedMonths = predicted,
                                predIndex = predIndex,
                                onPrev = { if (predIndex > 0) predIndex-- },
                                onNext = { if (predIndex < predicted.lastIndex) predIndex++ }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PredictedMonthChart(
                                prediction = predMonth,
                                ovulationDays = ovulationDays
                                    .filter { YearMonth.from(it) == predMonth?.month }
                                    .map { it.dayOfMonth }
                                    .toSet(),
                                showBloodflow = showBloodflow,
                                showPain = showPain,
                                showMood = showMood,
                                showEnergy = showEnergy
                            )
                        } else {
                            // ✅ no "Loading..." anywhere — show empty-state message IN the chart area
                            EmptyChartMessage(
                                text = "Oh no… there isn’t enough data here yet.\nLog more days to unlock predictions!",
                                height = 220.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ===== Mood header + info =====
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mood Distribution",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Brown
                            )
                            IconButton(onClick = { showInfoMood = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = Brown)
                            }
                        }

                        Divider(color = Brown, thickness = 2.dp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(12.dp))

                        MoodDonutChart(entries = chartEntries, month = selectedMonth)
                    }
                }
            }
        }
    }
}

/* =========================================================
   HELPERS
   ========================================================= */

@Composable
private fun EmptyChartMessage(
    text: String,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Softsoftyellow,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                color = Brown,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/* =========================================================
   SMALL COMPOSABLES
   ========================================================= */

@Composable
private fun FlowerChoice(resId: Int, selected: Boolean, onClick: () -> Unit) {
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

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), color = Brown)

        Surface(color = YellowStrong, shape = RoundedCornerShape(12.dp)) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = Brown,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LineToggleChip(
    label: String,
    color: Color,
    shape: MarkerShape,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val bg = if (enabled) color else color.copy(alpha = 0.3f)

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShapeIcon(shape = shape, color = Color.White, size = 14)
            Spacer(Modifier.width(6.dp))

            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = MaterialTheme.typography.labelMedium.fontSize
            )
        }
    }
}

@Composable
private fun ShapeIcon(shape: MarkerShape, color: Color, size: Int = 14) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val s = this.size.minDimension
        val center = Offset(s / 2f, s / 2f)

        when (shape) {
            MarkerShape.CIRCLE -> drawCircle(color = color, radius = s / 2f, center = center)

            MarkerShape.SQUARE -> drawRect(color = color, topLeft = Offset(0f, 0f), size = Size(s, s))

            MarkerShape.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(center.x, 0f)
                    lineTo(0f, s)
                    lineTo(s, s)
                    close()
                }
                drawPath(path, color)
            }

            MarkerShape.PENTAGON -> {
                val path = regularPolygonPath(center = center, radius = s / 2f, sides = 5)
                drawPath(path, color)
            }
        }
    }
}

/* =========================================================
   REAL CHART (MONTH)
   ========================================================= */

@Composable
private fun DailyMetricsChart(
    entries: List<DailyEntryEntity>,
    month: YearMonth,
    ovulationDays: Set<Int>,
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

    val pain: List<Float?> = days.map { day -> byDate[day]?.painCategory?.toFloat() }
    val energy: List<Float?> = days.map { day -> byDate[day]?.energyCategory?.toFloat() }
    val flow: List<Float?> = days.map { day -> byDate[day]?.bloodflowCategory?.toFloat() }
    val mood: List<Float?> = days.map { day ->
        byDate[day]?.moodCategory
            ?.takeIf { it in 0..4 }
            ?.let { (4f - it.toFloat()) }
    }

    val hasAnyData =
        pain.any { it != null } || energy.any { it != null } || flow.any { it != null } || mood.any { it != null }

    if (!hasAnyData) {
        EmptyChartMessage(
            text = "Oh no… there isn’t enough data here yet.\nLog more days to see your chart!",
            height = 220.dp
        )
        return
    }

    MultiAxisLineChart(
        valuesPain = pain,
        valuesMood = mood,
        valuesEnergy = energy,
        valuesFlow = flow,
        ovulationDays = ovulationDays,
        showBloodflow = showBloodflow,
        showPain = showPain,
        showMood = showMood,
        showEnergy = showEnergy
    )
}

/* =========================================================
   PREDICTIONS
   ========================================================= */

@Composable
private fun PredictionMonthSelector(
    predictedMonths: List<PeriodForecast.MonthlyPrediction>,
    predIndex: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    // ✅ never show "Loading..."
    val title = predictedMonths.getOrNull(predIndex)?.let { mp ->
        mp.month.month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${mp.month.year}"
    } ?: ""

    Surface(shape = RoundedCornerShape(20.dp), color = Softsoftyellow) {
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
                    ) { onPrev() }
            )

            Text(text = title, fontWeight = FontWeight.SemiBold, color = Brown)

            Text(
                text = "›",
                color = if (predIndex == predictedMonths.lastIndex) Brown.copy(alpha = 0.3f) else Brown,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable(
                        enabled = predIndex != predictedMonths.lastIndex,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onNext() }
            )
        }
    }
}

@Composable
private fun PredictedMonthChart(
    prediction: PeriodForecast.MonthlyPrediction?,
    ovulationDays: Set<Int>,
    showBloodflow: Boolean,
    showPain: Boolean,
    showMood: Boolean,
    showEnergy: Boolean
) {
    if (prediction == null) {
        EmptyChartMessage(
            text = "Oh no… there isn’t enough data here yet.\nLog more days to unlock predictions!",
            height = 220.dp
        )
        return
    }

    val days = prediction.painByDay.size.coerceAtLeast(30)

    val pain: List<Float?> = prediction.painByDay.map { it }
    val flow: List<Float?> = prediction.bloodflowByDay.map { it }

    val mood: List<Float?> = prediction.moodByDay.map { if (it > 0f) (4f - it) else 2f }
    val energy: List<Float?> = prediction.energyByDay.map { if (it > 0f) it else 2f }

    val hasAnyData =
        pain.any { it != null } || flow.any { it != null } || mood.any { it != null } || energy.any { it != null }

    if (!hasAnyData) {
        EmptyChartMessage(
            text = "Oh no… there isn’t enough data here yet.\nLog more days to unlock predictions!",
            height = 220.dp
        )
        return
    }

    MultiAxisLineChart(
        valuesPain = pain.ifEmpty { List(days) { null } },
        valuesMood = mood.ifEmpty { List(days) { null } },
        valuesEnergy = energy.ifEmpty { List(days) { null } },
        valuesFlow = flow.ifEmpty { List(days) { null } },
        ovulationDays = ovulationDays,
        showBloodflow = showBloodflow,
        showPain = showPain,
        showMood = showMood,
        showEnergy = showEnergy
    )
}

/* =========================================================
   DONUT CHART
   ========================================================= */

@Composable
fun MoodDonutChart(
    entries: List<DailyEntryEntity>,
    month: YearMonth,
    zoneId: ZoneId = ZoneId.systemDefault(),
    modifier: Modifier = Modifier
) {
    val moodValues = entries
        .filter {
            val d = Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
            YearMonth.from(d) == month
        }
        .mapNotNull { e -> e.moodCategory.takeIf { it in 0..4 } }

    val total = moodValues.size

    if (total == 0) {
        // ✅ show empty-state message in the chart area
        EmptyChartMessage(
            text = "Oh no… there isn’t enough data here yet.\nLog your mood for this month!",
            height = 220.dp,
            modifier = modifier
        )
        return
    }

    val counts = IntArray(5)
    for (m in moodValues) counts[m]++

    val perc = FloatArray(5) { i ->
        counts[i].toFloat() / total.toFloat()
    }

    val colors = listOf(
        MoodDarkBlue,
        MoodBrightBlue,
        MoodYellow,
        MoodDarkGreen,
        MoodBrightGreen
    )

    val moodLabels = listOf("Awful", "Bad", "Okay", "Happy", "Very happy")

    val moodIcons = listOf(
        R.drawable.awful,
        R.drawable.bad,
        R.drawable.okay,
        R.drawable.happy,
        R.drawable.veryhappy
    )

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val stroke = Stroke(width = 34f, cap = StrokeCap.Round)

                val diameter = size.minDimension
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$total days",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = "this month", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(Modifier.height(14.dp))

        for (i in 0..4) {
            val percentText = "${(perc[i] * 100).toInt()}%"
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

/* =========================================================
   MULTI AXIS LINE CHART (single source of truth)
   ========================================================= */

@Composable
private fun MultiAxisLineChart(
    valuesPain: List<Float?>,
    valuesMood: List<Float?>,
    valuesEnergy: List<Float?>,
    valuesFlow: List<Float?>,
    ovulationDays: Set<Int>,
    showBloodflow: Boolean,
    showPain: Boolean,
    showMood: Boolean,
    showEnergy: Boolean
) {
    val count = listOf(valuesPain.size, valuesMood.size, valuesEnergy.size, valuesFlow.size)
        .minOrNull() ?: 0
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

        fun yLeftFlipped(v: Float) =
            top + (v.coerceIn(0f, leftMax) / leftMax) * (bottom - top)


        fun yRight(v: Float) =
            bottom - (v.coerceIn(0f, rightMax) / rightMax) * (bottom - top)

        // =====================
        // OVULATION //TODO one solid colour
        // =====================
        // =====================
// FERTILE WINDOW (ovulation day + 3 days before) — solid color
// =====================

// ovulationDays is day-of-month numbers like {14}
// fertileDays becomes {11,12,13,14}
        val fertileDays: Set<Int> = buildSet {
            for (ovuDay in ovulationDays) {
                for (offset in 0 downTo -3) { // 0, -1, -2, -3
                    val d = ovuDay + offset
                    if (d in 1..count) add(d)   // clamp to valid days
                }
            }
        }

        for (i in 0 until count) {
            val dayOfMonth = i + 1
            if (fertileDays.contains(dayOfMonth)) {
                val xPos = x(i)
                val barWidth = (right - left) / (count - 1) // full day width


                drawRect(
                    color = MoodBrightBlue.copy(alpha = 0.25f), // ONE solid colour
                    topLeft = Offset(xPos - barWidth / 2f, top),
                    size = Size(barWidth, bottom - top)
                )
            }
        }







        // axes
        drawLine(Color.Black, Offset(left, top), Offset(left, bottom), 3f)
        drawLine(Color.Black, Offset(right, top), Offset(right, bottom), 3f)
        drawLine(Color.Black, Offset(left, bottom), Offset(right, bottom), 3f)

        // Y labels left (0..4)
        for (i in 0..leftMax.toInt()) {
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

        // Y labels right (0..3)
        for (i in 0..rightMax.toInt()) {
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

        // X labels (0/10/20/30 if available)
        val xLabelPaint = android.graphics.Paint().apply {
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val maxIndex = count - 1
        for (v in listOf(0, 10, 20, 30)) {
            if (v > maxIndex) continue
            val px = x(v)
            drawLine(Color.Black, Offset(px, bottom), Offset(px, bottom + 6f), 2f)
            drawContext.canvas.nativeCanvas.drawText(
                v.toString(),
                px,
                bottom + 26f,
                xLabelPaint
            )
        }

        fun drawSeries(
            values: List<Float?>,
            color: Color,
            rightAxis: Boolean,
            marker: MarkerShape
        ) {
            val path = Path()
            var started = false

            for (i in 0 until count) {
                val v = values[i]
                if (v == null) {
                    started = false
                    continue
                }
                val px = x(i)
                val py = if (rightAxis) yRight(v) else yLeft(v)

                if (!started) {
                    path.moveTo(px, py)
                    started = true
                } else {
                    path.lineTo(px, py)
                }
            }

            drawPath(path, color, style = Stroke(5f, cap = StrokeCap.Round))

            val markerSize = 12f
            for (i in 0 until count) {
                val v = values[i] ?: continue
                val px = x(i)
                val py = if (rightAxis) yRight(v) else yLeft(v)
                drawMarker(center = Offset(px, py), size = markerSize, color = color, shape = marker)
            }
        }

        fun drawSeriesCustomY(
            values: List<Float?>,
            color: Color,
            marker: MarkerShape,
            yMapper: (Float) -> Float
        ) {
            val path = Path()
            var started = false

            for (i in 0 until count) {
                val v = values[i] ?: continue
                val px = x(i)
                val py = yMapper(v)

                if (!started) {
                    path.moveTo(px, py)
                    started = true
                } else {
                    path.lineTo(px, py)
                }
            }

            drawPath(path, color, style = Stroke(5f, cap = StrokeCap.Round))

            val markerSize = 12f
            for (i in 0 until count) {
                val v = values[i] ?: continue
                drawMarker(
                    center = Offset(x(i), yMapper(v)),
                    size = markerSize,
                    color = color,
                    shape = marker
                )
            }
        }


        // LEFT axis label
        drawContext.canvas.nativeCanvas.drawText(
            "Pain / Mood / Energy",
            left - 36f,
            top - 8f,
            android.graphics.Paint().apply {
                textSize = 22f
                textAlign = android.graphics.Paint.Align.LEFT
            }
        )

        // RIGHT axis label
        drawContext.canvas.nativeCanvas.drawText(
            "Blood Flow",
            right + 36f,
            top - 8f,
            android.graphics.Paint().apply {
                textSize = 22f
                textAlign = android.graphics.Paint.Align.RIGHT
            }
        )

        // Shapes per category
        if (showBloodflow) drawSeries(valuesFlow, RedDark, true, MarkerShape.CIRCLE)
        if (showPain) drawSeries(valuesPain, YellowDark, false, MarkerShape.SQUARE)
        if (showMood) {
            drawSeriesCustomY(
                values = valuesMood,
                color = BlueDark,
                marker = MarkerShape.TRIANGLE,
                yMapper = ::yLeftFlipped
            )
        }
        if (showEnergy) drawSeries(valuesEnergy, GreenDark, false, MarkerShape.PENTAGON)
    }
}
