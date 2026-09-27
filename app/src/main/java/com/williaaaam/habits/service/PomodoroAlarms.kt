package com.williaaaam.habits.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.williaaaam.habits.data.HabitSession
import com.williaaaam.habits.domain.Pomodoro
import com.williaaaam.habits.habitsApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Wakes up at each focus/break switch to alert you and schedule the next switch. */
object PomodoroAlarms {
    private const val EXTRA_SESSION = "session"

    fun scheduleNext(context: Context, session: HabitSession, now: Long = System.currentTimeMillis()) {
        val phase = Pomodoro.phase(now - session.startedAt)
        val at = session.startedAt + phase.endsAtElapsed
        val alarms = context.getSystemService(AlarmManager::class.java)
        val pi = pendingIntent(context, session.id)
        val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()
        if (exact) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, 0))
    }

    // The request code is fixed so there's only ever one pending alarm; the session id rides as an extra.
    private fun pendingIntent(context: Context, sessionId: Long): PendingIntent = PendingIntent.getBroadcast(
        context, 1,
        Intent(context, PomodoroReceiver::class.java).putExtra(EXTRA_SESSION, sessionId),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    class PomodoroReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val sessionId = intent.getLongExtra(EXTRA_SESSION, -1)
            val pending = goAsync()
            val app = context.habitsApp
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val session = app.repository.activeSession()
                    if (session != null && session.id == sessionId && session.pomodoro) {
                        // Nudge past the boundary so we read the phase that just began.
                        val now = System.currentTimeMillis() + 1_000
                        val phase = Pomodoro.phase(now - session.startedAt)
                        TimerNotifier.showPhaseAlert(context, session, focusStarting = phase.focus)
                        TimerNotifier.showRunning(context, session, now)
                        scheduleNext(context, session, now)
                    }
                } finally {
                    pending.finish()
                }
            }
        }
    }
}
