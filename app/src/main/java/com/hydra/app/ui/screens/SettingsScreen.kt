package com.hydra.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hydra.app.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

private val cooldownOptions = listOf(1, 30, 60, 90, 120)
private val cooldownLabels = listOf("1 min", "30 min", "1 hour", "1.5 hr", "2 hr")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.settingsState.collectAsState()
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
        )

        // ── Daily Goal ──────────────────────────────────────────────
        SettingsCard(
            title = "Daily Goal",
            icon = Icons.Filled.WaterDrop
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.dailyGoalMl} ml",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Recommended: 2,000 ml",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Slider(
                value = state.dailyGoalMl.toFloat(),
                onValueChange = { viewModel.setDailyGoal(it.roundToInt()) },
                valueRange = 500f..5000f,
                steps = 17, // 250ml steps between 500–5000 = 18 options → 17 steps
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("500 ml", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("5,000 ml", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }

        // ── Reminder Frequency ──────────────────────────────────────
        SettingsCard(
            title = "Reminder Frequency",
            icon = Icons.Filled.Notifications
        ) {
            Text(
                text = "How often to remind you after drinking water",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                cooldownOptions.forEachIndexed { index, minutes ->
                    FilterChip(
                        selected = state.cooldownMinutes == minutes,
                        onClick = { viewModel.setCooldown(minutes) },
                        label = { Text(cooldownLabels[index]) }
                    )
                }
            }
        }

        // ── Reminders Toggle ────────────────────────────────────────
        SettingsCard(
            title = "Reminders",
            icon = Icons.Filled.Notifications
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable reminders",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Smart reminders when you unlock your phone",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(16.dp))
                Switch(
                    checked = state.remindersEnabled,
                    onCheckedChange = { viewModel.setRemindersEnabled(it) }
                )
            }
        }

        // ── Quiet Hours ─────────────────────────────────────────────
        SettingsCard(
            title = "Quiet Hours",
            icon = Icons.Filled.AccessTime
        ) {
            Text(
                text = "No reminders during these hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartTimePicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = state.quietHoursStart,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // End time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Until",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEndTimePicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = state.quietHoursEnd,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // ── Developer Options ───────────────────────────────────────
        val fsmState by viewModel.fsmState.collectAsState()
        val meta by viewModel.fsmMetadata.collectAsState()
        var now by remember { mutableStateOf(System.currentTimeMillis()) }

        androidx.compose.runtime.LaunchedEffect(Unit) {
            while(true) {
                kotlinx.coroutines.delay(1000)
                now = System.currentTimeMillis()
            }
        }

        SettingsCard(
            title = "Developer Options",
            icon = androidx.compose.material.icons.Icons.Filled.Build
        ) {
            Text(
                text = "Engine State: ${fsmState.name}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Goal Reached: ${meta.goalReached}", style = MaterialTheme.typography.bodySmall)
            Text("Consumed: ${meta.dailyWaterConsumed} / ${meta.dailyGoal}", style = MaterialTheme.typography.bodySmall)
            Text("Snooze Count: ${meta.snoozeCount}", style = MaterialTheme.typography.bodySmall)
            
            Spacer(Modifier.height(8.dp))
            when (fsmState) {
                com.hydra.app.model.ReminderState.COOLDOWN -> {
                    val diff = (meta.cooldownEndsAt - now).coerceAtLeast(0)
                    Text("Cooldown ends in: ${diff / 1000} seconds", style = MaterialTheme.typography.bodySmall)
                }
                com.hydra.app.model.ReminderState.SNOOZED -> {
                    val diff = (meta.snoozeEndsAt - now).coerceAtLeast(0)
                    Text("Snooze ends in: ${diff / 1000} seconds", style = MaterialTheme.typography.bodySmall)
                }
                else -> {
                    Text("Waiting for event...", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    // Time picker dialogs
    if (showStartTimePicker) {
        val parts = state.quietHoursStart.split(":")
        val pickerState = rememberTimePickerState(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 22,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Quiet hours start") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.setQuietHoursStart(
                        "%02d:%02d".format(pickerState.hour, pickerState.minute)
                    )
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = pickerState)
        }
    }

    if (showEndTimePicker) {
        val parts = state.quietHoursEnd.split(":")
        val pickerState = rememberTimePickerState(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 7,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Quiet hours end") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.setQuietHoursEnd(
                        "%02d:%02d".format(pickerState.hour, pickerState.minute)
                    )
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = pickerState)
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}
