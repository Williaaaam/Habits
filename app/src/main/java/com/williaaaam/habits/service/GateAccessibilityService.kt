package com.williaaaam.habits.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.williaaaam.habits.data.HabitsRepository
import com.williaaaam.habits.domain.BlockReason
import com.williaaaam.habits.domain.Decision
import com.williaaaam.habits.domain.Rules
import com.williaaaam.habits.habitsApp
import com.williaaaam.habits.ui.BlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Watches which app is in the foreground. Blocked apps are let through only while there is
 * credit and no habit timer is running; credit drains every second they stay on screen.
 */
class GateAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repo: HabitsRepository

    private var blocked: Set<String> = emptySet()
    private var habitRunning = false
    private var currentPackage: String? = null
    private var screenOn = true

    private var spendJob: Job? = null
    private var spendingPackage: String? = null

    /** Windows that float over the current app without replacing it. */
    private val overlayPackages = mutableSetOf("com.android.systemui")

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    screenOn = false
                    stopSpending()
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    screenOn = true
                    currentPackage?.let(::evaluate)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        repo = habitsApp.repository
        getSystemService(InputMethodManager::class.java)?.enabledInputMethodList?.forEach {
            overlayPackages += it.packageName
        }
        ContextCompat.registerReceiver(
            this,
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        scope.launch {
            combine(repo.blockedPackages, repo.activeSession) { packages, session -> packages to (session != null) }
                .collect { (packages, running) ->
                    blocked = packages
                    habitRunning = running
                    currentPackage?.let(::evaluate)
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg in overlayPackages) return
        if (pkg == currentPackage && spendJob?.isActive == true) return
        currentPackage = pkg
        if (pkg == packageName) {
            stopSpending()
            return
        }
        evaluate(pkg)
    }

    private fun evaluate(pkg: String) {
        if (!::repo.isInitialized || pkg == packageName || !screenOn) return
        scope.launch {
            val decision = Rules.decide(pkg, blocked, habitRunning, repo.balanceSeconds())
            if (pkg != currentPackage) return@launch // user already moved on
            when (decision) {
                Decision.NotGated -> stopSpending()
                Decision.Allow -> startSpending(pkg)
                is Decision.Block -> {
                    stopSpending()
                    block(pkg, decision.reason)
                }
            }
        }
    }

    private fun startSpending(pkg: String) {
        if (spendJob?.isActive == true && spendingPackage == pkg) return
        stopSpending()
        spendingPackage = pkg
        spendJob = scope.launch {
            var last = SystemClock.elapsedRealtime()
            var carryMs = 0L
            while (isActive) {
                delay(1000)
                if (!screenOn || currentPackage != pkg) break
                val now = SystemClock.elapsedRealtime()
                carryMs += now - last
                last = now
                val seconds = carryMs / 1000
                if (seconds == 0L) continue
                carryMs -= seconds * 1000
                if (repo.spend(seconds) <= 0) {
                    block(pkg, BlockReason.NO_CREDIT)
                    break
                }
            }
        }
    }

    private fun stopSpending() {
        spendJob?.cancel()
        spendJob = null
        spendingPackage = null
    }

    private fun block(pkg: String, reason: BlockReason) {
        startActivity(BlockActivity.intent(this, pkg, reason))
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
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
