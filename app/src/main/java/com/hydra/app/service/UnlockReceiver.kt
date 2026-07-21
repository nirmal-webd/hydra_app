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
        val app = context.applicationContext as HydraApp
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                app.reminderStateManager.dispatch(ReminderEvent.PhoneUnlocked)
            }
            Intent.ACTION_SCREEN_ON -> {
                val km = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
                if (!km.isKeyguardLocked) {
                    app.reminderStateManager.dispatch(ReminderEvent.PhoneUnlocked)
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                app.reminderStateManager.dispatch(ReminderEvent.ScreenTurnedOff)
            }
        }
    }
}
