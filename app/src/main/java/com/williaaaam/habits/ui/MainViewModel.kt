package com.williaaaam.habits.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.williaaaam.habits.data.BlockedApp
import com.williaaaam.habits.data.DailyLedger
import com.williaaaam.habits.data.Habit
import com.williaaaam.habits.data.HabitSession
import com.williaaaam.habits.domain.Rules
import com.williaaaam.habits.habitsApp
import com.williaaaam.habits.usage.UsageStatsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application.habitsApp
    private val repo = app.repository

    private fun <T> kotlinx.coroutines.flow.Flow<T>.state(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    /** Ticks every second; drives the timer display and the midnight rollover. */
    val now: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }.state(System.currentTimeMillis())

    private val today = now.map { Rules.dayKey(it) }.distinctUntilChanged()

    val habits: StateFlow<List<Habit>> = repo.habits.state(emptyList())
    val blockedApps: StateFlow<List<BlockedApp>> = repo.blockedApps.state(emptyList())
    val activeSession: StateFlow<HabitSession?> = repo.activeSession.state(null)
    val ledger: StateFlow<DailyLedger?> = today.flatMapLatest { repo.ledger(it) }.state(null)
    val finishedToday: StateFlow<List<HabitSession>> =
        today.flatMapLatest { repo.finishedSessionsSince(Rules.startOfDay(System.currentTimeMillis())) }
            .state(emptyList())

    private val _installedApps = MutableStateFlow<List<InstalledApp>?>(null)
    val installedApps: StateFlow<List<InstalledApp>?> = _installedApps.asStateFlow()

    private val _usage = MutableStateFlow<Map<String, Long>>(emptyMap())
    val usage: StateFlow<Map<String, Long>> = _usage.asStateFlow()

    private val _permissions = MutableStateFlow(PermissionState.read(application))
    val permissions: StateFlow<PermissionState> = _permissions.asStateFlow()

    /** Called whenever the app comes back to the foreground (e.g. from Settings). */
    fun refresh() {
        _permissions.value = PermissionState.read(app)
        viewModelScope.launch {
            _usage.value = withContext(Dispatchers.IO) { UsageStatsReader.todayForegroundMillis(app) }
        }
    }

    fun loadInstalledApps() {
        if (_installedApps.value != null) return
        viewModelScope.launch {
            _installedApps.value = withContext(Dispatchers.IO) { InstalledApps.load(app) }
        }
    }

    fun startHabit(habit: Habit) = viewModelScope.launch { app.startHabit(habit) }
    fun stopHabit() = viewModelScope.launch { app.stopHabit() }
    fun saveHabit(habit: Habit) = viewModelScope.launch { repo.saveHabit(habit) }
    fun deleteHabit(habit: Habit) = viewModelScope.launch { repo.deleteHabit(habit) }
    fun setBlocked(app: InstalledApp, blocked: Boolean) =
        viewModelScope.launch { repo.setBlocked(app.packageName, app.label, blocked) }
}
