package com.williaaaam.habits.domain

import java.time.Instant
import java.time.ZoneId

enum class BlockReason { HABIT_RUNNING, NO_CREDIT }

sealed interface Decision {
    /** The app isn't on the blocked list; nothing to do. */
    data object NotGated : Decision

    /** A blocked app that may be used right now; its use drains credit. */
    data object Allow : Decision

    data class Block(val reason: BlockReason) : Decision
}

object Rules {

    /**
     * Seconds of app time earned for doing a habit for [elapsedMs].
     * Proportional to the habit's ratio (e.g. 30 min → 15 min), rounded down to whole minutes.
     */
    fun creditSeconds(elapsedMs: Long, habitMinutes: Int, rewardMinutes: Int): Long {
        if (elapsedMs <= 0 || habitMinutes <= 0 || rewardMinutes <= 0) return 0
        val rewardSeconds = (elapsedMs / 1000) * rewardMinutes / habitMinutes
        return rewardSeconds / 60 * 60
    }

    fun decide(
        packageName: String,
        blockedPackages: Set<String>,
        habitRunning: Boolean,
        balanceSeconds: Long,
    ): Decision = when {
        packageName !in blockedPackages -> Decision.NotGated
        habitRunning -> Decision.Block(BlockReason.HABIT_RUNNING)
        balanceSeconds <= 0 -> Decision.Block(BlockReason.NO_CREDIT)
        else -> Decision.Allow
    }

    /** Key for the daily credit ledger; a new key at local midnight resets the credit. */
    fun dayKey(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().toString()

    fun startOfDay(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
            .atStartOfDay(zone).toInstant().toEpochMilli()
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
