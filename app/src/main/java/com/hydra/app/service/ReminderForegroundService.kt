package com.hydra.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hydra.app.HydraApp
import kotlinx.coroutines.launch

/**
 * Long-running foreground service that hosts the reminder engine.
 *
 * Responsibilities:
 * - Keeps the process alive for alarm delivery
 * - Registers/unregisters UnlockReceiver for ACTION_USER_PRESENT
 * - Restores any timers that were running before process death
 */
class ReminderForegroundService : Service() {

    private var unlockReceiver: UnlockReceiver? = null

    companion object {
        private const val CHANNEL_ID_FOREGROUND = "hydra_foreground_service"
        private const val NOTIFICATION_ID_FOREGROUND = 2001

        const val ACTION_START = "com.hydra.app.service.START"
        const val ACTION_STOP = "com.hydra.app.service.STOP"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID_FOREGROUND, buildForegroundNotification())
                registerUnlockReceiver()
                val app = applicationContext as HydraApp
                startUsageMonitor(app)
                app.reminderStateManager.restoreTimersIfNeeded()
                startNotificationUpdater(app)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterUnlockReceiver()
        stopUsageMonitor()
        notificationJob?.cancel()
    }

    private fun registerUnlockReceiver() {
        if (unlockReceiver != null) return
        unlockReceiver = UnlockReceiver()
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)
    }

    private fun unregisterUnlockReceiver() {
        unlockReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
            unlockReceiver = null
        }
    }

    private var usageMonitor: UsageMonitor? = null
    private var notificationJob: kotlinx.coroutines.Job? = null

    private fun startUsageMonitor(app: HydraApp) {
        if (usageMonitor == null) {
            usageMonitor = UsageMonitor(
                context = this,
                preferencesManager = app.preferencesManager,
                reminderStateManager = app.reminderStateManager,
                scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
            )
        }
        usageMonitor?.start()
    }

    private fun stopUsageMonitor() {
        usageMonitor?.stop()
        usageMonitor = null
    }

    private fun startNotificationUpdater(app: HydraApp) {
        notificationJob?.cancel()
        notificationJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            app.reminderStateStore.metadata.collect { meta ->
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIFICATION_ID_FOREGROUND, buildForegroundNotification(meta))
            }
        }
    }

    private fun buildForegroundNotification(meta: com.hydra.app.model.ReminderMetadata? = null): Notification {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID_FOREGROUND) == null) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_FOREGROUND,
                    "Hydra Background",
                    NotificationManager.IMPORTANCE_MIN
                ).apply { description = "Keeps hydration tracking active" }
            )
        }
        
        val contentText = if (meta != null) {
            if (meta.goalReached) {
                "Daily goal reached! 🎉 (${meta.dailyWaterConsumed}/${meta.dailyGoal} ml)"
            } else {
                "${meta.dailyWaterConsumed} / ${meta.dailyGoal} ml consumed today"
            }
        } else {
            "Hydration tracking active"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hydra")
            .setContentText(contentText)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}
