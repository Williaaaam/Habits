package com.williaaaam.habits.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.williaaaam.habits.data.BlockedApp
import com.williaaaam.habits.data.Habit
import com.williaaaam.habits.data.HabitSession
import com.williaaaam.habits.data.TodayState
import com.williaaaam.habits.habitsApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application.habitsApp
    private val repo = app.repository

    private fun <T> Flow<T>.state(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    /** Ticks every second; drives the live timer. */
    val now: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }.state(System.currentTimeMillis())

    /** Today's habits with the running timer's time counted live. */
    val today: StateFlow<TodayState?> = combine(repo.todayInputs(), now) { inputs, time -> inputs.at(time) }.state(null)

    val habits: StateFlow<List<Habit>> = repo.habits.state(emptyList())
    val blockedApps: StateFlow<List<BlockedApp>> = repo.blockedApps.state(emptyList())
    val activeSession: StateFlow<HabitSession?> = repo.activeSession.state(null)

    private val _installedApps = MutableStateFlow<List<InstalledApp>?>(null)
    val installedApps: StateFlow<List<InstalledApp>?> = _installedApps.asStateFlow()

    private val _permissions = MutableStateFlow(PermissionState.read(application))
    val permissions: StateFlow<PermissionState> = _permissions.asStateFlow()

    /** Called whenever the app comes back to the foreground (e.g. from Settings). */
    fun refresh() {
        _permissions.value = PermissionState.read(app)
    }

    fun loadInstalledApps() {
        if (_installedApps.value != null) return
        viewModelScope.launch {
            _installedApps.value = withContext(Dispatchers.IO) { InstalledApps.load(app) }
        }
    }

    fun startTimer(habit: Habit, pomodoro: Boolean) = viewModelScope.launch { app.startTimer(habit, pomodoro) }
    fun stopTimer() = viewModelScope.launch { app.stopTimer() }
    fun setChecked(habit: Habit, checked: Boolean) = viewModelScope.launch { app.setChecked(habit, checked) }
    fun saveHabit(habit: Habit) = viewModelScope.launch { repo.saveHabit(habit) }
    fun deleteHabit(habit: Habit) = viewModelScope.launch { repo.deleteHabit(habit) }
    fun setBlocked(installed: InstalledApp, blocked: Boolean) =
        viewModelScope.launch { repo.setBlocked(installed.packageName, installed.label, blocked) }
}
