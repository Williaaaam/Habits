package com.williaaaam.habits

import com.williaaaam.habits.domain.BlockReason
import com.williaaaam.habits.domain.Decision
import com.williaaaam.habits.domain.Format
import com.williaaaam.habits.domain.Rules
import com.williaaaam.habits.usage.FgEvent
import com.williaaaam.habits.usage.UsageMath
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class RulesTest {
    private val min = 60_000L

    @Test fun fullBlockEarnsFullReward() {
        assertEquals(15 * 60L, Rules.creditSeconds(30 * min, 30, 15))
    }

    @Test fun creditIsProportional() {
        assertEquals(5 * 60L, Rules.creditSeconds(10 * min, 30, 15))
        assertEquals(30 * 60L, Rules.creditSeconds(60 * min, 30, 15))
    }

    @Test fun creditRoundsDownToWholeMinutes() {
        // 3 min of reading at 30→15 is 90 s, rounded down to 1 min.
        assertEquals(60L, Rules.creditSeconds(3 * min, 30, 15))
        assertEquals(0L, Rules.creditSeconds(1 * min, 30, 15))
    }

    @Test fun noCreditForNothing() {
        assertEquals(0L, Rules.creditSeconds(0, 30, 15))
        assertEquals(0L, Rules.creditSeconds(-5, 30, 15))
        assertEquals(0L, Rules.creditSeconds(30 * min, 0, 15))
        assertEquals(0L, Rules.creditSeconds(30 * min, 30, 0))
    }

    private val blocked = setOf("com.instagram.android")

    @Test fun unblockedAppIsNotGated() {
        assertEquals(Decision.NotGated, Rules.decide("com.whatsapp", blocked, habitRunning = true, balanceSeconds = 0))
    }

    @Test fun runningHabitLocksEvenWithCredit() {
        assertEquals(
            Decision.Block(BlockReason.HABIT_RUNNING),
            Rules.decide("com.instagram.android", blocked, habitRunning = true, balanceSeconds = 600),
        )
    }

    @Test fun noCreditBlocks() {
        assertEquals(
            Decision.Block(BlockReason.NO_CREDIT),
            Rules.decide("com.instagram.android", blocked, habitRunning = false, balanceSeconds = 0),
        )
    }

    @Test fun creditAllows() {
        assertEquals(Decision.Allow, Rules.decide("com.instagram.android", blocked, habitRunning = false, balanceSeconds = 1))
    }

    @Test fun dayKeyChangesAtLocalMidnight() {
        val zone = ZoneId.of("America/New_York")
        val beforeMidnight = ZonedDateTime.of(2026, 9, 27, 23, 59, 59, 0, zone).toInstant().toEpochMilli()
        val afterMidnight = beforeMidnight + 1000
        assertEquals("2026-09-27", Rules.dayKey(beforeMidnight, zone))
        assertEquals("2026-09-28", Rules.dayKey(afterMidnight, zone))
        assertEquals(afterMidnight, Rules.startOfDay(afterMidnight + 5 * min, zone))
    }

    @Test fun formatting() {
        assertEquals("0m", Format.minutes(59))
        assertEquals("15m", Format.minutes(15 * 60))
        assertEquals("1h 05m", Format.minutes(65 * 60))
        assertEquals("4:07", Format.clock(247_000))
        assertEquals("1:02:03", Format.clock(3_723_000))
    }

    @Test fun usageTotals() {
        val events = listOf(
            FgEvent("insta", false, 50),          // was already open at window start: ignored
            FgEvent("insta", true, 100),
            FgEvent("insta", false, 400),
            FgEvent("tiktok", true, 500),
            FgEvent("tiktok", false, 700),
            FgEvent("insta", true, 900),          // still open
        )
        assertEquals(mapOf("insta" to 300L + 100L, "tiktok" to 200L), UsageMath.foregroundTotals(events, 1000))
    }
}
