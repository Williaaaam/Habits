package com.williaaaam.habits.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.williaaaam.habits.data.complete
import com.williaaaam.habits.domain.Rules
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val WEEKS = 17

@Composable
fun ProgressScreen(vm: MainViewModel) {
    val summaries by vm.summaries.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val today = vm.todayDate()

    val overall = remember(summaries) {
        summaries.filter { it.total > 0 }.associate { LocalDate.parse(it.date) to it.done.toFloat() / it.total }
    }
    val completeDays = remember(summaries) {
        summaries.filter { it.complete }.mapTo(HashSet()) { LocalDate.parse(it.date) }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = "All habits") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("🔥 ${Rules.currentStreak(completeDays, today)}", "current streak")
                Stat("🏆 ${Rules.bestStreak(completeDays)}", "best streak")
                Stat("✅ ${completeDays.size}", "days unlocked")
            }
            Heatmap(overall, today, Modifier.padding(top = 16.dp))
            Text(
                "Each square is a day; darker = more habits done.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        habits.forEach { habit ->
            val doneDays = remember(history, habit) {
                history
                    .filter { it.habitId == habit.id && Rules.isHabitDone(habit.type, it.seconds, it.checked, habit.goalMinutes) }
                    .mapTo(HashSet()) { LocalDate.parse(it.date) }
            }
            SectionCard(title = habit.name) {
                Text(
                    "🔥 ${Rules.currentStreak(doneDays, today)} day streak · best ${Rules.bestStreak(doneDays)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Heatmap(doneDays.associateWith { 1f }, today, Modifier.padding(top = 12.dp))
            }
        }

        if (habits.isEmpty()) {
            Text("Add habits to start tracking your streaks.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

/** GitHub-style grid: one column per week (Mon–Sun), the last column is this week. */
@Composable
fun Heatmap(values: Map<LocalDate, Float>, today: LocalDate, modifier: Modifier = Modifier) {
    val filled = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceVariant
    val firstMonday = today.with(DayOfWeek.MONDAY).minusWeeks((WEEKS - 1).toLong())
    Canvas(modifier.fillMaxWidth().aspectRatio(WEEKS / 7f)) {
        val cell = size.width / WEEKS
        val gap = cell * 0.18f
        val radius = CornerRadius(cell * 0.2f)
        val days = ChronoUnit.DAYS.between(firstMonday, today).toInt()
        for (i in 0..days) {
            val date = firstMonday.plusDays(i.toLong())
            val v = values[date] ?: 0f
            val color = if (v <= 0f) empty else filled.copy(alpha = 0.3f + 0.7f * v.coerceAtMost(1f))
            drawRoundRect(
                color = color,
                topLeft = Offset((i / 7) * cell + gap / 2, (i % 7) * cell + gap / 2),
                size = Size(cell - gap, cell - gap),
                cornerRadius = radius,
            )
        }
    }
}
