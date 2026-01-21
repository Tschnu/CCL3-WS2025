package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.Brown
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

        // ✅ Your normal page content
        Column(modifier = Modifier.fillMaxSize()) {

            CalendarHeader(
                selectionMode = selectionMode,
                onFlowerClick = {
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
                            entryViewModel.savePeriodDays(selectedDates.toList())
                            selectedDates.clear()
                            selectionMode = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Brown
                        )
                    ) {
                        Text(text = "Period",
                            color = Softsoftyellow)
                    }

                    Button(
                        onClick = {
                            selectedDates.clear()
                            selectionMode = false
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(2.dp, Color.Red)
                    ) {
                        Text("Cancel", color = Color.Red)
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
                        selectedDates = selectedDates
                    )
                }
            }
        }

        // ✅ Floating "Today" button (only when current month is NOT visible)
        if (!isCurrentMonthVisible) {
            androidx.compose.material3.Button(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(currentMonthIndex)
                    }
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Brown),
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
    onFlowerClick: () -> Unit
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
                painter = painterResource(R.drawable.flower_4),
                contentDescription = "Select period days",
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = if (selectionMode) Brown.copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onFlowerClick() }
                    .padding(6.dp)
                    .alpha(if (selectionMode) 0.6f else 1f)
            )
        }


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
    val brown = Color(0xFF3D2B1F)

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
            color = brown,
            modifier = Modifier.fillMaxWidth(0.98f)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = brown,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Divider(
            thickness = 2.dp,
            color = brown,
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
    selectedDates: MutableSet<LocalDate>
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
                                selectedDates = selectedDates
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
    selectedDates: MutableSet<LocalDate>
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
                    Modifier.border(2.dp, Color.Red, cellShape)
                } else Modifier
            )
            .then(
                if (selectionMode && selectedDates.contains(date)) {
                    Modifier.border(3.dp, Color.Red, cellShape)
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
        flowToShow?.let { value ->
            val imageRes = when (value) {
                1 -> R.drawable.little_blood_full
                2 -> R.drawable.middle_blood_full
                3 -> R.drawable.big_blood_full
                else -> null
            }

            imageRes?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    alpha = if (isPredicted) 0.5f else 1f
                )
            }
        }

        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isFuture) futureText else Brown,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,


        )
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
