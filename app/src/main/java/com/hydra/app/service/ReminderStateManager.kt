package com.hydra.app.service

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hydra.app.data.datastore.PreferencesManager
import com.hydra.app.data.datastore.ReminderStateStore
import com.hydra.app.data.repository.ReminderRepository
import com.hydra.app.data.repository.WaterRepository
import com.hydra.app.model.ReminderAction
import com.hydra.app.model.ReminderEvent
import com.hydra.app.model.ReminderLog
import com.hydra.app.model.ReminderReason
import com.hydra.app.model.ReminderState
import com.hydra.app.model.ReminderType
import com.hydra.app.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Single source of truth for the reminder engine.
 *
 * Responsibilities:
 * - Receives events from the outside world (UI, receivers, timers)
 * - Runs them through ReminderReducer (pure FSM)
 * - Persists resulting state + metadata to ReminderStateStore
 * - Executes side effects (timers, notifications, DB writes)
 *
 * Thread safety: all transitions are serialized via a Mutex.
 */
class ReminderStateManager(
    private val context: Context,
    private val stateStore: ReminderStateStore,
    private val waterRepository: WaterRepository,
    private val reminderRepository: ReminderRepository,
    private val preferencesManager: PreferencesManager,
    private val cooldownDurationMs: Long = DEFAULT_COOLDOWN_MS
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val notificationHelper = NotificationHelper(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val DEFAULT_COOLDOWN_MS = 60 * 60 * 1000L // 1 hour

        // Alarm actions
        const val ACTION_COOLDOWN_EXPIRED = "com.hydra.app.ACTION_COOLDOWN_EXPIRED"
        const val ACTION_SNOOZE_EXPIRED = "com.hydra.app.ACTION_SNOOZE_EXPIRED"
        const val ACTION_DAY_RESET = "com.hydra.app.ACTION_DAY_RESET"

        const val ALARM_REQUEST_COOLDOWN = 1001
        const val ALARM_REQUEST_SNOOZE = 1002
    }

    /**
     * The main entry point. All transitions flow through here.
     */
    fun dispatch(event: ReminderEvent) {
        scope.launch {
            mutex.withLock {
                val currentState = stateStore.getCurrentState()
                val currentMeta = stateStore.getCurrentMetadata()
                val isUnlocked = isDeviceUnlocked()

                val result = ReminderReducer.reduce(
                    state = currentState,
                    metadata = currentMeta,
                    event = event,
                    isDeviceUnlocked = isUnlocked,
                    inQuietHours = isQuietHoursNow(),
                    cooldownDurationMs = cooldownDurationMs
                )

                // 1. Persist new state + metadata
                stateStore.save(result.state, result.metadata)

                // 2. Execute side effects
                result.effects.forEach { executeEffect(it, result.metadata) }

                // 3. Write audit log to Room
                logTransition(event, result.state, result.metadata)
            }
        }
    }

    private suspend fun isQuietHoursNow(): Boolean {
        val qStart = kotlinx.coroutines.flow.first(preferencesManager.quietHoursStart)
        val qEnd = kotlinx.coroutines.flow.first(preferencesManager.quietHoursEnd)
        return com.hydra.app.utils.DateUtils.isInQuietHours(qStart, qEnd)
    }

    private suspend fun executeEffect(effect: ReminderEffect, metadata: com.hydra.app.model.ReminderMetadata) {
        when (effect) {
            is ReminderEffect.ShowNotification -> {
                val reminderId = reminderRepository.logReminder(
                    ReminderLog(
                        type = metadata.reminderReason.toType(),
                        action = ReminderAction.SHOWN
                    )
                )
                notificationHelper.showReminderNotification(
                    reminderId = reminderId,
                    title = "Time to hydrate! 💧",
                    message = buildReminderMessage(metadata)
                )
            }

            is ReminderEffect.CancelNotification -> {
                notificationHelper.cancelReminderNotification()
            }

            is ReminderEffect.StartCooldownTimer -> {
                cancelAlarm(ALARM_REQUEST_SNOOZE)
                scheduleAlarm(
                    action = ACTION_COOLDOWN_EXPIRED,
                    triggerAtMs = System.currentTimeMillis() + effect.durationMs,
                    requestCode = ALARM_REQUEST_COOLDOWN
                )
            }

            is ReminderEffect.StartSnoozeTimer -> {
                cancelAlarm(ALARM_REQUEST_COOLDOWN)
                scheduleAlarm(
                    action = ACTION_SNOOZE_EXPIRED,
                    triggerAtMs = System.currentTimeMillis() + effect.durationMs,
                    requestCode = ALARM_REQUEST_SNOOZE
                )
            }

            is ReminderEffect.CancelAllTimers -> {
                cancelAlarm(ALARM_REQUEST_COOLDOWN)
                cancelAlarm(ALARM_REQUEST_SNOOZE)
            }

            is ReminderEffect.LogWater -> {
                // Guard: bootstrap fires WaterLogged(0) to arm the engine — skip DB write
                if (effect.amountMl > 0) {
                    waterRepository.logWater(effect.amountMl, effect.source)
                }
            }
        }
    }

    /**
     * Called on service start to re-arm any timer that was running before process death.
     */
    fun restoreTimersIfNeeded() {
        scope.launch {
            val state = stateStore.getCurrentState()
            val meta = stateStore.getCurrentMetadata()
            val now = System.currentTimeMillis()

            when (state) {
                ReminderState.COOLDOWN -> {
                    val remaining = meta.cooldownEndsAt - now
                    if (remaining > 0) {
                        scheduleAlarm(ACTION_COOLDOWN_EXPIRED, meta.cooldownEndsAt, ALARM_REQUEST_COOLDOWN)
                    } else if (remaining <= 0 && meta.cooldownEndsAt > 0) {
                        // Cooldown expired while we were dead — fire the event now
                        dispatch(ReminderEvent.CooldownExpired)
                    }
                }
                ReminderState.SNOOZED -> {
                    val remaining = meta.snoozeEndsAt - now
                    if (remaining > 0) {
                        scheduleAlarm(ACTION_SNOOZE_EXPIRED, meta.snoozeEndsAt, ALARM_REQUEST_SNOOZE)
                    } else if (remaining <= 0 && meta.snoozeEndsAt > 0) {
                        dispatch(ReminderEvent.SnoozeExpired(0))
                    }
                }
                ReminderState.PENDING -> {
                    // Nothing to restore — waiting for unlock broadcast
                }
                ReminderState.SHOWING -> {
                    // Notification may have been dismissed by system — re-show if still SHOWING
                    val meta2 = stateStore.getCurrentMetadata()
                    notificationHelper.showReminderNotification(
                        reminderId = -1L,
                        title = "Time to hydrate! 💧",
                        message = buildReminderMessage(meta2)
                    )
                }
            }
        }
    }

    private fun isDeviceUnlocked(): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return !km.isKeyguardLocked
    }

    private fun scheduleAlarm(action: String, triggerAtMs: Long, requestCode: Int) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            this.action = action
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        }
    }

    private fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { alarmManager.cancel(it) }
    }

    private fun buildReminderMessage(metadata: com.hydra.app.model.ReminderMetadata): String {
        val remaining = (metadata.dailyGoal - metadata.dailyWaterConsumed).coerceAtLeast(0)
        
        var prefix = ""
        if (metadata.reminderReason == ReminderReason.SOCIAL_APP_USAGE && metadata.appPackage != null) {
            val appName = try {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(metadata.appPackage, 0)
                pm.getApplicationLabel(info).toString()
            } catch (e: Exception) {
                "that app"
            }
            prefix = "You've been using $appName for a while. "
        }

        return if (remaining > 0) {
            "${prefix}You need $remaining ml more to reach your daily goal."
        } else {
            "${prefix}Great job! You've hit your goal. Keep drinking! 🎉"
        }
    }

    private suspend fun logTransition(
        event: ReminderEvent,
        newState: ReminderState,
        metadata: com.hydra.app.model.ReminderMetadata
    ) {
        // Only log SHOWING transitions (when a reminder actually appears)
        if (newState == ReminderState.SHOWING) {
            // Already logged in ShowNotification effect with a proper reminderId
        }
    }

    private fun ReminderReason.toType(): String = when (this) {
        ReminderReason.COOLDOWN_COMPLETE -> ReminderType.UNLOCK
        ReminderReason.SNOOZE_EXPIRED -> ReminderType.UNLOCK
        ReminderReason.SNOOZE_LIMIT -> ReminderType.UNLOCK
        ReminderReason.USER_DISMISSED -> ReminderType.UNLOCK
        ReminderReason.PHONE_UNLOCK -> ReminderType.UNLOCK
        ReminderReason.SOCIAL_APP_USAGE -> ReminderType.APP_USAGE
    }
}
