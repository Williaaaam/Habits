package com.williaaaam.habits.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.williaaaam.habits.data.HabitsRepository
import com.williaaaam.habits.domain.Decision
import com.williaaaam.habits.domain.Rules
import com.williaaaam.habits.habitsApp
import com.williaaaam.habits.ui.BlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Watches which app is in the foreground and covers blocked apps with [BlockActivity]
 * until all of today's habits are done.
 */
class GateAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repo: HabitsRepository

    private var blocked: Set<String> = emptySet()
    private var currentPackage: String? = null

    /** Windows that float over the current app without replacing it. */
    private val overlayPackages = mutableSetOf("com.android.systemui")

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            currentPackage?.let(::evaluate)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = habitsApp.repository
        getSystemService(InputMethodManager::class.java)?.enabledInputMethodList?.forEach {
            overlayPackages += it.packageName
        }
        ContextCompat.registerReceiver(
            this, unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT), ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        // Re-check the app on screen whenever the rules change: blocked list, progress, or a timer
        // crossing its goal (checked every 15 s), and at midnight when a new day starts.
        val ticker = flow {
            while (true) {
                emit(Unit)
                delay(15_000)
            }
        }
        scope.launch {
            combine(repo.blockedPackages, repo.todayState(), ticker) { packages, _, _ -> packages }
                .collect { packages ->
                    blocked = packages
                    currentPackage?.let(::evaluate)
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg in overlayPackages) return
        currentPackage = pkg
        evaluate(pkg)
    }

    private fun evaluate(pkg: String) {
        if (!::repo.isInitialized || pkg == packageName || pkg !in blocked) return
        scope.launch {
            val decision = Rules.decide(pkg, blocked, repo.isDayComplete())
            if (decision == Decision.Block && pkg == currentPackage) {
                startActivity(BlockActivity.intent(this@GateAccessibilityService, pkg))
            }
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        runCatching { unregisterReceiver(unlockReceiver) }
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun isEnabled(context: Context): Boolean {
            val expected = ComponentName(context, GateAccessibilityService::class.java)
            val enabled = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
        }
    }
}
