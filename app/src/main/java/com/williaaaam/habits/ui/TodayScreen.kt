package com.williaaaam.habits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.williaaaam.habits.data.HabitSession
import com.williaaaam.habits.data.HabitStatus
import com.williaaaam.habits.domain.Format
import com.williaaaam.habits.domain.HabitType
import com.williaaaam.habits.domain.Pomodoro

@Composable
fun TodayScreen(vm: MainViewModel, openSetup: () -> Unit, openHabits: () -> Unit) {
    val now by vm.now.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    val permissions by vm.permissions.collectAsStateWithLifecycle()
    val state = today ?: return

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!permissions.accessibility) {
            SectionCard(container = MaterialTheme.colorScheme.errorContainer) {
                Text("Blocking is OFF.", style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = openSetup) { Text("Turn it on →") }
            }
        }

        Column {
            Text(
                when {
                    state.habits.isEmpty() -> "NO HABITS"
                    state.complete -> "UNLOCKED"
                    else -> "LOCKED"
                },
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                when {
                    state.habits.isEmpty() -> "Add a habit. Until then nothing is locked."
                    state.complete -> "Done for today. Apps are open till midnight."
                    else -> "${state.doneCount}/${state.habits.size} done. Finish them all to unlock."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.habits.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { state.doneCount.toFloat() / state.habits.size },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            } else {
                TextButton(onClick = openHabits) { Text("Add a habit →") }
            }
        }

        state.active?.let { session -> ActiveTimerCard(session, now, onStop = { vm.stopTimer() }) }

        if (state.habits.isNotEmpty()) {
            SectionCard(title = "Today") {
                state.habits.forEachIndexed { index, status ->
                    if (index > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    HabitRow(
                        status = status,
                        timerRunning = state.active != null,
                        runningThis = state.active?.habitId == status.habit.id,
                        onStart = { pomodoro -> vm.startTimer(status.habit, pomodoro) },
                        onCheck = { vm.setChecked(status.habit, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTimerCard(session: HabitSession, now: Long, onStop: () -> Unit) {
    val elapsed = now - session.startedAt
    SectionCard(container = MaterialTheme.colorScheme.surfaceContainerHigh) {
        if (session.pomodoro) {
            val phase = Pomodoro.phase(elapsed)
            Text(
                "${session.habitName} · ${if (phase.focus) "focus" else "break"} #${phase.round}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(Format.clock(phase.msLeft), style = MaterialTheme.typography.displayLarge)
        } else {
            Text(session.habitName, style = MaterialTheme.typography.titleSmall)
            Text(Format.clock(elapsed), style = MaterialTheme.typography.displayLarge)
        }
        Button(onClick = onStop, modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) { Text("STOP") }
    }
}

@Composable
private fun HabitRow(
    status: HabitStatus,
    timerRunning: Boolean,
    runningThis: Boolean,
    onStart: (pomodoro: Boolean) -> Unit,
    onCheck: (Boolean) -> Unit,
) {
    val habit = status.habit
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text((if (status.done) "✓ " else "") + habit.name, style = MaterialTheme.typography.titleSmall)
            if (habit.type == HabitType.TIMER) {
                Text(
                    "${Format.minutes(status.seconds)} / ${habit.goalMinutes}m" + if (runningThis) " · running" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { status.fraction },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 12.dp),
                )
            }
        }
        when {
            habit.type == HabitType.CHECK -> Checkbox(checked = status.checked, onCheckedChange = onCheck)
            status.done -> Icon(Icons.Default.Check, contentDescription = "Done")
            !timerRunning -> StartButton(onStart)
        }
    }
}

@Composable
private fun StartButton(onStart: (pomodoro: Boolean) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { menu = true }) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Text("GO")
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text("Timer") }, onClick = { menu = false; onStart(false) })
            DropdownMenuItem(
                text = { Text("Pomodoro ${Pomodoro.FOCUS_MINUTES}/${Pomodoro.BREAK_MINUTES}") },
                onClick = { menu = false; onStart(true) },
            )
        }
    }
}
