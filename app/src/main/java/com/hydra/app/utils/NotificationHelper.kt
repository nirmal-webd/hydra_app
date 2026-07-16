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

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            "Hydration Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to drink water"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminderNotification(reminderId: Long, title: String, message: String) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: 250ml
        val add250Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_ADD_250ML
            putExtra(NotificationActionReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val add250PendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            add250Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Custom (Launch MainActivity)
        val customIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.EXTRA_SHOW_CUSTOM_DIALOG, true)
            putExtra(NotificationActionReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val customPendingIntent = PendingIntent.getActivity(
            context,
            2,
            customIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze / Later
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Basic icon for now
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .addAction(0, "\uD83D\uDCA7 250 ml", add250PendingIntent)
            .addAction(0, "\u2615 Custom", customPendingIntent)
            .addAction(0, "\u23F0 Later", snoozePendingIntent)

        notificationManager.notify(NOTIFICATION_ID_REMINDER, builder.build())
    }

    fun cancelReminderNotification() {
        notificationManager.cancel(NOTIFICATION_ID_REMINDER)
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders_channel"
        const val NOTIFICATION_ID_REMINDER = 1001
    }
}
