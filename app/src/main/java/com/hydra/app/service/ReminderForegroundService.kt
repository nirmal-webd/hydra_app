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
                app.reminderStateManager.restoreTimersIfNeeded()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterUnlockReceiver()
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

    private fun buildForegroundNotification(): Notification {
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
        return NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hydra")
            .setContentText("Hydration tracking active")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}
