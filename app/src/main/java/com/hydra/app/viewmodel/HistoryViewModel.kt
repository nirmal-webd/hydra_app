package com.hydra.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hydra.app.HydraApp
import com.hydra.app.model.WaterLog
import com.hydra.app.utils.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DayGroup(
    val date: LocalDate,
    val label: String,
    val totalMl: Int,
    val goalMl: Int,
    val logs: List<WaterLog>
) {
    val goalReached: Boolean get() = totalMl >= goalMl
    val progressFraction: Float get() = if (goalMl > 0) (totalMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HydraApp
    private val waterRepository = app.waterRepository
    private val preferencesManager = app.preferencesManager

    private val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

    val dayGroups: StateFlow<List<DayGroup>> = combine(
        waterRepository.getAllLogs(),
        preferencesManager.dailyGoalMl
    ) { logs, goal ->
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        logs
            .groupBy { DateUtils.toLocalDate(it.timestamp) }
            .entries
            .sortedByDescending { it.key }
            .map { (date, dayLogs) ->
                val label = when (date) {
                    today -> "Today"
                    yesterday -> "Yesterday"
                    else -> date.format(dayFormatter)
                }
                DayGroup(
                    date = date,
                    label = label,
                    totalMl = dayLogs.sumOf { it.amountMl },
                    goalMl = goal,
                    logs = dayLogs.sortedByDescending { it.timestamp }
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun deleteLog(waterLog: WaterLog) {
        viewModelScope.launch {
            waterRepository.deleteLog(waterLog)
        }
    }
}
