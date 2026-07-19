package com.hydra.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import com.hydra.app.ui.screens.DashboardScreen
import com.hydra.app.ui.screens.HistoryScreen
import com.hydra.app.ui.screens.SettingsScreen
import com.hydra.app.ui.screens.OnboardingScreen

enum class HydraRoute(val route: String) {
    ONBOARDING("onboarding"),
    DASHBOARD("dashboard"),
    HISTORY("history"),
    SETTINGS("settings")
}

@Composable
fun HydraNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = HydraRoute.DASHBOARD.route,
    showCustomDialog: Boolean = false,
    onCustomDialogDismiss: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(500)) },
        exitTransition = { fadeOut(animationSpec = tween(500)) },
        popEnterTransition = { fadeIn(animationSpec = tween(500)) },
        popExitTransition = { fadeOut(animationSpec = tween(500)) }
    ) {
        composable(HydraRoute.ONBOARDING.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(HydraRoute.DASHBOARD.route) {
                        popUpTo(HydraRoute.ONBOARDING.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(HydraRoute.DASHBOARD.route) {
            DashboardScreen(
                initialShowCustomDialog = showCustomDialog,
                onCustomDialogDismiss = onCustomDialogDismiss
            )
        }
        composable(HydraRoute.HISTORY.route) {
            HistoryScreen()
        }
        composable(HydraRoute.SETTINGS.route) {
            SettingsScreen()
        }
    }
}
