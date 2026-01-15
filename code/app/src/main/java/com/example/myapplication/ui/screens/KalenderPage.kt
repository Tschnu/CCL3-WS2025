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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.RowScope
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.navigation.Screen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
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

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = monthsBefore
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CalendarHeader()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(months) { month ->
                MonthCalendar(month = month, navController = navController)
            }
        }


    }
}

@Composable
fun CalendarHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Quiet Bloom Logo",
            modifier = Modifier
                .height(120.dp)
                .wrapContentWidth()
        )

        Divider(
            color = Color(0xFF3D2B1F),
            thickness = 2.dp,
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(0.9f)
        )
    }
}




@Composable
fun MonthCalendar(
    month: YearMonth,
    navController: NavController
) {
    val today = LocalDate.now()
    val firstDayOfMonth = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDayOffset = (firstDayOfMonth.dayOfWeek.value + 6) % 7

    Column {
        Text(
            text = month.month
                .getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { it.uppercase() } + "${month.year}",
            style = MaterialTheme.typography.bodyLarge,
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
                            DayCell(day = dayNumber, month = month, navController = navController)
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
    navController: NavController
) {
    val date = month.atDay(day)
    val today = LocalDate.now()
    val isClickable = !date.isAfter(today)

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .background(
                color = if (isClickable) Color(0xFFFFFFFF) else Color(0xFFE5E2DC),
                shape = RoundedCornerShape(8.dp)
            )
            .then(
                if (isClickable) Modifier.clickable {
                    navController.navigate(
                        Screen.AddEntry.createRoute(date.toString())
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isClickable) Color.Black else Color.Gray
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