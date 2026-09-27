package com.williaaaam.habits.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.williaaaam.habits.data.Habit
import com.williaaaam.habits.domain.HabitType

@Composable
fun HabitsScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Habit?>(null) }
    var deleting by remember { mutableStateOf<Habit?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Your blocked apps unlock each day once every habit here is done.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (habits.isEmpty()) {
                item {
                    Text(
                        "No habits yet. Ideas: Read 30 min · Meditate 10 min · Study (Pomodoro) 50 min · Make bed · Drink water",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            items(habits, key = { it.id }) { habit ->
                SectionCard(modifier = Modifier.clickable { editing = habit }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(habit.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                when (habit.type) {
                                    HabitType.TIMER -> "${habit.goalMinutes} min a day (timer)"
                                    HabitType.CHECK -> "Check off once a day"
                                },
                            )
                        }
                        IconButton(onClick = { deleting = habit }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete ${habit.name}")
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { editing = Habit(name = "", type = HabitType.TIMER, goalMinutes = 30) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add habit") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    editing?.let { habit ->
        HabitDialog(
            initial = habit,
            onDismiss = { editing = null },
            onSave = {
                vm.saveHabit(it)
                editing = null
            },
        )
    }

    deleting?.let { habit ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete ${habit.name}?") },
            text = { Text("Its history and streak will be deleted too.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteHabit(habit)
                    deleting = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDialog(initial: Habit, onDismiss: () -> Unit, onSave: (Habit) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var type by remember { mutableStateOf(initial.type) }
    var goal by remember { mutableStateOf(initial.goalMinutes.coerceAtLeast(1).toString()) }
    val goalMinutes = goal.toIntOrNull()
    val valid = name.isNotBlank() && (type == HabitType.CHECK || (goalMinutes != null && goalMinutes in 1..1440))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "New habit" else "Edit habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Habit (e.g. Reading)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == HabitType.TIMER,
                        onClick = { type = HabitType.TIMER },
                        label = { Text("Timed") },
                    )
                    FilterChip(
                        selected = type == HabitType.CHECK,
                        onClick = { type = HabitType.CHECK },
                        label = { Text("Check off") },
                    )
                }
                if (type == HabitType.TIMER) {
                    OutlinedTextField(
                        value = goal, onValueChange = { goal = it.filter(Char::isDigit) },
                        label = { Text("Minutes per day") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Time adds up across timer sessions. Use a normal or Pomodoro timer.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text("Done with one tap on the Today screen.", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            type = type,
                            goalMinutes = if (type == HabitType.TIMER) goalMinutes!! else 0,
                        ),
                    )
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
