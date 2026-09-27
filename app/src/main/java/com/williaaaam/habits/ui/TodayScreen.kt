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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.williaaaam.habits.data.HabitSession
import com.williaaaam.habits.data.HabitStatus
import com.williaaaam.habits.data.complete
import com.williaaaam.habits.domain.Format
import com.williaaaam.habits.domain.HabitType
import com.williaaaam.habits.domain.Pomodoro
import com.williaaaam.habits.domain.Rules

@Composable
fun TodayScreen(vm: MainViewModel, openSetup: () -> Unit, openHabits: () -> Unit, openApps: () -> Unit) {
    val now by vm.now.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    val blocked by vm.blockedApps.collectAsStateWithLifecycle()
    val usage by vm.usage.collectAsStateWithLifecycle()
    val summaries by vm.summaries.collectAsStateWithLifecycle()
    val permissions by vm.permissions.collectAsStateWithLifecycle()
    val state = today ?: return

    val streak = remember(summaries) {
        Rules.currentStreak(summaries.filter { it.complete }.mapTo(HashSet()) { java.time.LocalDate.parse(it.date) }, vm.todayDate())
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!permissions.accessibility) {
            SectionCard(container = MaterialTheme.colorScheme.errorContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Text(
                        "Blocking is off. Turn on the Habits accessibility service.",
                        Modifier.weight(1f).padding(horizontal = 12.dp),
                    )
                    TextButton(onClick = openSetup) { Text("Fix") }
                }
            }
        }

        // Status: locked or unlocked for the day.
        val unlocked = state.complete
        SectionCard(
            container = if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                when {
                    state.habits.isEmpty() -> "No habits yet"
                    unlocked -> "🔓 Apps unlocked"
                    else -> "🔒 Apps locked"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                when {
                    state.habits.isEmpty() -> "Add the habits you want to do every day. Until then your blocked apps stay open."
                    unlocked -> "All habits done — enjoy your apps until midnight."
                    else -> "${state.doneCount} of ${state.habits.size} habits done. Finish them all to unlock ${blocked.size} app${if (blocked.size == 1) "" else "s"}."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state.habits.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { state.doneCount.toFloat() / state.habits.size },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
            if (streak > 0) {
                Text("🔥 $streak-day streak", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            }
            if (state.habits.isEmpty()) {
                TextButton(onClick = openHabits) { Text("Add a habit") }
            }
        }

        state.active?.let { session -> ActiveTimerCard(session, now, onStop = { vm.stopTimer() }) }

        if (state.habits.isNotEmpty()) {
            SectionCard(title = "Today's habits") {
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

        SectionCard(title = "Screen time today") {
            when {
                blocked.isEmpty() -> {
                    Text("No blocked apps yet.")
                    TextButton(onClick = openApps) { Text("Choose apps to block") }
                }
                !permissions.usageAccess -> {
                    Text("Allow usage access to see how long you've spent in each app.")
                    TextButton(onClick = openSetup) { Text("Set up") }
                }
                else -> {
                    val total = blocked.sumOf { usage[it.packageName] ?: 0L }
                    blocked.forEach { app ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text(app.label, Modifier.weight(1f))
                            Text(Format.minutes((usage[app.packageName] ?: 0L) / 1000))
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("Total", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(Format.minutes(total / 1000), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveTimerCard(session: HabitSession, now: Long, onStop: () -> Unit) {
    val elapsed = now - session.startedAt
    SectionCard(container = MaterialTheme.colorScheme.secondaryContainer) {
        if (session.pomodoro) {
            val phase = Pomodoro.phase(elapsed)
            Text(
                "${session.habitName} · ${if (phase.focus) "Focus" else "Break"} · round ${phase.round}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(Format.clock(phase.msLeft), style = MaterialTheme.typography.displayMedium)
            Text(
                "${Format.minutes(Pomodoro.countedMs(elapsed, true) / 1000)} of focus so far. Breaks don't count.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text("${session.habitName} in progress", style = MaterialTheme.typography.titleMedium)
            Text(Format.clock(elapsed), style = MaterialTheme.typography.displayMedium)
        }
        Button(onClick = onStop, modifier = Modifier.padding(top = 12.dp).fillMaxWidth()) { Text("Stop & log time") }
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
            Text(
                (if (status.done) "✓ " else "") + habit.name,
                style = MaterialTheme.typography.titleSmall,
            )
            if (habit.type == HabitType.TIMER) {
                Text(
                    "${Format.minutes(status.seconds)} of ${habit.goalMinutes} min" + if (runningThis) " · running" else "",
                    style = MaterialTheme.typography.bodySmall,
                )
                LinearProgressIndicator(
                    progress = { status.fraction },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 12.dp),
                )
            }
        }
        when {
            habit.type == HabitType.CHECK -> Checkbox(checked = status.checked, onCheckedChange = onCheck)
            status.done -> Icon(Icons.Default.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary)
            !timerRunning -> StartButton(onStart)
        }
    }
}

@Composable
private fun StartButton(onStart: (pomodoro: Boolean) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(onClick = { menu = true }) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Text("Start")
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text("Timer") }, onClick = { menu = false; onStart(false) })
            DropdownMenuItem(
                text = { Text("Pomodoro (${Pomodoro.FOCUS_MINUTES}/${Pomodoro.BREAK_MINUTES})") },
                onClick = { menu = false; onStart(true) },
            )
        }
    }
}
