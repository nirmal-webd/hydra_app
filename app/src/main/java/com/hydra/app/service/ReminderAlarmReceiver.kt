package com.hydra.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hydra.app.HydraApp
import com.hydra.app.model.ReminderEvent

/**
 * Receives alarm broadcasts for cooldown and snooze expiry.
 * Converts the broadcast into a ReminderEvent and dispatches to ReminderStateManager.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HydraApp
        val manager = app.reminderStateManager

        when (intent.action) {
            ReminderStateManager.ACTION_COOLDOWN_EXPIRED -> {
                manager.dispatch(ReminderEvent.CooldownExpired)
            }
            ReminderStateManager.ACTION_SNOOZE_EXPIRED -> {
                // Duration doesn't matter here — snooze already ran
                manager.dispatch(ReminderEvent.SnoozeExpired(0))
            }
            ReminderStateManager.ACTION_DAY_RESET -> {
                manager.dispatch(ReminderEvent.DayReset)
            }
        }
    }
}
