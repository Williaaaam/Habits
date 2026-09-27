package com.williaaaam.habits.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.williaaaam.habits.domain.HabitType

/** A daily habit. [goalMinutes] only matters for [HabitType.TIMER] habits. */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: HabitType,
    val goalMinutes: Int,
)

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val label: String,
)

/** How far a habit got on one local day (yyyy-MM-dd). */
@Entity(tableName = "progress", primaryKeys = ["habitId", "date"])
data class HabitProgress(
    val habitId: Long,
    val date: String,
    val seconds: Long = 0,
    val checked: Boolean = false,
)

/**
 * One run of a habit timer. The row with a null [endedAt] is the timer that is running now.
 * The habit's name is copied in, so editing or deleting the habit mid-run is harmless.
 */
@Entity(tableName = "sessions")
data class HabitSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val habitName: String,
    val pomodoro: Boolean,
    val startedAt: Long,
    val endedAt: Long? = null,
    val countedSeconds: Long = 0,
)

/** How many habits were done on a day, frozen as of that day (for the overall heatmap/streak). */
@Entity(tableName = "day_summary")
data class DaySummary(
    @PrimaryKey val date: String,
    val done: Int,
    val total: Int,
)

val DaySummary.complete: Boolean get() = total > 0 && done >= total
