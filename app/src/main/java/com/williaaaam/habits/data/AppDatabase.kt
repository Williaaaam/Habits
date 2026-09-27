package com.williaaaam.habits.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Habit::class, BlockedApp::class, HabitSession::class, DailyLedger::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun sessionDao(): SessionDao
    abstract fun ledgerDao(): LedgerDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "habits.db").build()
    }
}
