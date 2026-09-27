package com.williaaaam.habits.data

import androidx.room.withTransaction
import com.williaaaam.habits.domain.Pomodoro
import com.williaaaam.habits.domain.Rules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsRepository(
    private val db: AppDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val habitDao = db.habitDao()
    private val blockedAppDao = db.blockedAppDao()
    private val progressDao = db.progressDao()
    private val sessionDao = db.sessionDao()

    val habits: Flow<List<Habit>> = habitDao.observeAll()
    val blockedApps: Flow<List<BlockedApp>> = blockedAppDao.observeAll()
    val blockedPackages: Flow<Set<String>> = blockedApps.map { apps -> apps.mapTo(HashSet()) { it.packageName } }
    val activeSession: Flow<HabitSession?> = sessionDao.observeActive()

    /** The current day key, re-checked every [pollMs] so midnight is noticed. */
    fun today(pollMs: Long = 30_000): Flow<String> = flow {
        while (true) {
            emit(Rules.dayKey(clock()))
            delay(pollMs)
        }
    }.distinctUntilChanged()

    /** Today's stored habits/progress/timer; changes when any of them (or the date) changes. */
    fun todayInputs(): Flow<TodayInputs> = today().flatMapLatest { date ->
        combine(habitDao.observeAll(), progressDao.observeForDate(date), sessionDao.observeActive()) { h, p, s ->
            TodayInputs(date, h, p, s)
        }
    }

    /** Today's state as of each change (a running timer is counted up to that moment). */
    fun todayState(): Flow<TodayState> = todayInputs().map { it.at(clock()) }

    /** Whether all of today's habits are done right now, counting a running timer's time. */
    suspend fun isDayComplete(): Boolean {
        val now = clock()
        val date = Rules.dayKey(now)
        return TodayState.build(date, habitDao.getAll(), progressDao.getForDate(date), sessionDao.getActive(), now).complete
    }

    suspend fun saveHabit(habit: Habit) = habitDao.upsert(habit)

    suspend fun deleteHabit(habit: Habit) = db.withTransaction {
        habitDao.delete(habit)
        progressDao.deleteForHabit(habit.id)
    }

    suspend fun setBlocked(packageName: String, label: String, blocked: Boolean) {
        if (blocked) blockedAppDao.upsert(BlockedApp(packageName, label)) else blockedAppDao.delete(packageName)
    }

    suspend fun activeSession(): HabitSession? = sessionDao.getActive()

    /** Starts a timer for [habit]; returns null if another timer is already running. */
    suspend fun startTimer(habit: Habit, pomodoro: Boolean): HabitSession? = db.withTransaction {
        if (sessionDao.getActive() != null) return@withTransaction null
        val session = HabitSession(habitId = habit.id, habitName = habit.name, pomodoro = pomodoro, startedAt = clock())
        session.copy(id = sessionDao.insert(session))
    }

    /** Stops the running timer and adds its (focus) time to today's progress for that habit. */
    suspend fun stopTimer(): HabitSession? = db.withTransaction {
        val session = sessionDao.getActive() ?: return@withTransaction null
        val end = clock()
        val counted = Pomodoro.countedMs(end - session.startedAt, session.pomodoro) / 1000
        val done = session.copy(endedAt = end, countedSeconds = counted)
        sessionDao.update(done)
        val date = Rules.dayKey(end)
        val p = progressDao.get(session.habitId, date) ?: HabitProgress(session.habitId, date)
        progressDao.upsert(p.copy(seconds = p.seconds + counted))
        done
    }

    suspend fun setChecked(habit: Habit, checked: Boolean) {
        val date = Rules.dayKey(clock())
        val p = progressDao.get(habit.id, date) ?: HabitProgress(habit.id, date)
        progressDao.upsert(p.copy(checked = checked))
    }
}
