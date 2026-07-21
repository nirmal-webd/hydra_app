package com.hydra.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hydra.app.HydraApp
import com.hydra.app.model.ReminderEvent

/**
 * Handles notification action button taps.
 * Converts taps to FSM events and dispatches to ReminderStateManager.
 * No direct DB writes here — the FSM handles that via effects.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HydraApp
        val manager = app.reminderStateManager

        when (intent.action) {
            ACTION_ADD_250ML -> {
                // "Drank Water" action — ReminderAccepted logs water via FSM effect
                manager.dispatch(ReminderEvent.ReminderAccepted)
            }
            ACTION_SNOOZE_5 -> manager.dispatch(ReminderEvent.ReminderSnoozed(5))
            ACTION_SNOOZE_10 -> manager.dispatch(ReminderEvent.ReminderSnoozed(10))
            ACTION_SNOOZE_15 -> manager.dispatch(ReminderEvent.ReminderSnoozed(15))
            ACTION_SNOOZE_30 -> manager.dispatch(ReminderEvent.ReminderSnoozed(30))
            ACTION_DISMISS -> manager.dispatch(ReminderEvent.ReminderDismissed)
            ACTION_SWIPE_DISMISS -> manager.dispatch(ReminderEvent.ReminderSwiped)
        }
    }

    companion object {
        const val ACTION_ADD_250ML = "com.hydra.app.ACTION_ADD_250ML"
        const val ACTION_SNOOZE_5 = "com.hydra.app.ACTION_SNOOZE_5"
        const val ACTION_SNOOZE_10 = "com.hydra.app.ACTION_SNOOZE_10"
        const val ACTION_SNOOZE_15 = "com.hydra.app.ACTION_SNOOZE_15"
        const val ACTION_SNOOZE_30 = "com.hydra.app.ACTION_SNOOZE_30"
        const val ACTION_DISMISS = "com.hydra.app.ACTION_DISMISS"
        const val ACTION_SWIPE_DISMISS = "com.hydra.app.ACTION_SWIPE_DISMISS"
        const val ACTION_SNOOZE = "com.hydra.app.ACTION_SNOOZE" // legacy compat
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }
}
