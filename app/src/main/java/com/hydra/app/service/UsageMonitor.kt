package com.hydra.app.service

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.hydra.app.data.datastore.PreferencesManager
import com.hydra.app.model.ReminderEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class UsageMonitor(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val reminderStateManager: ReminderStateManager,
    private val scope: CoroutineScope
) {
    private var monitorJob: Job? = null
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    // Track consecutive minutes a monitored app is in foreground
    private var consecutiveForegroundMinutes = 0
    private var lastForegroundApp: String? = null

    fun start() {
        if (monitorJob != null) return
        monitorJob = scope.launch {
            while (isActive) {
                checkUsage()
                delay(60_000L) // Poll every 60 seconds
            }
        }
    }

    fun stop() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private suspend fun checkUsage() {
        try {
            val appRemindersEnabled = preferencesManager.appRemindersEnabled.first()
            if (!appRemindersEnabled) {
                consecutiveForegroundMinutes = 0
                return
            }

            // Get current monitored apps and threshold
            val monitoredApps = preferencesManager.monitoredApps.first()
            if (monitoredApps.isEmpty()) {
                consecutiveForegroundMinutes = 0
                return
            }
            
            val thresholdMinutes = preferencesManager.appDurationMinutes.first()

            val now = System.currentTimeMillis()
            // Query the last 2 minutes to reliably get the current foreground app
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 120_000,
                now
            )

            if (stats.isNullOrEmpty()) {
                consecutiveForegroundMinutes = 0
                return
            }

            // Find the app with the most recent lastTimeUsed
            val currentApp = stats.maxByOrNull { it.lastTimeUsed }?.packageName

            if (currentApp != null && monitoredApps.contains(currentApp)) {
                if (currentApp == lastForegroundApp) {
                    consecutiveForegroundMinutes++
                } else {
                    lastForegroundApp = currentApp
                    consecutiveForegroundMinutes = 1
                }

                if (consecutiveForegroundMinutes >= thresholdMinutes) {
                    // Threshold reached! Dispatch event.
                    Log.d("UsageMonitor", "Usage threshold reached for $currentApp")
                    reminderStateManager.dispatch(ReminderEvent.AppUsageDetected(currentApp))
                    // Reset to avoid spamming
                    consecutiveForegroundMinutes = 0
                }
            } else {
                lastForegroundApp = null
                consecutiveForegroundMinutes = 0
            }
        } catch (e: Exception) {
            Log.e("UsageMonitor", "Error checking usage stats", e)
        }
    }
}
