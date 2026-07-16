package com.hydra.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hydra.app.HydraApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val dailyGoalMl: Int = 2000,
    val cooldownMinutes: Int = 60,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val remindersEnabled: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HydraApp
    private val prefs = app.preferencesManager

    val settingsState: StateFlow<SettingsState> = combine(
        prefs.dailyGoalMl,
        prefs.cooldownMinutes,
        prefs.quietHoursStart,
        prefs.quietHoursEnd,
        prefs.unlockRemindersEnabled
    ) { goal, cooldown, qStart, qEnd, reminders ->
        SettingsState(
            dailyGoalMl = goal,
            cooldownMinutes = cooldown,
            quietHoursStart = qStart,
            quietHoursEnd = qEnd,
            remindersEnabled = reminders
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsState()
    )

    fun setDailyGoal(ml: Int) {
        viewModelScope.launch {
            prefs.setDailyGoalMl(ml)
            // Also update FSM metadata so reminder suppression sees new goal immediately
            app.reminderStateStore.updateDailyGoal(ml)
        }
    }

    fun setCooldown(minutes: Int) {
        viewModelScope.launch { prefs.setCooldownMinutes(minutes) }
    }

    fun setQuietHoursStart(time: String) {
        viewModelScope.launch { prefs.setQuietHoursStart(time) }
    }

    fun setQuietHoursEnd(time: String) {
        viewModelScope.launch { prefs.setQuietHoursEnd(time) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setUnlockRemindersEnabled(enabled) }
    }
}
