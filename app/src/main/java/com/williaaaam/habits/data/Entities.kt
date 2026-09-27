package com.williaaaam.habits.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A habit and its exchange rate: [habitMinutes] of doing it earns [rewardMinutes] of app time. */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val habitMinutes: Int,
    val rewardMinutes: Int,
)

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val label: String,
)

/**
 * One run of a habit timer. The row with a null [endedAt] is the timer that is running now.
 * The habit's name and ratio are copied in, so editing or deleting the habit mid-run is harmless.
 */
@Entity(tableName = "sessions")
data class HabitSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val habitName: String,
    val habitMinutes: Int,
    val rewardMinutes: Int,
    val startedAt: Long,
    val endedAt: Long? = null,
    val earnedSeconds: Long = 0,
)

/** App-time credit for one local day (yyyy-MM-dd). A new day starts with a new, empty row. */
@Entity(tableName = "ledger")
data class DailyLedger(
    @PrimaryKey val date: String,
    val earnedSeconds: Long = 0,
    val spentSeconds: Long = 0,
) {
    val balanceSeconds: Long get() = (earnedSeconds - spentSeconds).coerceAtLeast(0)
}
