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
    @Query("SELECT * FROM habits ORDER BY id")
    fun observeAll(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY id")
    suspend fun getAll(): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun get(id: Long): Habit?

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
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE date = :date")
    fun observeForDate(date: String): Flow<List<HabitProgress>>

    @Query("SELECT * FROM progress WHERE date = :date")
    suspend fun getForDate(date: String): List<HabitProgress>

    @Query("SELECT * FROM progress WHERE habitId = :habitId AND date = :date")
    suspend fun get(habitId: Long, date: String): HabitProgress?

    @Upsert
    suspend fun upsert(progress: HabitProgress)

    @Query("DELETE FROM progress WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: Long)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1")
    fun observeActive(): Flow<HabitSession?>

    @Query("SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun getActive(): HabitSession?

    @Insert
    suspend fun insert(session: HabitSession): Long

    @Update
    suspend fun update(session: HabitSession)
}
