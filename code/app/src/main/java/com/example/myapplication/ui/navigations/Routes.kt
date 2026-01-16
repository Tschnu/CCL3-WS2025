package com.example.myapplication.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myapplication.R

sealed class Screen(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int
) {
    data object StatisticsPage : Screen("statistics_page", "Statistics", R.drawable.magic_crystal_ball)
    data object Profile : Screen("profile", "Profile", R.drawable.crown)
    data object Home : Screen("home", "Home", R.drawable.spa_flower)
    data object Journal : Screen("journal", "Journal", R.drawable.analyze_lines)
    data object AddEntry : Screen(
        route = "add_entry/{date}",
        label = "Add Entry",
        icon = 0
    ) {
        val arguments = listOf(
            navArgument("date") {
                type = NavType.StringType
            }
        )

        fun createRoute(date: String) = "add_entry/$date"
    }

}

val bottomTabs = listOf(
    Screen.StatisticsPage,
    Screen.Home,
    Screen.Profile
)