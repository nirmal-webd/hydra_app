package com.hydra.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hydra.app.HydraApp
import com.hydra.app.model.ReminderEvent

/**
 * Listens for ACTION_USER_PRESENT (device unlocked) and dispatches PhoneUnlocked to the FSM.
 * Registered dynamically in ReminderForegroundService — not in manifest — so it only fires
 * when the service is alive.
 */
class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val app = context.applicationContext as HydraApp
            app.reminderStateManager.dispatch(ReminderEvent.PhoneUnlocked)
        }
    }
}
