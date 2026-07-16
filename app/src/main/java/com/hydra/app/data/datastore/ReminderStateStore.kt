package com.hydra.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hydra.app.model.ReminderMetadata
import com.hydra.app.model.ReminderReason
import com.hydra.app.model.ReminderState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.reminderDataStore: DataStore<Preferences> by preferencesDataStore(name = "reminder_engine_state")

/**
 * Persists ReminderState + ReminderMetadata to DataStore.
 * This is the single source of truth for the reminder engine across process restarts.
 */
class ReminderStateStore(private val context: Context) {

    private object Keys {
        // FSM state
        val FSM_STATE = stringPreferencesKey("fsm_state")

        // Metadata
        val LAST_DRINK_TIMESTAMP = longPreferencesKey("last_drink_timestamp")
        val DAILY_WATER_CONSUMED = intPreferencesKey("daily_water_consumed")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val GOAL_REACHED = booleanPreferencesKey("goal_reached")
        val COOLDOWN_ENDS_AT = longPreferencesKey("cooldown_ends_at")
        val SNOOZE_ENDS_AT = longPreferencesKey("snooze_ends_at")
        val SNOOZE_COUNT = intPreferencesKey("snooze_count")
        val PENDING_REMINDER = booleanPreferencesKey("pending_reminder")
        val REMINDER_REASON = stringPreferencesKey("reminder_reason")
        val LAST_REMINDER_SHOWN_AT = longPreferencesKey("last_reminder_shown_at")
        val APP_PACKAGE = stringPreferencesKey("app_package")
    }

    val fsmState: Flow<ReminderState> = context.reminderDataStore.data.map { prefs ->
        prefs[Keys.FSM_STATE]?.let {
            runCatching { ReminderState.valueOf(it) }.getOrDefault(ReminderState.COOLDOWN)
        } ?: ReminderState.COOLDOWN
    }

    val metadata: Flow<ReminderMetadata> = context.reminderDataStore.data.map { prefs ->
        ReminderMetadata(
            lastDrinkTimestamp = prefs[Keys.LAST_DRINK_TIMESTAMP] ?: 0L,
            dailyWaterConsumed = prefs[Keys.DAILY_WATER_CONSUMED] ?: 0,
            dailyGoal = prefs[Keys.DAILY_GOAL] ?: 2000,
            goalReached = prefs[Keys.GOAL_REACHED] ?: false,
            cooldownEndsAt = prefs[Keys.COOLDOWN_ENDS_AT] ?: 0L,
            snoozeEndsAt = prefs[Keys.SNOOZE_ENDS_AT] ?: 0L,
            snoozeCount = prefs[Keys.SNOOZE_COUNT] ?: 0,
            pendingReminder = prefs[Keys.PENDING_REMINDER] ?: false,
            reminderReason = prefs[Keys.REMINDER_REASON]?.let {
                runCatching { ReminderReason.valueOf(it) }.getOrDefault(ReminderReason.COOLDOWN_COMPLETE)
            } ?: ReminderReason.COOLDOWN_COMPLETE,
            appPackage = prefs[Keys.APP_PACKAGE],
            lastReminderShownAt = prefs[Keys.LAST_REMINDER_SHOWN_AT] ?: 0L
        )
    }

    suspend fun save(state: ReminderState, meta: ReminderMetadata) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.FSM_STATE] = state.name
            prefs[Keys.LAST_DRINK_TIMESTAMP] = meta.lastDrinkTimestamp
            prefs[Keys.DAILY_WATER_CONSUMED] = meta.dailyWaterConsumed
            prefs[Keys.DAILY_GOAL] = meta.dailyGoal
            prefs[Keys.GOAL_REACHED] = meta.goalReached
            prefs[Keys.COOLDOWN_ENDS_AT] = meta.cooldownEndsAt
            prefs[Keys.SNOOZE_ENDS_AT] = meta.snoozeEndsAt
            prefs[Keys.SNOOZE_COUNT] = meta.snoozeCount
            prefs[Keys.PENDING_REMINDER] = meta.pendingReminder
            prefs[Keys.REMINDER_REASON] = meta.reminderReason.name
            
            if (meta.appPackage != null) {
                prefs[Keys.APP_PACKAGE] = meta.appPackage
            } else {
                prefs.remove(Keys.APP_PACKAGE)
            }
            
            prefs[Keys.LAST_REMINDER_SHOWN_AT] = meta.lastReminderShownAt
        }
    }

    suspend fun getCurrentState(): ReminderState = fsmState.first()
    suspend fun getCurrentMetadata(): ReminderMetadata = metadata.first()

    suspend fun updateDailyGoal(goalMl: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.DAILY_GOAL] = goalMl
        }
    }
}
