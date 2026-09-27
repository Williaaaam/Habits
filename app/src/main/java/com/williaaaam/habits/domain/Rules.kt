package com.williaaaam.habits.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class HabitType {
    /** Done once the day's timed total reaches the goal (e.g. read 30 min). */
    TIMER,

    /** Done when checked off (e.g. make the bed). */
    CHECK,
}

sealed interface Decision {
    /** The app isn't on the blocked list; nothing to do. */
    data object NotGated : Decision

    /** A blocked app, but today's habits are done. */
    data object Allow : Decision

    /** A blocked app and today's habits aren't done yet. */
    data object Block : Decision
}

object Rules {

    fun isHabitDone(type: HabitType, seconds: Long, checked: Boolean, goalMinutes: Int): Boolean = when (type) {
        HabitType.CHECK -> checked
        HabitType.TIMER -> checked || seconds >= goalMinutes * 60L
    }

    /** With no habits set up there is nothing to do, so the day counts as complete. */
    fun isDayComplete(habitsDone: List<Boolean>): Boolean = habitsDone.all { it }

    fun decide(packageName: String, blockedPackages: Set<String>, dayComplete: Boolean): Decision = when {
        packageName !in blockedPackages -> Decision.NotGated
        dayComplete -> Decision.Allow
        else -> Decision.Block
    }

    /** Key for per-day records; a new key at local midnight starts a fresh day. */
    fun dayKey(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        localDate(epochMillis, zone).toString()

    fun localDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    fun startOfDay(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        localDate(epochMillis, zone).atStartOfDay(zone).toInstant().toEpochMilli()

    /** Days in a row ending today (or yesterday, if today isn't done yet). */
    fun currentStreak(doneDays: Set<LocalDate>, today: LocalDate): Int {
        var day = if (today in doneDays) today else today.minusDays(1)
        var streak = 0
        while (day in doneDays) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun bestStreak(doneDays: Set<LocalDate>): Int {
        var best = 0
        for (day in doneDays) {
            if (day.minusDays(1) in doneDays) continue // not the start of a run
            var length = 0
            var d = day
            while (d in doneDays) {
                length++
                d = d.plusDays(1)
            }
            best = maxOf(best, length)
        }
        return best
    }
}

/** Classic Pomodoro: 25 min focus, 5 min break, repeat. Only focus time counts toward a habit. */
object Pomodoro {
    const val FOCUS_MINUTES = 25
    const val BREAK_MINUTES = 5
    private const val FOCUS_MS = FOCUS_MINUTES * 60_000L
    private const val CYCLE_MS = (FOCUS_MINUTES + BREAK_MINUTES) * 60_000L

    data class Phase(val focus: Boolean, val round: Int, val msLeft: Long, val endsAtElapsed: Long)

    /** Time that counts toward the habit after [elapsedMs] on the clock. */
    fun countedMs(elapsedMs: Long, pomodoro: Boolean): Long {
        if (elapsedMs <= 0) return 0
        if (!pomodoro) return elapsedMs
        val cycles = elapsedMs / CYCLE_MS
        val rest = elapsedMs % CYCLE_MS
        return cycles * FOCUS_MS + minOf(rest, FOCUS_MS)
    }

    fun phase(elapsedMs: Long): Phase {
        val e = elapsedMs.coerceAtLeast(0)
        val cycle = e / CYCLE_MS
        val rest = e % CYCLE_MS
        val focus = rest < FOCUS_MS
        val end = cycle * CYCLE_MS + if (focus) FOCUS_MS else CYCLE_MS
        return Phase(focus = focus, round = (cycle + 1).toInt(), msLeft = end - e, endsAtElapsed = end)
    }
}

object Format {
    /** "0m", "45m", "1h 05m" */
    fun minutes(seconds: Long): String {
        val totalMinutes = (seconds.coerceAtLeast(0)) / 60
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return if (h > 0) "${h}h ${m.toString().padStart(2, '0')}m" else "${m}m"
    }

    /** "4:07", "1:02:03" */
    fun clock(millis: Long): String {
        val total = millis.coerceAtLeast(0) / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        val ss = s.toString().padStart(2, '0')
        return if (h > 0) "$h:${m.toString().padStart(2, '0')}:$ss" else "$m:$ss"
    }
}
