package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.screens.AddEntryPage
import com.example.myapplication.ui.screens.KalenderPage
import com.example.myapplication.ui.screens.PersonalProfilePage
import com.example.myapplication.ui.screens.StatisticsPage

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) { KalenderPage(navController)}
        composable(Screen.StatisticsPage.route) { StatisticsPage() }  // Changed this line!
        composable(Screen.Profile.route) { PersonalProfilePage() }

        composable(Screen.AddEntry.route) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            AddEntryPage(date = date)
        }
    }
}
