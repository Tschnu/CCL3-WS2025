package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.viewModel.EntryViewModel

@Composable
fun StatisticsPage(navController: NavController) {
    val entryViewModel: EntryViewModel = viewModel()
    val stats = entryViewModel.periodStats.collectAsState().value

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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Analyze",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Brown
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ✅ Button looks the same, but icon left + text centered
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
                        // Centered text
                        Text(
                            text = "Journal Entries",
                            style = MaterialTheme.typography.titleLarge,
                            color = Softsoftyellow,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        // Icon pinned to the left
                        Icon(
                            painter = painterResource(id = R.drawable.journal),
                            contentDescription = "Journal",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                                .size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Brown, thickness = 2.dp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Averages in days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Brown
                )

                Spacer(modifier = Modifier.height(10.dp))

                Divider(color = Brown, thickness = 2.dp)

                Spacer(modifier = Modifier.height(18.dp))

                // ✅ ONLY these two (ignore Cycle Fluctuations)
                StatRow(
                    label = "Cycle length",
                    value = "${stats.avgCycleDays}"
                )

                Spacer(modifier = Modifier.height(14.dp))

                StatRow(
                    label = "Average Period",
                    value = "${stats.avgPeriodDays}"
                )
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Brown,
            modifier = Modifier.weight(1f)
        )

        Surface(
            color = Brown,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 76.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
