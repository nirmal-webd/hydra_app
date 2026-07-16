package com.hydra.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hydra.app.ui.screens.DashboardScreen
import com.hydra.app.ui.screens.HistoryScreen
import com.hydra.app.ui.screens.SettingsScreen

enum class HydraRoute(val route: String) {
    DASHBOARD("dashboard"),
    HISTORY("history"),
    SETTINGS("settings")
}

@Composable
fun HydraNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HydraRoute.DASHBOARD.route,
        modifier = modifier
    ) {
        composable(HydraRoute.DASHBOARD.route) {
            DashboardScreen()
        }
        composable(HydraRoute.HISTORY.route) {
            HistoryScreen()
        }
        composable(HydraRoute.SETTINGS.route) {
            SettingsScreen()
        }
    }
}
