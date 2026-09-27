package com.williaaaam.habits.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.williaaaam.habits.R
import com.williaaaam.habits.data.TodayState
import com.williaaaam.habits.domain.HabitType
import com.williaaaam.habits.habitsApp
import com.williaaaam.habits.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Home-screen widget: today's habits and whether your apps are unlocked. */
class TodayWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        val app = context.habitsApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                render(context, Content.from(app.repository.todayState().first()))
            } finally {
                pending.finish()
            }
        }
    }

    data class Content(val status: String, val habits: String) {
        companion object {
            fun from(state: TodayState): Content {
                val status = when {
                    state.habits.isEmpty() -> "No habits yet"
                    state.complete -> "Unlocked"
                    else -> "Locked · ${state.doneCount} of ${state.habits.size}"
                }
                val habits = state.habits.joinToString("\n") { s ->
                    val mark = if (s.done) "✓" else "○"
                    val detail = if (s.habit.type == HabitType.TIMER) {
                        " ${minOf(s.seconds / 60, s.habit.goalMinutes.toLong())}/${s.habit.goalMinutes}m"
                    } else {
                        ""
                    }
                    "$mark ${s.habit.name}$detail"
                }
                return Content(status, habits)
            }
        }
    }

    companion object {
        fun render(context: Context, content: Content) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TodayWidget::class.java))
            if (ids.isEmpty()) return
            val open = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val views = RemoteViews(context.packageName, R.layout.widget_today).apply {
                setTextViewText(R.id.widget_status, content.status)
                setTextViewText(R.id.widget_habits, content.habits)
                setOnClickPendingIntent(R.id.widget_root, open)
            }
            manager.updateAppWidget(ids, views)
        }
    }
}
