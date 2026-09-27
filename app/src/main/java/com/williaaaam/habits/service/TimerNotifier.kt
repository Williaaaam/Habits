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
import com.williaaaam.habits.ui.MainActivity

object TimerNotifier {
    private const val CHANNEL_ID = "habit_timer"
    private const val NOTIFICATION_ID = 1

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Habit timer", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Shows the habit timer while it runs"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun showRunning(context: Context, session: HabitSession) {
        val stop = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, StopTimerReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_habit)
            .setContentTitle("${session.habitName} in progress")
            .setContentText("Blocked apps stay locked until you stop.")
            .setOngoing(true)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setWhen(session.startedAt)
            .setContentIntent(openApp(context))
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.ic_stat_habit), "Stop", stop,
                ).build(),
            )
            .build()
        notify(context, notification)
    }

    fun showFinished(context: Context, session: HabitSession) {
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_habit)
            .setContentTitle("${session.habitName} done")
            .setContentText("You earned ${Format.minutes(session.earnedSeconds)} of app time.")
            .setAutoCancel(true)
            .setContentIntent(openApp(context))
            .build()
        notify(context, notification)
    }

    private fun notify(context: Context, notification: Notification) {
        val manager = context.getSystemService(NotificationManager::class.java)
        // Without the notification permission this is silently dropped; the in-app timer still works.
        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }

    private fun openApp(context: Context): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
