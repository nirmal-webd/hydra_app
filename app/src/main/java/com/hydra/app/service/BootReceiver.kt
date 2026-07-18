package com.hydra.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * Listens for device boot and automatically restarts the background tracking service.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val validActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED
        )

        if (intent.action in validActions) {
            val serviceIntent = Intent(context, ReminderForegroundService::class.java).apply {
                action = ReminderForegroundService.ACTION_START
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
