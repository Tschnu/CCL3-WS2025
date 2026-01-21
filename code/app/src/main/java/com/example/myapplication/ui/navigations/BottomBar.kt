package com.example.myapplication.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.R
import java.time.LocalDate

@Composable
fun BottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ""

    val brown = Color(0xFF3D2B1F)
    val selectedYellow = Color(0xFFFFCB3C)
    val yellowish = Color(0xFFFBF3D3)

    val onHome = currentRoute == Screen.Home.route
    val onAnalyze = currentRoute == Screen.StatisticsPage.route

    Surface(
        color = yellowish,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // ✅ less vertical padding overall
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    // ✅ keep bottom safe area
                    .padding(
                        bottom = WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Calender",
                    selected = onHome,
                    selectedBg = selectedYellow,
                    selectedLabelColor = selectedYellow,
                    unselectedLabelColor = brown,
                    iconRes = if (onHome) R.drawable.calender_filled else R.drawable.calender_lines,
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                BottomNavItem(
                    label = "Add Entry",
                    selected = false,
                    selectedBg = selectedYellow,
                    selectedLabelColor = selectedYellow,
                    unselectedLabelColor = brown,
                    iconRes = R.drawable.plus,
                    onClick = {
                        val today = LocalDate.now().toString()
                        navController.navigate(Screen.AddEntry.createRoute(today))
                    }
                )

                BottomNavItem(
                    label = "Analyze",
                    selected = onAnalyze,
                    selectedBg = selectedYellow,
                    selectedLabelColor = selectedYellow,
                    unselectedLabelColor = brown,
                    iconRes = if (onAnalyze) R.drawable.analyze_filled else R.drawable.analyze_lines,
                    onClick = {
                        navController.navigate(Screen.StatisticsPage.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    selectedBg: Color,
    selectedLabelColor: Color,
    unselectedLabelColor: Color,
    iconRes: Int,
    onClick: () -> Unit
) {
    val bg = if (selected) selectedBg else Color.Transparent
    val labelColor = if (selected) selectedLabelColor else unselectedLabelColor

    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onClick,
                    indication = null,
                interactionSource = remember { MutableInteractionSource() }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ pill keeps width but is less tall
        Box(
            modifier = Modifier
                .width(54.dp)          // keep same "width feel"
                .height(44.dp)         // smaller height than before
                .background(bg, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp) // slightly smaller icon
            )
        }

        // ✅ bring label closer to icon
        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            textAlign = TextAlign.Center
        )
    }
}
