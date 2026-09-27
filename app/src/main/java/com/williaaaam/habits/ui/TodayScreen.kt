package com.williaaaam.habits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.williaaaam.habits.domain.Format
import com.williaaaam.habits.domain.Rules

@Composable
fun TodayScreen(vm: MainViewModel, openSetup: () -> Unit, openHabits: () -> Unit, openApps: () -> Unit) {
    val now by vm.now.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val active by vm.activeSession.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val blocked by vm.blockedApps.collectAsStateWithLifecycle()
    val usage by vm.usage.collectAsStateWithLifecycle()
    val finished by vm.finishedToday.collectAsStateWithLifecycle()
    val permissions by vm.permissions.collectAsStateWithLifecycle()

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

        SectionCard(container = MaterialTheme.colorScheme.primaryContainer) {
            Text("App time left today", style = MaterialTheme.typography.labelLarge)
            Text(
                Format.minutes(ledger?.balanceSeconds ?: 0),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Earned ${Format.minutes(ledger?.earnedSeconds ?: 0)} · used ${Format.minutes(ledger?.spentSeconds ?: 0)} · resets at midnight",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        val session = active
        if (session != null) {
            val elapsed = now - session.startedAt
            SectionCard(title = "${session.habitName} in progress") {
                Text(Format.clock(elapsed), style = MaterialTheme.typography.displaySmall)
                Text(
                    "Earning ${Format.minutes(Rules.creditSeconds(elapsed, session.habitMinutes, session.rewardMinutes))} " +
                        "so far (${session.habitMinutes} min → ${session.rewardMinutes} min). Blocked apps are locked until you stop.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = { vm.stopHabit() }, modifier = Modifier.padding(top = 12.dp).fillMaxWidth()) {
                    Text("Stop & collect")
                }
            }
        } else {
            SectionCard(title = "Earn app time") {
                if (habits.isEmpty()) {
                    Text("You don't have any habits yet.")
                    TextButton(onClick = openHabits) { Text("Add a habit") }
                }
                habits.forEach { habit ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(habit.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${habit.habitMinutes} min → ${habit.rewardMinutes} min of apps",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        FilledTonalButton(onClick = { vm.startHabit(habit) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text("Start")
                        }
                    }
                }
            }
        }

        if (finished.isNotEmpty()) {
            SectionCard(title = "Done today") {
                finished.forEach { s ->
                    val minutes = ((s.endedAt ?: s.startedAt) - s.startedAt) / 60_000
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text("${s.habitName} · $minutes min", Modifier.weight(1f))
                        Text("+${Format.minutes(s.earnedSeconds)}")
                    }
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
