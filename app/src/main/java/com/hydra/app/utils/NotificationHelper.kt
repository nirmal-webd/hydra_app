package com.hydra.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.hydra.app.MainActivity
import com.hydra.app.service.NotificationActionReceiver

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Hydration Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Context-aware hydration reminders"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminderNotification(reminderId: Long, title: String, message: String, snoozeDurationMins: Int) {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Drank Water" action
        val drankIntent = broadcastPendingIntent(NotificationActionReceiver.ACTION_ADD_250ML, 10)

        // Snooze action
        val snoozeAction = when (snoozeDurationMins) {
            5 -> NotificationActionReceiver.ACTION_SNOOZE_5
            15 -> NotificationActionReceiver.ACTION_SNOOZE_15
            else -> NotificationActionReceiver.ACTION_SNOOZE_10
        }
        val snoozeIntent = broadcastPendingIntent(snoozeAction, 11)

        // "Not Now" / Dismiss
        val dismissIntent = broadcastPendingIntent(NotificationActionReceiver.ACTION_DISMISS, 12)

        // Custom amount (opens app)
        val customIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_SHOW_CUSTOM_DIALOG, true)
        }
        val customPendingIntent = PendingIntent.getActivity(
            context, 13, customIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setOngoing(true) // Sticky notification
            .setAutoCancel(false)  // Don't dismiss on tap — user must respond
            .addAction(0, "💧 250ml", drankIntent)
            .addAction(0, "✏️ Custom", customPendingIntent)
            .addAction(0, "⏰ $snoozeDurationMins min", snoozeIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    fun cancelReminderNotification() {
        notificationManager.cancel(NOTIFICATION_ID_REMINDER)
    }

    private fun broadcastPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders_channel"
        const val NOTIFICATION_ID_REMINDER = 1001
    }
}
