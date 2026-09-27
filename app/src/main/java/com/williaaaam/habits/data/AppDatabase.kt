package com.williaaaam.habits.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Habit::class, BlockedApp::class, HabitProgress::class, HabitSession::class, DaySummary::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun progressDao(): ProgressDao
    abstract fun sessionDao(): SessionDao
    abstract fun daySummaryDao(): DaySummaryDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "habits.db")
                // Version 1 was the "minutes for minutes" prototype; its data doesn't carry over.
                .fallbackToDestructiveMigrationFrom(1)
                .build()
    }
}
