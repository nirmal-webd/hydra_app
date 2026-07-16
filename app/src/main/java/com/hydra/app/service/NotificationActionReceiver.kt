package com.hydra.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hydra.app.HydraApp
import com.hydra.app.model.ReminderAction
import com.hydra.app.model.WaterLogSource
import com.hydra.app.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HydraApp
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val notificationHelper = NotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            when (intent.action) {
                ACTION_ADD_250ML -> {
                    val waterLogId = app.waterRepository.logWater(250, WaterLogSource.NOTIFICATION_QUICK)
                    if (reminderId != -1L) {
                        app.reminderRepository.linkReminderToWaterLog(reminderId, waterLogId)
                        val reminder = app.reminderRepository.getReminderById(reminderId)
                        if (reminder != null) {
                            app.reminderRepository.updateReminder(reminder.copy(action = ReminderAction.ACCEPTED))
                        }
                    }
                    notificationHelper.cancelReminderNotification()
                }
                ACTION_SNOOZE -> {
                    // Update action to SNOOZED in DB
                    if (reminderId != -1L) {
                        val reminder = app.reminderRepository.getReminderById(reminderId)
                        if (reminder != null) {
                            app.reminderRepository.updateReminder(reminder.copy(action = ReminderAction.SNOOZED))
                        }
                    }
                    notificationHelper.cancelReminderNotification()
                }
            }
        }
    }

    companion object {
        const val ACTION_ADD_250ML = "com.hydra.app.ACTION_ADD_250ML"
        const val ACTION_SNOOZE = "com.hydra.app.ACTION_SNOOZE"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }
}
