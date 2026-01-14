package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.screens.AddEntryPage
import com.example.myapplication.ui.screens.KalenderPage
import com.example.myapplication.ui.screens.PersonalProfilePage
import com.example.myapplication.ui.screens.StatisticsPage
import com.example.myapplication.viewModel.EntryViewModel

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

        composable(
            route = Screen.AddEntry.route,
            arguments = Screen.AddEntry.arguments
        ) { backStackEntry ->

            val date = backStackEntry.arguments?.getString("date")
                ?: error("date argument missing")

            val entryViewModel: EntryViewModel = viewModel()

            AddEntryPage(
                date = date,
                viewModel = entryViewModel
            )
        }

    }
}
