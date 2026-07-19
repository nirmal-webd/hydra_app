package com.hydra.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hydra.app.HydraApp
import com.hydra.app.model.WaterLog
import com.hydra.app.model.WaterLogSource
import com.hydra.app.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class DailyStatus {
    MET, NOT_MET, NO_DATA
}

data class WeeklyDayStatus(
    val label: String,
    val status: DailyStatus
)

data class DashboardState(
    val todayTotal: Int = 0,
    val dailyGoal: Int = 2000,
    val todayLogs: List<WaterLog> = emptyList(),
    val streak: Int = 0,
    val isGoalReached: Boolean = false,
    val weeklyStatus: List<WeeklyDayStatus> = emptyList()
) {
    val progress: Float
        get() = if (dailyGoal > 0) (todayTotal.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f

    val percentage: Int
        get() = if (dailyGoal > 0) ((todayTotal.toFloat() / dailyGoal) * 100).toInt().coerceAtMost(100) else 0

    val remaining: Int
        get() = (dailyGoal - todayTotal).coerceAtLeast(0)
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HydraApp
    private val waterRepository = app.waterRepository
    private val reminderRepository = app.reminderRepository
    private val preferencesManager = app.preferencesManager

    private val _streak = MutableStateFlow(0)
    private val _weeklyStatus = MutableStateFlow<List<WeeklyDayStatus>>(emptyList())

    private val waterState = combine(
        waterRepository.getTodayTotal(),
        waterRepository.getTodayLogs(),
        preferencesManager.dailyGoalMl
    ) { total, logs, goal ->
        Triple(total, logs, goal)
    }

    // Removed reminderState since we no longer display remindersShown or remindersAccepted

    val dashboardState: StateFlow<DashboardState> = combine(
        waterState,
        _streak.asStateFlow(),
        _weeklyStatus.asStateFlow()
    ) { water, streak, weekly ->
        DashboardState(
            todayTotal = water.first,
            todayLogs = water.second,
            dailyGoal = water.third,
            streak = streak,
            isGoalReached = water.first >= water.third,
            weeklyStatus = weekly
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardState()
    )

    init {
        viewModelScope.launch {
            // Recompute streak whenever today's total or goal changes
            combine(
                waterRepository.getTodayTotal(),
                preferencesManager.dailyGoalMl
            ) { _, goal -> goal }
                .collect { goal -> computeHistoricalStats(goal) }
        }
        
        // Ensure Reminder Engine stays in perfect sync with the database,
        // particularly to recover from stale states or UI deletions.
        viewModelScope.launch {
            waterRepository.getTodayTotal().collect { total ->
                app.reminderStateStore.syncDailyWater(total)
            }
        }
    }

    fun logWater(amountMl: Int, source: String = WaterLogSource.MANUAL) {
        // Route through the FSM — WaterLogged is a global transition that
        // resets cooldown, cancels pending reminders, and logs to Room via effect.
        app.reminderStateManager.dispatch(
            com.hydra.app.model.ReminderEvent.WaterLogged(amountMl, source)
        )
    }

    fun deleteLog(waterLog: WaterLog) {
        viewModelScope.launch {
            waterRepository.deleteLog(waterLog)
            app.reminderStateManager.dispatch(
                com.hydra.app.model.ReminderEvent.WaterLogDeleted(waterLog.amountMl)
            )
        }
    }

    private suspend fun computeHistoricalStats(goalMl: Int) {
        try {
            val allLogs = waterRepository.getAllLogsSnapshot()

            // Group logs by date and sum amounts
            val dailyTotals: Map<LocalDate, Int> = allLogs.groupBy { log ->
                DateUtils.toLocalDate(log.timestamp)
            }.mapValues { (_, logs) ->
                logs.sumOf { it.amountMl }
            }

            val today = LocalDate.now()
            
            // 1. Compute Weekly Status (Last 7 days, ending today)
            val weekly = mutableListOf<WeeklyDayStatus>()
            for (i in 6 downTo 0) {
                val date = today.minusDays(i.toLong())
                val dayTotal = dailyTotals[date] ?: 0
                
                val status = if (dayTotal >= goalMl) {
                    DailyStatus.MET
                } else if (dayTotal > 0) {
                    DailyStatus.NOT_MET
                } else {
                    DailyStatus.NO_DATA
                }
                
                // M, T, W, T, F, S, S
                val label = date.dayOfWeek.name.take(1)
                weekly.add(WeeklyDayStatus(label, status))
            }
            _weeklyStatus.value = weekly

            // 2. Compute Streak
            var streak = 0

            // Check if today's goal is met — include today in streak
            val todayTotal = dailyTotals[today] ?: 0
            if (todayTotal >= goalMl) {
                streak = 1
            }

            // Count consecutive completed days going backwards from yesterday
            var checkDate = today.minusDays(1)
            while (true) {
                val dayTotal = dailyTotals[checkDate] ?: 0
                if (dayTotal >= goalMl) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }

            _streak.value = streak
        } catch (_: Exception) {
            _streak.value = 0
            _weeklyStatus.value = emptyList()
        }
    }
}
