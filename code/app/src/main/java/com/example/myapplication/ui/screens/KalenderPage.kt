package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.RowScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.viewModel.EntryViewModel
import java.time.*
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun KalenderPage(navController: NavController) {

    val monthsBefore = 6
    val monthsAfter = 6

    val startMonth = YearMonth.now().minusMonths(monthsBefore.toLong())
    val months = (0..(monthsBefore + monthsAfter)).map {
        startMonth.plusMonths(it.toLong())
    }

    val entryViewModel: EntryViewModel = viewModel()
    val bloodflowMap = entryViewModel.bloodflowByDate.collectAsState().value

    val startDateLong = months.first().atDay(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val endDateLong = months.last().atEndOfMonth()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    LaunchedEffect(startDateLong, endDateLong) {
        entryViewModel.loadBloodflowForRange(startDateLong, endDateLong)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = monthsBefore)

    Column(modifier = Modifier.fillMaxSize()) {
        CalendarHeader()

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
                    bloodflowMap = bloodflowMap
                )
            }
        }
    }
}

@Composable
fun CalendarHeader() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Quiet Bloom Logo",
            modifier = Modifier.height(120.dp)
        )

        Divider(
            color = Color(0xFF3D2B1F),
            thickness = 2.dp,
            modifier = Modifier.padding(top = 4.dp).fillMaxWidth(0.9f)
        )
    }
}

@Composable
fun MonthCalendar(
    month: YearMonth,
    navController: NavController,
    bloodflowMap: Map<Long, Int>
) {
    val firstDayOfMonth = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDayOffset = (firstDayOfMonth.dayOfWeek.value + 6) % 7

    Column {
        Text(
            text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { it.uppercase() } + " ${month.year}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        WeekDayHeader()
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
                                bloodflowMap = bloodflowMap
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
    val days = DayOfWeek.values().toList()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach {
            Text(
                text = it.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun RowScope.DayCell(
    day: Int,
    month: YearMonth,
    navController: NavController,
    bloodflowMap: Map<Long, Int>
) {
    val dateLong = month.atDay(day)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val bloodflow = bloodflowMap[dateLong]

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .background(Color(0xFFEFECE5), RoundedCornerShape(8.dp))
            .clickable {
                navController.navigate(
                    Screen.AddEntry.createRoute(
                        LocalDate.ofEpochDay(dateLong / 86_400_000).toString()
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {

        bloodflow?.let {
            val image = when (it) {
                1 -> R.drawable.little_blood_full
                2 -> R.drawable.middle_blood_full
                3 -> R.drawable.big_blood_full
                else -> null
            }

            image?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(4.dp)
                )
            }
        }

        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

@Composable
fun RowScope.EmptyCell() {
    Box(
        modifier = Modifier.weight(1f).aspectRatio(1f)
    )
}
