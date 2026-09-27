package com.williaaaam.habits.data

import androidx.room.withTransaction
import com.williaaaam.habits.domain.Rules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitsRepository(
    private val db: AppDatabase,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val habitDao = db.habitDao()
    private val blockedAppDao = db.blockedAppDao()
    private val sessionDao = db.sessionDao()
    private val ledgerDao = db.ledgerDao()

    val habits: Flow<List<Habit>> = habitDao.observeAll()
    val blockedApps: Flow<List<BlockedApp>> = blockedAppDao.observeAll()
    val blockedPackages: Flow<Set<String>> = blockedApps.map { apps -> apps.mapTo(HashSet()) { it.packageName } }
    val activeSession: Flow<HabitSession?> = sessionDao.observeActive()

    fun ledger(date: String): Flow<DailyLedger?> = ledgerDao.observe(date)
    fun finishedSessionsSince(since: Long): Flow<List<HabitSession>> = sessionDao.observeFinishedSince(since)

    suspend fun saveHabit(habit: Habit) = habitDao.upsert(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.delete(habit)

    suspend fun setBlocked(packageName: String, label: String, blocked: Boolean) {
        if (blocked) blockedAppDao.upsert(BlockedApp(packageName, label)) else blockedAppDao.delete(packageName)
    }

    suspend fun isHabitRunning(): Boolean = sessionDao.getActive() != null

    /** Starts a timer for [habit]; returns null if another habit is already running. */
    suspend fun startHabit(habit: Habit): HabitSession? = db.withTransaction {
        if (sessionDao.getActive() != null) return@withTransaction null
        val session = HabitSession(
            habitId = habit.id,
            habitName = habit.name,
            habitMinutes = habit.habitMinutes,
            rewardMinutes = habit.rewardMinutes,
            startedAt = clock(),
        )
        session.copy(id = sessionDao.insert(session))
    }

    /** Stops the running timer and adds the earned credit to today's balance. */
    suspend fun stopHabit(): HabitSession? = db.withTransaction {
        val session = sessionDao.getActive() ?: return@withTransaction null
        val end = clock()
        val earned = Rules.creditSeconds(end - session.startedAt, session.habitMinutes, session.rewardMinutes)
        val finished = session.copy(endedAt = end, earnedSeconds = earned)
        sessionDao.update(finished)
        val day = Rules.dayKey(end)
        ledgerDao.ensure(day)
        ledgerDao.addEarned(day, earned)
        finished
    }

    suspend fun balanceSeconds(): Long = ledgerDao.get(Rules.dayKey(clock()))?.balanceSeconds ?: 0

    /** Uses up [seconds] of today's credit and returns what's left. */
    suspend fun spend(seconds: Long): Long = db.withTransaction {
        val day = Rules.dayKey(clock())
        ledgerDao.ensure(day)
        ledgerDao.addSpent(day, seconds)
        ledgerDao.get(day)?.balanceSeconds ?: 0
    }
}
