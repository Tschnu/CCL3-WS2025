package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.alpha
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.ui.theme.MoodBrightBlue
import com.example.myapplication.ui.theme.RedLight
import com.example.myapplication.ui.theme.RedLightLight
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.viewModel.EntryViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch


@Composable
fun KalenderPage(navController: NavController) {

    val monthsBefore = 6
    val monthsAfter = 6

    val startMonth = YearMonth.now().minusMonths(monthsBefore.toLong())
    val months = remember(monthsBefore, monthsAfter) {
        (0..(monthsBefore + monthsAfter)).map { startMonth.plusMonths(it.toLong()) }
    }

    val entryViewModel: EntryViewModel = viewModel()

    val bloodflowMap = entryViewModel.bloodflowByDate.collectAsState().value
    val predictedBloodflowMap = entryViewModel.predictedBloodflowByDate.collectAsState().value

    val startDateLong = months.first().atDay(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val endDateLong = months.last().atEndOfMonth()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    var selectionMode by remember { mutableStateOf(false) }
    val selectedDates = remember { mutableStateSetOf<LocalDate>() }

    val ovulationDays = entryViewModel.ovulationDays.collectAsState().value

    LaunchedEffect(startDateLong, endDateLong) {
        entryViewModel.loadBloodflowForRange(startDateLong, endDateLong)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = monthsBefore)

    val scope = rememberCoroutineScope()
    val currentMonthIndex = remember(monthsBefore) { monthsBefore }

    val isCurrentMonthVisible by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.any { it.index == currentMonthIndex }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            CalendarHeader(
                selectionMode = selectionMode,
                onToggleSelection = {
                    selectionMode = !selectionMode
                    if (!selectionMode) selectedDates.clear()
                }
            )


            if (selectionMode && selectedDates.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            entryViewModel.togglePeriodDays(selectedDates.toList())
                            selectedDates.clear()
                            selectionMode = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Brown
                        )
                    ) {
                        Text(text = "Save",
                            color = Softsoftyellow)
                    }

                    Button(
                        onClick = {
                            selectedDates.clear()
                            selectionMode = false
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(2.dp, RedLightLight)
                    ) {
                        Text("Cancel", color = RedLightLight)
                    }
                }
            }

            WeekDayHeader()
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(months) { month ->
                    MonthCalendar(
                        month = month,
                        navController = navController,
                        bloodflowMap = bloodflowMap,
                        predictedMap = predictedBloodflowMap,
                        selectionMode = selectionMode,
                        selectedDates = selectedDates,
                        ovulationDays = ovulationDays
                    )
                }
            }
        }

        if (!isCurrentMonthVisible) {
            Button(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(currentMonthIndex)
                    }
                },
                colors = buttonColors(containerColor = Brown),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text("Today", color = Softsoftyellow, fontWeight = FontWeight.SemiBold)
            }
        }


    }
}


@Composable
fun CalendarHeader(
    selectionMode: Boolean,
    onToggleSelection: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Quiet Bloom Logo",
                modifier = Modifier.height(80.dp)
            )
            Image(
                painter = painterResource(
                    if (selectionMode)
                        R.drawable.multi_tool_filled
                    else
                        R.drawable.multi_tool
                ),
                contentDescription = "Select period days",
                modifier = Modifier
                    .size(44.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onToggleSelection()
                    }
            )

        }


        Divider(
            color = Brown,
            thickness = 2.dp,
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(0.9f)
        )


        Divider(
            color = Brown,
            thickness = 2.dp,
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(0.9f)
        )
    }
}

@Composable
fun MonthHeader(month: YearMonth) {

    val title =
        month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { it.uppercase() } + " ${month.year}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            thickness = 2.dp,
            color = Brown,
            modifier = Modifier.fillMaxWidth(0.98f)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Brown,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Divider(
            thickness = 2.dp,
            color = Brown,
            modifier = Modifier.fillMaxWidth(0.98f)
        )
    }
}

@Composable
fun MonthCalendar(
    month: YearMonth,
    navController: NavController,
    bloodflowMap: Map<Long, Int>,
    predictedMap: Map<Long, Int>,
    selectionMode: Boolean,
    selectedDates: MutableSet<LocalDate>,
    ovulationDays: Set<LocalDate>
) {
    val firstDayOfMonth = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDayOffset = (firstDayOfMonth.dayOfWeek.value + 6) % 7

    Column {
        MonthHeader(month)

        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = firstDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(rows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - firstDayOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            DayCell(
                                day = dayNumber,
                                month = month,
                                navController = navController,
                                bloodflowMap = bloodflowMap,
                                predictedMap = predictedMap,
                                selectionMode = selectionMode,
                                selectedDates = selectedDates,
                                ovulationDays = ovulationDays
                            )
                        } else {
                            EmptyCell()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekDayHeader() {

    val days = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach {
            Text(
                text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Brown
            )
        }
    }
}

@Composable
fun RowScope.DayCell(
    day: Int,
    month: YearMonth,
    navController: NavController,
    bloodflowMap: Map<Long, Int>,
    predictedMap: Map<Long, Int>,
    selectionMode: Boolean,
    selectedDates: MutableSet<LocalDate>,
    ovulationDays: Set<LocalDate>
) {
    val futureText = Color(0x993D2B1F)
    val cellShape = RoundedCornerShape(8.dp)

    val date = month.atDay(day)
    val today = LocalDate.now()
    val isToday = date == today


    val isFuture = date.isAfter(today)
    val isClickable = !isFuture

    val dateLong = date
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val realFlow = bloodflowMap[dateLong]
    val predictedFlow = predictedMap[dateLong]

    val isOvulation = ovulationDays.contains(date)
    val isOvulationMinus1 = ovulationDays.contains(date.plusDays(1))
    val isOvulationMinus2 = ovulationDays.contains(date.plusDays(2))

    val flowToShow = realFlow ?: predictedFlow
    val isPredicted = realFlow == null && predictedFlow != null

    val hasRealPeriod = realFlow != null && realFlow > 0

    val fillColor = if (!isFuture) Softsoftyellow else Color.Transparent

    val futureOutline = if (isFuture) {
        Modifier.border(width = 2.dp, color = Softsoftyellow, shape = cellShape)
    } else Modifier

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .background(fillColor, cellShape)
            .then(futureOutline)
            .then(
                if (hasRealPeriod) {
                    Modifier.border(2.dp, RedLight.copy(alpha = 0.5f), cellShape)
                } else Modifier
            )
            .then(
                if (selectionMode && selectedDates.contains(date) && hasRealPeriod) {
                    Modifier.drawBehind {
                        val strokeWidth = 3.dp.toPx()
                        val dashWidth = 10f
                        val dashGap = 6f

                        drawRoundRect(
                            color = Brown,
                            size = size,
                            style = Stroke(
                                width = strokeWidth,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(dashWidth, dashGap)
                                )
                            ),
                            cornerRadius = CornerRadius(16f)
                        )
                    }
                } else Modifier
            )
            .then(
                if (selectionMode && selectedDates.contains(date) && !hasRealPeriod) {
                    Modifier.border(3.dp, RedLight.copy(alpha = 0.5f), cellShape)
                } else Modifier
            )
            .then(
                if (isToday) {
                    Modifier.border(2.dp, Brown, cellShape)
                } else Modifier
            )
            .then(
                if (isClickable) Modifier.clickable {
                    if (selectionMode) {
                        if (selectedDates.contains(date)) {
                            selectedDates.remove(date)
                        } else {
                            selectedDates.add(date)
                        }
                    } else {
                        navController.navigate(Screen.AddEntry.createRoute(date.toString()))
                    }
                } else Modifier
            )
        ,
        contentAlignment = Alignment.Center
    )
    {

        // 🔵 OVULATION VISUALS (background layer)
        if (ovulationDays.isNotEmpty()) {

            // großer Kreis: Ovulationstag
            if (isOvulation) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            color = MoodBrightBlue.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(50)
                        )
                )
            }

            if (isOvulationMinus1 || isOvulationMinus2) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .background(
                            color = MoodBrightBlue.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(50)
                        )
                        .align(Alignment.Center)
                        .padding(bottom = 6.dp)
                )
            }
        }



        flowToShow?.let { value ->
            val imageRes = when (value) {
                1 -> R.drawable.splatter_light
                2 -> R.drawable.splatter_medium
                3 -> R.drawable.splatter_heavy
                else -> null
            }

            imageRes?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    alpha = if (isPredicted) 0.3f else 0.5f
                )
            }
        }

        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                hasRealPeriod -> Brown
                isFuture -> futureText
                else -> Brown
            },
            fontWeight = when{
                hasRealPeriod -> FontWeight.ExtraBold
                isToday -> FontWeight.ExtraBold
                else -> FontWeight.Medium
            }
        )
        if (selectionMode && selectedDates.contains(date)) {
            Text(
                text = if (hasRealPeriod) "–" else "+",
                color = RedLight.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun RowScope.EmptyCell() {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
    )
}
