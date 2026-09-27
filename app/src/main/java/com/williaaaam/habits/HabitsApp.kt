package com.williaaaam.habits

import android.app.Application
import android.content.Context
import com.williaaaam.habits.data.AppDatabase
import com.williaaaam.habits.data.Habit
import com.williaaaam.habits.data.HabitSession
import com.williaaaam.habits.data.HabitsRepository
import com.williaaaam.habits.service.PomodoroAlarms
import com.williaaaam.habits.service.TimerNotifier
import com.williaaaam.habits.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HabitsApp : Application() {
    val repository: HabitsRepository by lazy { HabitsRepository(AppDatabase.create(this)) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        TimerNotifier.createChannels(this)
        // Keep the home-screen widget in step with today's progress while the app is alive.
        appScope.launch {
            repository.todayState()
                .map { TodayWidget.Content.from(it) }
                .distinctUntilChanged()
                .collect { TodayWidget.render(this@HabitsApp, it) }
        }
    }

    /** Starts a habit timer with its notification. Returns null if one is already running. */
    suspend fun startTimer(habit: Habit, pomodoro: Boolean): HabitSession? =
        repository.startTimer(habit, pomodoro)?.also {
            TimerNotifier.showRunning(this, it)
            if (it.pomodoro) PomodoroAlarms.scheduleNext(this, it)
        }

    /** Stops the running timer, records its time, and says how it went. */
    suspend fun stopTimer(): HabitSession? =
        repository.stopTimer()?.also {
            PomodoroAlarms.cancel(this)
            TimerNotifier.showFinished(this, it)
        }

    suspend fun setChecked(habit: Habit, checked: Boolean) = repository.setChecked(habit, checked)
}

val Context.habitsApp: HabitsApp get() = applicationContext as HabitsApp
