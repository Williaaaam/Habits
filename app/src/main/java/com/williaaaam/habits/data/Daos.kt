package com.williaaaam.habits.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Habit>>

    @Upsert
    suspend fun upsert(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)
}

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY label COLLATE NOCASE")
    fun observeAll(): Flow<List<BlockedApp>>

    @Upsert
    suspend fun upsert(app: BlockedApp)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1")
    fun observeActive(): Flow<HabitSession?>

    @Query("SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun getActive(): HabitSession?

    @Query("SELECT * FROM sessions WHERE endedAt IS NOT NULL AND endedAt >= :since ORDER BY endedAt DESC")
    fun observeFinishedSince(since: Long): Flow<List<HabitSession>>

    @Insert
    suspend fun insert(session: HabitSession): Long

    @Update
    suspend fun update(session: HabitSession)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger WHERE date = :date")
    fun observe(date: String): Flow<DailyLedger?>

    @Query("SELECT * FROM ledger WHERE date = :date")
    suspend fun get(date: String): DailyLedger?

    @Query("INSERT OR IGNORE INTO ledger (date, earnedSeconds, spentSeconds) VALUES (:date, 0, 0)")
    suspend fun ensure(date: String)

    @Query("UPDATE ledger SET earnedSeconds = earnedSeconds + :seconds WHERE date = :date")
    suspend fun addEarned(date: String, seconds: Long)

    @Query("UPDATE ledger SET spentSeconds = spentSeconds + :seconds WHERE date = :date")
    suspend fun addSpent(date: String, seconds: Long)
}
