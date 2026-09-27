package com.williaaaam.habits

import android.app.Application
import com.williaaaam.habits.data.AppDatabase
import com.williaaaam.habits.data.Habit
import com.williaaaam.habits.data.HabitSession
import com.williaaaam.habits.data.HabitsRepository
import com.williaaaam.habits.service.TimerNotifier

class HabitsApp : Application() {
    val repository: HabitsRepository by lazy { HabitsRepository(AppDatabase.create(this)) }

    override fun onCreate() {
        super.onCreate()
        TimerNotifier.createChannel(this)
    }

    /** Starts a habit timer and shows its notification. Returns null if one is already running. */
    suspend fun startHabit(habit: Habit): HabitSession? =
        repository.startHabit(habit)?.also { TimerNotifier.showRunning(this, it) }

    /** Stops the running timer, credits the earned time, and says how much was earned. */
    suspend fun stopHabit(): HabitSession? =
        repository.stopHabit()?.also { TimerNotifier.showFinished(this, it) }
}

val android.content.Context.habitsApp: HabitsApp get() = applicationContext as HabitsApp
