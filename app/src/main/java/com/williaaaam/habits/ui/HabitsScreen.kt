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
import androidx.compose.material3.ExtendedFloatingActionButton
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

@Composable
fun HabitsScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Habit?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Each habit has an exchange rate: do it for X minutes, earn Y minutes of your blocked apps. " +
                        "Partial time counts proportionally.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (habits.isEmpty()) {
                item { Text("No habits yet — tap “Add habit”.", style = MaterialTheme.typography.bodyLarge) }
            }
            items(habits, key = { it.id }) { habit ->
                SectionCard(modifier = Modifier.clickable { editing = habit }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(habit.name, style = MaterialTheme.typography.titleMedium)
                            Text("${habit.habitMinutes} min → ${habit.rewardMinutes} min of apps")
                        }
                        IconButton(onClick = { vm.deleteHabit(habit) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete ${habit.name}")
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { editing = Habit(name = "", habitMinutes = 30, rewardMinutes = 15) },
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
}

@Composable
private fun HabitDialog(initial: Habit, onDismiss: () -> Unit, onSave: (Habit) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var habitMinutes by remember { mutableStateOf(initial.habitMinutes.toString()) }
    var rewardMinutes by remember { mutableStateOf(initial.rewardMinutes.toString()) }
    val h = habitMinutes.toIntOrNull()
    val r = rewardMinutes.toIntOrNull()
    val valid = name.isNotBlank() && h != null && h in 1..1440 && r != null && r in 1..1440

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "New habit" else "Edit habit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Habit (e.g. Reading)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = habitMinutes, onValueChange = { habitMinutes = it.filter(Char::isDigit) },
                        label = { Text("Do (min)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    Text("→")
                    OutlinedTextField(
                        value = rewardMinutes, onValueChange = { rewardMinutes = it.filter(Char::isDigit) },
                        label = { Text("Earn (min)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(initial.copy(name = name.trim(), habitMinutes = h!!, rewardMinutes = r!!)) },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
