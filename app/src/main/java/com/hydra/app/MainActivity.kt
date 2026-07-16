package com.hydra.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hydra.app.navigation.HydraNavGraph
import com.hydra.app.navigation.HydraRoute
import com.hydra.app.service.ReminderForegroundService
import com.hydra.app.ui.theme.HydraTheme

private data class BottomNavItem(
    val route: HydraRoute,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = HydraRoute.DASHBOARD,
        label = "Dashboard",
        selectedIcon = Icons.Filled.WaterDrop,
        unselectedIcon = Icons.Outlined.WaterDrop
    ),
    BottomNavItem(
        route = HydraRoute.HISTORY,
        label = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    BottomNavItem(
        route = HydraRoute.SETTINGS,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

class MainActivity : ComponentActivity() {

    private var showCustomDialog by mutableStateOf(false)

    // Runtime permission launcher for POST_NOTIFICATIONS (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Result handled passively — service already started regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()

        // Step A: Request notification permission, start service, arm FSM
        requestNotificationPermissionIfNeeded()
        startReminderService()
        (application as HydraApp).bootstrapEngineIfNeeded()

        setContent {
            HydraTheme {
                HydraMainContent(
                    showCustomDialog = showCustomDialog,
                    onCustomDialogDismiss = { showCustomDialog = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        showCustomDialog = intent.getBooleanExtra(EXTRA_SHOW_CUSTOM_DIALOG, false)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun startReminderService() {
        val intent = Intent(this, ReminderForegroundService::class.java).apply {
            action = ReminderForegroundService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
    }

    companion object {
        const val EXTRA_SHOW_CUSTOM_DIALOG = "extra_show_custom_dialog"
    }
}

@Composable
private fun HydraMainContent(
    showCustomDialog: Boolean = false,
    onCustomDialogDismiss: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HydraNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            showCustomDialog = showCustomDialog,
            onCustomDialogDismiss = onCustomDialogDismiss
        )
    }
}
