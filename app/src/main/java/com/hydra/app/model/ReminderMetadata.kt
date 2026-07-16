package com.hydra.app.model

/**
 * All contextual data for the reminder engine.
 * The FSM only stores ReminderState; everything else lives here.
 * Persisted via DataStore so it survives process death.
 */
data class ReminderMetadata(
    // Water intake tracking
    val lastDrinkTimestamp: Long = 0L,
    val dailyWaterConsumed: Int = 0,
    val dailyGoal: Int = 2000,
    val goalReached: Boolean = false,

    // Cooldown
    val cooldownEndsAt: Long = 0L,

    // Snooze
    val snoozeEndsAt: Long = 0L,
    val snoozeCount: Int = 0,

    // Pending reminder
    val pendingReminder: Boolean = false,
    val reminderReason: ReminderReason = ReminderReason.COOLDOWN_COMPLETE,
    val appPackage: String? = null,

    // Audit
    val lastReminderShownAt: Long = 0L
)
