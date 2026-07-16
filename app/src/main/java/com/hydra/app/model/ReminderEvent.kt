package com.hydra.app.model

/**
 * All events that drive the reminder FSM.
 * Transitions happen ONLY in response to these events — no ad-hoc state mutation elsewhere.
 */
sealed class ReminderEvent {
    // Global — handled from ANY state
    data class WaterLogged(val amountMl: Int, val source: String = WaterLogSource.MANUAL) : ReminderEvent()
    object DayReset : ReminderEvent()

    // Timer callbacks
    object CooldownExpired : ReminderEvent()
    data class SnoozeExpired(val snoozeDurationMinutes: Int) : ReminderEvent()

    // Device events
    object PhoneUnlocked : ReminderEvent()

    // Notification actions (user response)
    object ReminderAccepted : ReminderEvent()     // "Drank Water"
    data class ReminderSnoozed(val durationMinutes: Int) : ReminderEvent()
    object ReminderDismissed : ReminderEvent()    // "Not Now"
}


