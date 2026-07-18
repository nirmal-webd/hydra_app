package com.hydra.app.service

import com.hydra.app.model.ReminderEvent
import com.hydra.app.model.ReminderMetadata
import com.hydra.app.model.ReminderReason
import com.hydra.app.model.ReminderState
import com.hydra.app.model.WaterLogSource

const val MAX_SNOOZE_COUNT = 3

/**
 * The result of a single FSM transition.
 *
 * @param state      The new behavioral state.
 * @param metadata   Updated metadata snapshot.
 * @param effects    Side effects to execute after persisting state (fire-and-forget).
 */
data class TransitionResult(
    val state: ReminderState,
    val metadata: ReminderMetadata,
    val effects: List<ReminderEffect>
)

/**
 * Side effects that the ReminderStateManager executes after a transition.
 * Keeping them as a sealed class makes the reducer a pure function (testable without Android).
 */
sealed class ReminderEffect {
    object ShowNotification : ReminderEffect()
    object CancelNotification : ReminderEffect()
    data class StartCooldownTimer(val durationMs: Long) : ReminderEffect()
    data class StartSnoozeTimer(val durationMs: Long) : ReminderEffect()
    object CancelAllTimers : ReminderEffect()
    data class LogWater(val amountMl: Int, val source: String) : ReminderEffect()
}

/**
 * Pure-function FSM reducer.
 *
 * Takes (currentState, metadata, event, deviceUnlocked, cooldownDurationMs)
 * and returns (nextState, updatedMetadata, sideEffects).
 *
 * NEVER reads system state directly. All context is passed as parameters.
 */
object ReminderReducer {

    fun reduce(
        state: ReminderState,
        metadata: ReminderMetadata,
        event: ReminderEvent,
        isDeviceUnlocked: Boolean,
        inQuietHours: Boolean,
        cooldownDurationMs: Long,
        now: Long = System.currentTimeMillis()
    ): TransitionResult {

        // ─── GLOBAL TRANSITIONS ───────────────────────────────────────────────
        // WaterLogged and DayReset override any current state unconditionally.

        if (event is ReminderEvent.WaterLogged) {
            val newConsumed = metadata.dailyWaterConsumed + event.amountMl
            val goalReached = newConsumed >= metadata.dailyGoal
            return TransitionResult(
                state = ReminderState.COOLDOWN,
                metadata = metadata.copy(
                    lastDrinkTimestamp = now,
                    dailyWaterConsumed = newConsumed,
                    goalReached = goalReached,
                    snoozeCount = 0,
                    pendingReminder = false,
                    cooldownEndsAt = now + cooldownDurationMs
                ),
                effects = listOf(
                    ReminderEffect.LogWater(event.amountMl, event.source),
                    ReminderEffect.CancelAllTimers,
                    ReminderEffect.CancelNotification,
                    ReminderEffect.StartCooldownTimer(cooldownDurationMs)
                )
            )
        }

        if (event is ReminderEvent.WaterLogDeleted) {
            val newConsumed = (metadata.dailyWaterConsumed - event.amountMl).coerceAtLeast(0)
            val goalReached = newConsumed >= metadata.dailyGoal
            return TransitionResult(
                state = state, // preserve current state
                metadata = metadata.copy(
                    dailyWaterConsumed = newConsumed,
                    goalReached = goalReached
                ),
                // No effect needed, DashboardViewModel already deleted it from Room
                effects = emptyList()
            )
        }

        if (event is ReminderEvent.DayReset) {
            return TransitionResult(
                state = ReminderState.COOLDOWN,
                metadata = metadata.copy(
                    dailyWaterConsumed = 0,
                    goalReached = false,
                    snoozeCount = 0,
                    pendingReminder = false,
                    cooldownEndsAt = now + cooldownDurationMs
                ),
                effects = listOf(
                    ReminderEffect.CancelAllTimers,
                    ReminderEffect.CancelNotification,
                    ReminderEffect.StartCooldownTimer(cooldownDurationMs)
                )
            )
        }

        // ─── STATE-SPECIFIC TRANSITIONS ───────────────────────────────────────

        return when (state) {

            ReminderState.COOLDOWN -> reduceCooldown(metadata, event, isDeviceUnlocked, inQuietHours, cooldownDurationMs, now)

            ReminderState.PENDING -> reducePending(metadata, event, isDeviceUnlocked, inQuietHours, cooldownDurationMs, now)

            ReminderState.SHOWING -> reduceShowing(metadata, event, isDeviceUnlocked, inQuietHours, cooldownDurationMs, now)

            ReminderState.SNOOZED -> reduceSnoozed(metadata, event, isDeviceUnlocked, inQuietHours, now)
        }
    }

    // ─── COOLDOWN ─────────────────────────────────────────────────────────────

    private fun reduceCooldown(
        metadata: ReminderMetadata,
        event: ReminderEvent,
        isDeviceUnlocked: Boolean,
        inQuietHours: Boolean,
        cooldownDurationMs: Long,
        now: Long
    ): TransitionResult {
        return when (event) {
            is ReminderEvent.CooldownExpired -> {
                when {
                    metadata.goalReached -> {
                        // Goal reached — suppress and restart cooldown quietly
                        TransitionResult(
                            state = ReminderState.COOLDOWN,
                            metadata = metadata.copy(cooldownEndsAt = now + cooldownDurationMs),
                            effects = listOf(ReminderEffect.StartCooldownTimer(cooldownDurationMs))
                        )
                    }
                    isDeviceUnlocked && !inQuietHours -> {
                        // Unlocked → show immediately
                        TransitionResult(
                            state = ReminderState.SHOWING,
                            metadata = metadata.copy(
                                lastReminderShownAt = now,
                                reminderReason = ReminderReason.COOLDOWN_COMPLETE
                            ),
                            effects = listOf(ReminderEffect.ShowNotification)
                        )
                    }
                    else -> {
                        // Locked → queue for next unlock
                        TransitionResult(
                            state = ReminderState.PENDING,
                            metadata = metadata.copy(
                                pendingReminder = true,
                                reminderReason = ReminderReason.COOLDOWN_COMPLETE
                            ),
                            effects = emptyList()
                        )
                    }
                }
            }
            is ReminderEvent.PhoneUnlocked -> noOp(ReminderState.COOLDOWN, metadata)
            else -> noOp(ReminderState.COOLDOWN, metadata)
        }
    }

    // ─── PENDING ──────────────────────────────────────────────────────────────

    private fun reducePending(
        metadata: ReminderMetadata,
        event: ReminderEvent,
        isDeviceUnlocked: Boolean,
        inQuietHours: Boolean,
        cooldownDurationMs: Long,
        now: Long
    ): TransitionResult {
        return when (event) {
            is ReminderEvent.PhoneUnlocked -> {
                when {
                    metadata.goalReached -> {
                        // Goal reached — suppress and restart cooldown
                        TransitionResult(
                            state = ReminderState.COOLDOWN,
                            metadata = metadata.copy(
                                pendingReminder = false,
                                cooldownEndsAt = now + cooldownDurationMs
                            ),
                            effects = listOf(ReminderEffect.StartCooldownTimer(cooldownDurationMs))
                        )
                    }
                    inQuietHours -> {
                        // Phone unlocked but it's quiet hours — stay in PENDING
                        noOp(ReminderState.PENDING, metadata)
                    }
                    else -> {
                        // Unlocked with pending reminder → show it
                        TransitionResult(
                            state = ReminderState.SHOWING,
                            metadata = metadata.copy(
                                pendingReminder = false,
                                lastReminderShownAt = now
                            ),
                            effects = listOf(ReminderEffect.ShowNotification)
                        )
                    }
                }
            }
            is ReminderEvent.AppUsageDetected -> {
                when {
                    metadata.goalReached -> {
                        TransitionResult(
                            state = ReminderState.COOLDOWN,
                            metadata = metadata.copy(
                                pendingReminder = false,
                                cooldownEndsAt = now + cooldownDurationMs
                            ),
                            effects = listOf(ReminderEffect.StartCooldownTimer(cooldownDurationMs))
                        )
                    }
                    inQuietHours -> {
                        // Suppress app usage reminder during quiet hours (no-op)
                        noOp(ReminderState.PENDING, metadata)
                    }
                    else -> {
                        TransitionResult(
                            state = ReminderState.SHOWING,
                            metadata = metadata.copy(
                                pendingReminder = false,
                                reminderReason = ReminderReason.SOCIAL_APP_USAGE,
                                appPackage = event.appPackage,
                                lastReminderShownAt = now
                            ),
                            effects = listOf(ReminderEffect.ShowNotification)
                        )
                    }
                }
            }
            else -> noOp(ReminderState.PENDING, metadata)
        }
    }

    // ─── SHOWING ──────────────────────────────────────────────────────────────

    private fun reduceShowing(
        metadata: ReminderMetadata,
        event: ReminderEvent,
        isDeviceUnlocked: Boolean,
        inQuietHours: Boolean,
        cooldownDurationMs: Long,
        now: Long
    ): TransitionResult {
        return when (event) {
            is ReminderEvent.ReminderAccepted -> {
                // "Drank Water" tapped on notification — log 250ml quick add
                val newConsumed = metadata.dailyWaterConsumed + 250
                TransitionResult(
                    state = ReminderState.COOLDOWN,
                    metadata = metadata.copy(
                        lastDrinkTimestamp = now,
                        dailyWaterConsumed = newConsumed,
                        goalReached = newConsumed >= metadata.dailyGoal,
                        snoozeCount = 0,
                        pendingReminder = false,
                        cooldownEndsAt = now + cooldownDurationMs
                    ),
                    effects = listOf(
                        ReminderEffect.LogWater(250, WaterLogSource.NOTIFICATION_QUICK),
                        ReminderEffect.CancelNotification,
                        ReminderEffect.StartCooldownTimer(cooldownDurationMs)
                    )
                )
            }

            is ReminderEvent.ReminderSnoozed -> {
                val newSnoozeCount = metadata.snoozeCount + 1
                val snoozeDurationMs = event.durationMinutes * 60_000L
                if (newSnoozeCount < MAX_SNOOZE_COUNT) {
                    // Normal snooze
                    TransitionResult(
                        state = ReminderState.SNOOZED,
                        metadata = metadata.copy(
                            snoozeCount = newSnoozeCount,
                            snoozeEndsAt = now + snoozeDurationMs
                        ),
                        effects = listOf(
                            ReminderEffect.CancelNotification,
                            ReminderEffect.StartSnoozeTimer(snoozeDurationMs)
                        )
                    )
                } else {
                    // 3rd snooze — back to PENDING, wait for next unlock. Reset count.
                    TransitionResult(
                        state = ReminderState.PENDING,
                        metadata = metadata.copy(
                            snoozeCount = 0,
                            pendingReminder = true,
                            reminderReason = ReminderReason.SNOOZE_LIMIT
                        ),
                        effects = listOf(ReminderEffect.CancelNotification)
                    )
                }
            }

            is ReminderEvent.ReminderDismissed -> {
                // "Not Now" — wait until next unlock, do not accumulate
                TransitionResult(
                    state = ReminderState.PENDING,
                    metadata = metadata.copy(
                        pendingReminder = true,
                        reminderReason = ReminderReason.USER_DISMISSED
                    ),
                    effects = listOf(ReminderEffect.CancelNotification)
                )
            }

            is ReminderEvent.PhoneUnlocked -> noOp(ReminderState.SHOWING, metadata)
            else -> noOp(ReminderState.SHOWING, metadata)
        }
    }

    // ─── SNOOZED ──────────────────────────────────────────────────────────────

    private fun reduceSnoozed(
        metadata: ReminderMetadata,
        event: ReminderEvent,
        isDeviceUnlocked: Boolean,
        inQuietHours: Boolean,
        now: Long
    ): TransitionResult {
        return when (event) {
            is ReminderEvent.SnoozeExpired -> {
                if (isDeviceUnlocked && !inQuietHours) {
                    TransitionResult(
                        state = ReminderState.SHOWING,
                        metadata = metadata.copy(
                            lastReminderShownAt = now,
                            reminderReason = ReminderReason.SNOOZE_EXPIRED
                        ),
                        effects = listOf(ReminderEffect.ShowNotification)
                    )
                } else {
                    TransitionResult(
                        state = ReminderState.PENDING,
                        metadata = metadata.copy(
                            pendingReminder = true,
                            reminderReason = ReminderReason.SNOOZE_EXPIRED
                        ),
                        effects = emptyList()
                    )
                }
            }
            is ReminderEvent.PhoneUnlocked -> noOp(ReminderState.SNOOZED, metadata) // snooze timer still running
            else -> noOp(ReminderState.SNOOZED, metadata)
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private fun noOp(state: ReminderState, metadata: ReminderMetadata) =
        TransitionResult(state, metadata, emptyList())
}
