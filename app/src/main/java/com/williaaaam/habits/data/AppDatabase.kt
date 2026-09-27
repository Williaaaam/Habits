package com.williaaaam.habits.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Habit::class, BlockedApp::class, HabitProgress::class, HabitSession::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun progressDao(): ProgressDao
    abstract fun sessionDao(): SessionDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "habits.db")
                // Version 1 was the "minutes for minutes" prototype; its data doesn't carry over.
                .fallbackToDestructiveMigrationFrom(1)
                .addMigrations(DROP_DAY_SUMMARY)
                .build()

        /** Version 3 dropped the streak/heatmap history table; everything else is kept. */
        private val DROP_DAY_SUMMARY = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS day_summary")
            }
        }
    }
}
