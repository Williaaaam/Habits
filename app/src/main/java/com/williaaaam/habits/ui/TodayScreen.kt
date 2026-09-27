package com.williaaaam.habits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (!permissions.accessibility) {
            Section {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Blocking is off", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Turn on the accessibility service so apps can be locked.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.Secondary,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedPill("Turn on", onClick = openSetup, small = true)
                }
            }
        }

        Section {
            Text(
                when {
                    state.habits.isEmpty() -> "No habits yet"
                    state.complete -> "Unlocked"
                    else -> "Locked"
                },
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                when {
                    state.habits.isEmpty() -> "Add the habits you want to do every day."
                    state.complete -> "You did everything. Apps are open until midnight."
                    else -> "${state.doneCount} of ${state.habits.size} done · finish them all to unlock"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.Secondary,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (state.habits.isNotEmpty()) {
                ThinProgress(state.doneCount.toFloat() / state.habits.size, Modifier.padding(top = 14.dp, bottom = 4.dp))
            } else {
                PillButton("Add a habit", onClick = openHabits, modifier = Modifier.padding(top = 14.dp))
            }
        }

        state.active?.let { session -> ActiveTimer(session, now, onStop = { vm.stopTimer() }) }

        state.habits.forEach { status ->
            HabitRow(
                status = status,
                timerRunning = state.active != null,
                runningThis = state.active?.habitId == status.habit.id,
                onStart = { pomodoro -> vm.startTimer(status.habit, pomodoro) },
                onCheck = { vm.setChecked(status.habit, it) },
            )
            Hairline()
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ActiveTimer(session: HabitSession, now: Long, onStop: () -> Unit) {
    val elapsed = now - session.startedAt
    Section {
        val label: String
        val clock: String
        if (session.pomodoro) {
            val phase = Pomodoro.phase(elapsed)
            label = "${session.habitName} · ${if (phase.focus) "Focus" else "Break"} · round ${phase.round}"
            clock = Format.clock(phase.msLeft)
        } else {
            label = session.habitName
            clock = Format.clock(elapsed)
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Palette.Secondary)
        Text(clock, style = MaterialTheme.typography.displayLarge)
        PillButton("Stop", onClick = onStop, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
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
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HabitAvatar(habit.name, status.done)
        Column(Modifier.weight(1f)) {
            Text(habit.name, style = MaterialTheme.typography.titleSmall)
            Text(
                when {
                    habit.type == HabitType.CHECK -> if (status.checked) "Done" else "Check off"
                    else -> "${Format.minutes(status.seconds)} of ${habit.goalMinutes}m" + if (runningThis) " · running" else ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = Palette.Secondary,
            )
            if (habit.type == HabitType.TIMER && !status.done) {
                ThinProgress(status.fraction, Modifier.padding(top = 8.dp))
            }
        }
        when {
            habit.type == HabitType.CHECK -> RoundCheck(status.checked, onCheck)
            status.done -> RoundCheck(true, null)
            !timerRunning -> StartButton(onStart)
        }
    }
}

@Composable
private fun StartButton(onStart: (pomodoro: Boolean) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Box {
        OutlinedPill("Start", onClick = { menu = true }, small = true)
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, containerColor = Palette.Raised) {
            DropdownMenuItem(text = { Text("Timer") }, onClick = { menu = false; onStart(false) })
            DropdownMenuItem(
                text = { Text("Pomodoro · ${Pomodoro.FOCUS_MINUTES}/${Pomodoro.BREAK_MINUTES}") },
                onClick = { menu = false; onStart(true) },
            )
        }
    }
}
