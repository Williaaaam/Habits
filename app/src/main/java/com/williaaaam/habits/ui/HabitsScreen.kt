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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
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
        LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                Section {
                    Text(
                        "Do all of these every day to unlock your apps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.Secondary,
                    )
                }
            }
            if (habits.isEmpty()) {
                item {
                    Section {
                        Text("Nothing here yet", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Try Read 30m, Study 50m or Make bed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.Secondary,
                        )
                    }
                }
            }
            items(habits, key = { it.id }) { habit ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { editing = habit }
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HabitAvatar(habit.name, done = false)
                    Column(Modifier.weight(1f)) {
                        Text(habit.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            when (habit.type) {
                                HabitType.TIMER -> "${habit.goalMinutes} min a day"
                                HabitType.CHECK -> "Check off daily"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.Secondary,
                        )
                    }
                    IconButton(onClick = { deleting = habit }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete ${habit.name}", tint = Palette.Secondary)
                    }
                }
                Hairline()
            }
        }
        FloatingActionButton(
            onClick = { editing = Habit(name = "", type = HabitType.TIMER, goalMinutes = 30) },
            shape = CircleShape,
            containerColor = Palette.Blue,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Default.Add, contentDescription = "Add habit") }
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
            containerColor = Palette.Raised,
            title = { Text("Delete ${habit.name}?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Today's progress on it goes too.", color = Palette.Secondary) },
            confirmButton = {
                PillButton("Delete", onClick = {
                    vm.deleteHabit(habit)
                    deleting = null
                })
            },
            dismissButton = { OutlinedPill("Cancel", onClick = { deleting = null }) },
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

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = Palette.Blue.copy(alpha = 0.15f),
        selectedLabelColor = Palette.Blue,
        labelColor = Palette.Text,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Palette.Raised,
        title = { Text(if (initial.id == 0L) "New habit" else "Edit habit", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, placeholder = { Text("Reading") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == HabitType.TIMER,
                        onClick = { type = HabitType.TIMER },
                        label = { Text("Timed") },
                        shape = CircleShape,
                        colors = chipColors,
                    )
                    FilterChip(
                        selected = type == HabitType.CHECK,
                        onClick = { type = HabitType.CHECK },
                        label = { Text("Check off") },
                        shape = CircleShape,
                        colors = chipColors,
                    )
                }
                if (type == HabitType.TIMER) {
                    OutlinedTextField(
                        value = goal, onValueChange = { goal = it.filter(Char::isDigit) },
                        label = { Text("Minutes per day") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            PillButton(
                "Save",
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
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Palette.Text) } },
    )
}
