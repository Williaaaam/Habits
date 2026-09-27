package com.williaaaam.habits

import com.williaaaam.habits.domain.Decision
import com.williaaaam.habits.domain.Format
import com.williaaaam.habits.domain.HabitType
import com.williaaaam.habits.domain.Pomodoro
import com.williaaaam.habits.domain.Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class RulesTest {
    private val min = 60_000L

    @Test fun timerHabitDoneAtGoal() {
        assertFalse(Rules.isHabitDone(HabitType.TIMER, 29 * 60L, checked = false, goalMinutes = 30))
        assertTrue(Rules.isHabitDone(HabitType.TIMER, 30 * 60L, checked = false, goalMinutes = 30))
    }

    @Test fun checkHabitDoneWhenChecked() {
        assertFalse(Rules.isHabitDone(HabitType.CHECK, 9999, checked = false, goalMinutes = 0))
        assertTrue(Rules.isHabitDone(HabitType.CHECK, 0, checked = true, goalMinutes = 0))
    }

    @Test fun dayCompleteNeedsEveryHabit() {
        assertFalse(Rules.isDayComplete(listOf(true, false)))
        assertTrue(Rules.isDayComplete(listOf(true, true)))
        assertTrue(Rules.isDayComplete(emptyList()))
    }

    private val blocked = setOf("com.instagram.android")

    @Test fun decisions() {
        assertEquals(Decision.NotGated, Rules.decide("com.whatsapp", blocked, dayComplete = false))
        assertEquals(Decision.Block, Rules.decide("com.instagram.android", blocked, dayComplete = false))
        assertEquals(Decision.Allow, Rules.decide("com.instagram.android", blocked, dayComplete = true))
    }

    @Test fun dayKeyChangesAtLocalMidnight() {
        val zone = ZoneId.of("America/New_York")
        val beforeMidnight = ZonedDateTime.of(2026, 9, 27, 23, 59, 59, 0, zone).toInstant().toEpochMilli()
        val afterMidnight = beforeMidnight + 1000
        assertEquals("2026-09-27", Rules.dayKey(beforeMidnight, zone))
        assertEquals("2026-09-28", Rules.dayKey(afterMidnight, zone))
        assertEquals(afterMidnight, Rules.startOfDay(afterMidnight + 5 * min, zone))
    }

    @Test fun pomodoroCountsOnlyFocusTime() {
        assertEquals(10 * min, Pomodoro.countedMs(10 * min, pomodoro = false))
        assertEquals(10 * min, Pomodoro.countedMs(10 * min, pomodoro = true))
        assertEquals(25 * min, Pomodoro.countedMs(28 * min, pomodoro = true)) // on break
        assertEquals(27 * min, Pomodoro.countedMs(32 * min, pomodoro = true)) // round 2
        assertEquals(50 * min, Pomodoro.countedMs(60 * min, pomodoro = true))
    }

    @Test fun pomodoroPhases() {
        val p1 = Pomodoro.phase(10 * min)
        assertTrue(p1.focus); assertEquals(1, p1.round); assertEquals(15 * min, p1.msLeft)
        val p2 = Pomodoro.phase(27 * min)
        assertFalse(p2.focus); assertEquals(1, p2.round); assertEquals(3 * min, p2.msLeft); assertEquals(30 * min, p2.endsAtElapsed)
        val p3 = Pomodoro.phase(30 * min)
        assertTrue(p3.focus); assertEquals(2, p3.round); assertEquals(55 * min, p3.endsAtElapsed)
    }

    @Test fun formatting() {
        assertEquals("0m", Format.minutes(59))
        assertEquals("15m", Format.minutes(15 * 60))
        assertEquals("1h 05m", Format.minutes(65 * 60))
        assertEquals("4:07", Format.clock(247_000))
        assertEquals("1:02:03", Format.clock(3_723_000))
    }
}
