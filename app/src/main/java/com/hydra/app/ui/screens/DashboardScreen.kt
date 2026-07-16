package com.hydra.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hydra.app.ui.components.CircularProgressRing
import com.hydra.app.viewmodel.DashboardViewModel

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

        Spacer(modifier = Modifier.weight(1f))

        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Streak", value = "${state.streak} days")
                StatItem(label = "Remaining", value = "${state.remaining} ml")
                StatItem(label = "Reminders", value = "${state.remindersShown}")
            }
        }
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
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
