package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object StatisticsPage : Screen("statistics_page", "Statistics", Icons.Filled.Add)
    data object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    data object Home : Screen("home", "Home", Icons.Filled.FavoriteBorder)
}

val bottomTabs = listOf(
    Screen.StatisticsPage,
    Screen.Profile,
    Screen.Home
)
