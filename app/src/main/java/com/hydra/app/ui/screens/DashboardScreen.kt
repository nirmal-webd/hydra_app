package com.hydra.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hydra.app.ui.components.CircularProgressRing
import com.hydra.app.viewmodel.DashboardViewModel
import com.hydra.app.viewmodel.DailyStatus
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(),
    initialShowCustomDialog: Boolean = false,
    onCustomDialogDismiss: () -> Unit = {}
) {
    val state by viewModel.dashboardState.collectAsState()
    var showCustomDialog by remember(initialShowCustomDialog) { mutableStateOf(initialShowCustomDialog) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Hydration",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
        )

        // Progress Ring
        CircularProgressRing(
            currentMl = state.todayTotal,
            goalMl = state.dailyGoal
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        // Quick Add Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.logWater(250) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Filled.LocalDrink, contentDescription = "Drink")
                Spacer(modifier = Modifier.width(8.dp))
                Text("+ 250 ml")
            }

            OutlinedButton(
                onClick = { showCustomDialog = true },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Custom")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Custom")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Weekly Tracker
        Text(
            text = "This Week",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.weeklyStatus.forEach { dayStatus ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayStatus.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (dayStatus.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (dayStatus.isToday) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(
                                width = if (dayStatus.isToday) 2.dp else 0.dp,
                                color = if (dayStatus.isToday) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(if (dayStatus.isToday) 2.dp else 0.dp) // Space between border and colored background
                            .background(
                                color = when (dayStatus.status) {
                                    DailyStatus.MET -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                    DailyStatus.NOT_MET -> androidx.compose.ui.graphics.Color(0xFFE53935)
                                    DailyStatus.NO_DATA -> MaterialTheme.colorScheme.surfaceVariant
                                    DailyStatus.IN_PROGRESS -> androidx.compose.ui.graphics.Color(0xFFFFB74D) // Orange
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (dayStatus.status) {
                                DailyStatus.MET -> Icons.Filled.Check
                                DailyStatus.NOT_MET -> Icons.Filled.Close
                                DailyStatus.NO_DATA -> Icons.Filled.Remove
                                DailyStatus.IN_PROGRESS -> Icons.Filled.Remove
                            },
                            contentDescription = null,
                            tint = if (dayStatus.status == DailyStatus.NO_DATA) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Streak Info
        if (state.streak > 0) {
            Text(
                text = "🔥 ${state.streak} Day Streak!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Every drop counts! Start your hydration streak today. 💧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onDismiss = {
                showCustomDialog = false
                onCustomDialogDismiss()
            },
            onConfirm = { amount ->
                viewModel.logWater(amount)
                showCustomDialog = false
                onCustomDialogDismiss()
            }
        )
    }
}



@Composable
fun CustomAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Water") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Amount (ml)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = text.toIntOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
