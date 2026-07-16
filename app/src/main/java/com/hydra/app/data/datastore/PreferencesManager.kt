package com.hydra.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hydra_preferences")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val COOLDOWN_MINUTES = intPreferencesKey("cooldown_minutes")
        val APP_DURATION_MINUTES = intPreferencesKey("app_duration_minutes")
        val QUIET_HOURS_START = stringPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = stringPreferencesKey("quiet_hours_end")
        val UNLOCK_REMINDERS_ENABLED = booleanPreferencesKey("unlock_reminders_enabled")
        val APP_REMINDERS_ENABLED = booleanPreferencesKey("app_reminders_enabled")
        val MONITORED_APPS = stringSetPreferencesKey("monitored_apps")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    object Defaults {
        const val DAILY_GOAL_ML = 2000
        const val COOLDOWN_MINUTES = 30
        const val APP_DURATION_MINUTES = 15
        const val QUIET_HOURS_START = "22:00"
        const val QUIET_HOURS_END = "07:00"
        const val UNLOCK_REMINDERS_ENABLED = true
        const val APP_REMINDERS_ENABLED = true
        val MONITORED_APPS: Set<String> = emptySet()
        const val ONBOARDING_COMPLETED = false
    }

    val dailyGoalMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAILY_GOAL_ML] ?: Defaults.DAILY_GOAL_ML
    }

    val cooldownMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.COOLDOWN_MINUTES] ?: Defaults.COOLDOWN_MINUTES
    }

    val appDurationMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.APP_DURATION_MINUTES] ?: Defaults.APP_DURATION_MINUTES
    }

    val quietHoursStart: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.QUIET_HOURS_START] ?: Defaults.QUIET_HOURS_START
    }

    val quietHoursEnd: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.QUIET_HOURS_END] ?: Defaults.QUIET_HOURS_END
    }

    val unlockRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.UNLOCK_REMINDERS_ENABLED] ?: Defaults.UNLOCK_REMINDERS_ENABLED
    }

    val appRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.APP_REMINDERS_ENABLED] ?: Defaults.APP_REMINDERS_ENABLED
    }

    val monitoredApps: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.MONITORED_APPS] ?: Defaults.MONITORED_APPS
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: Defaults.ONBOARDING_COMPLETED
    }

    suspend fun setDailyGoalMl(value: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_ML] = value }
    }

    suspend fun setCooldownMinutes(value: Int) {
        context.dataStore.edit { it[Keys.COOLDOWN_MINUTES] = value }
    }

    suspend fun setAppDurationMinutes(value: Int) {
        context.dataStore.edit { it[Keys.APP_DURATION_MINUTES] = value }
    }

    suspend fun setQuietHoursStart(value: String) {
        context.dataStore.edit { it[Keys.QUIET_HOURS_START] = value }
    }

    suspend fun setQuietHoursEnd(value: String) {
        context.dataStore.edit { it[Keys.QUIET_HOURS_END] = value }
    }

    suspend fun setUnlockRemindersEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.UNLOCK_REMINDERS_ENABLED] = value }
    }

    suspend fun setAppRemindersEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.APP_REMINDERS_ENABLED] = value }
    }

    suspend fun setMonitoredApps(value: Set<String>) {
        context.dataStore.edit { it[Keys.MONITORED_APPS] = value }
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = value }
    }
}
