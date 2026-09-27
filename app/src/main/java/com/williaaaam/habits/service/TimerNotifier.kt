package com.williaaaam.habits.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.williaaaam.habits.R
import com.williaaaam.habits.data.HabitSession
import com.williaaaam.habits.domain.Format
import com.williaaaam.habits.domain.Pomodoro
import com.williaaaam.habits.ui.MainActivity

object TimerNotifier {
    private const val TIMER_CHANNEL = "habit_timer"
    private const val ALERT_CHANNEL = "pomodoro_alerts"
    private const val TIMER_ID = 1
    private const val ALERT_ID = 2

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(TIMER_CHANNEL, "Habit timer", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shows the habit timer while it runs"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(ALERT_CHANNEL, "Pomodoro alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Tells you when a focus block or break ends"
            },
        )
    }

    /** The ongoing timer notification. Pomodoro timers count down to the end of the current phase. */
    fun showRunning(context: Context, session: HabitSession, now: Long = System.currentTimeMillis()) {
        val builder = Notification.Builder(context, TIMER_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_habit)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setContentIntent(openApp(context))
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.ic_stat_habit), "Stop", stopIntent(context),
                ).build(),
            )
        if (session.pomodoro) {
            val phase = Pomodoro.phase(now - session.startedAt)
            builder
                .setContentTitle("${session.habitName} · ${if (phase.focus) "Focus" else "Break"} (round ${phase.round})")
                .setContentText(if (phase.focus) "Stay on it — break in a bit." else "Take a breather. Break time doesn't count.")
                .setChronometerCountDown(true)
                .setWhen(session.startedAt + phase.endsAtElapsed)
        } else {
            builder
                .setContentTitle("${session.habitName} in progress")
                .setContentText("Stop the timer when you're done.")
                .setWhen(session.startedAt)
        }
        notify(context, TIMER_ID, builder.build())
    }

    fun showPhaseAlert(context: Context, session: HabitSession, focusStarting: Boolean) {
        val notification = Notification.Builder(context, ALERT_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_habit)
            .setContentTitle(if (focusStarting) "Break's over" else "Focus block done")
            .setContentText(
                if (focusStarting) "Back to ${session.habitName} for ${Pomodoro.FOCUS_MINUTES} min."
                else "Take ${Pomodoro.BREAK_MINUTES} min off.",
            )
            .setAutoCancel(true)
            .setContentIntent(openApp(context))
            .build()
        notify(context, ALERT_ID, notification)
    }

    fun showFinished(context: Context, session: HabitSession) {
        val notification = Notification.Builder(context, TIMER_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_habit)
            .setContentTitle("${session.habitName}: +${Format.minutes(session.countedSeconds)}")
            .setContentText("Logged for today.")
            .setAutoCancel(true)
            .setContentIntent(openApp(context))
            .build()
        notify(context, TIMER_ID, notification)
    }

    private fun notify(context: Context, id: Int, notification: Notification) {
        val manager = context.getSystemService(NotificationManager::class.java)
        // Without the notification permission this is silently dropped; the in-app timer still works.
        runCatching { manager.notify(id, notification) }
    }

    private fun stopIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, 0,
        Intent(context, StopTimerReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun openApp(context: Context): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
