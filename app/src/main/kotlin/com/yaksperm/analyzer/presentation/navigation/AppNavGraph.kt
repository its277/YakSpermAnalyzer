package com.yaksperm.analyzer.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.yaksperm.analyzer.presentation.screens.history.HistoryScreen
import com.yaksperm.analyzer.presentation.screens.home.HomeScreen
import com.yaksperm.analyzer.presentation.screens.newanalysis.NewAnalysisScreen
import com.yaksperm.analyzer.presentation.screens.processing.ProcessingScreen
import com.yaksperm.analyzer.presentation.screens.results.ResultsScreen
import com.yaksperm.analyzer.presentation.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("new_analysis") {
            NewAnalysisScreen(navController = navController)
        }

        composable(
            route = "processing/{videoUri}",
            arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
        ) { backStack ->
            val encodedUri = backStack.arguments?.getString("videoUri") ?: return@composable
            val uri = Uri.parse(Uri.decode(encodedUri))
            ProcessingScreen(navController = navController, videoUri = uri)
        }

        composable(
            route = "results/{resultId}",
            arguments = listOf(navArgument("resultId") { type = NavType.LongType })
        ) { backStack ->
            val resultId = backStack.arguments?.getLong("resultId") ?: return@composable
            ResultsScreen(navController = navController, resultId = resultId)
        }

        composable("history") {
            HistoryScreen(navController = navController)
        }

        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}
