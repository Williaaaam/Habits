package com.williaaaam.habits.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.williaaaam.habits.habitsApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles the "Stop" button on the timer notification. */
class StopTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val app = context.habitsApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.stopHabit()
            } finally {
                pending.finish()
            }
        }
    }
}
