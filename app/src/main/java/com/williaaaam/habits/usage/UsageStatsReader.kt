package com.williaaaam.habits.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.williaaaam.habits.domain.Rules

/** Today's screen time per app, from Android's own usage history. Needs "Usage access". */
object UsageStatsReader {

    @Suppress("DEPRECATION")
    fun hasPermission(context: Context): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Foreground milliseconds per package since local midnight. */
    @Suppress("DEPRECATION") // MOVE_TO_* are the pre-API-29 names of ACTIVITY_RESUMED/PAUSED
    fun todayForegroundMillis(context: Context): Map<String, Long> {
        if (!hasPermission(context)) return emptyMap()
        val usm = context.getSystemService(UsageStatsManager::class.java)
        val now = System.currentTimeMillis()
        val events = usm.queryEvents(Rules.startOfDay(now), now)
        val list = ArrayList<FgEvent>()
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> list += FgEvent(event.packageName, true, event.timeStamp)
                UsageEvents.Event.MOVE_TO_BACKGROUND -> list += FgEvent(event.packageName, false, event.timeStamp)
            }
        }
        return UsageMath.foregroundTotals(list, now)
    }
}
