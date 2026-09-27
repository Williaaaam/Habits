package com.williaaaam.habits.data

import com.williaaaam.habits.domain.HabitType
import com.williaaaam.habits.domain.Pomodoro
import com.williaaaam.habits.domain.Rules

data class HabitStatus(val habit: Habit, val seconds: Long, val checked: Boolean) {
    val done: Boolean get() = Rules.isHabitDone(habit.type, seconds, checked, habit.goalMinutes)

    /** 0..1, for progress bars. */
    val fraction: Float get() = when {
        done -> 1f
        habit.type == HabitType.CHECK || habit.goalMinutes <= 0 -> 0f
        else -> (seconds.toFloat() / (habit.goalMinutes * 60f)).coerceIn(0f, 1f)
    }
}

/** The stored facts about a day; [at] turns them into a [TodayState] at a given moment. */
data class TodayInputs(
    val date: String,
    val habits: List<Habit>,
    val progress: List<HabitProgress>,
    val active: HabitSession?,
) {
    fun at(now: Long): TodayState = TodayState.build(date, habits, progress, active, now)
}

/** Everything about today, with a running timer's time included live. */
data class TodayState(
    val date: String,
    val habits: List<HabitStatus>,
    val active: HabitSession?,
) {
    val doneCount: Int get() = habits.count { it.done }
    val complete: Boolean get() = Rules.isDayComplete(habits.map { it.done })

    companion object {
        fun build(
            date: String,
            habits: List<Habit>,
            progress: List<HabitProgress>,
            active: HabitSession?,
            now: Long,
        ): TodayState {
            val byHabit = progress.associateBy { it.habitId }
            val liveSeconds = active?.let { Pomodoro.countedMs(now - it.startedAt, it.pomodoro) / 1000 } ?: 0
            val statuses = habits.map { habit ->
                val p = byHabit[habit.id]
                val extra = if (active?.habitId == habit.id) liveSeconds else 0
                HabitStatus(habit, (p?.seconds ?: 0) + extra, p?.checked ?: false)
            }
            return TodayState(date, statuses, active)
        }
    }
}
