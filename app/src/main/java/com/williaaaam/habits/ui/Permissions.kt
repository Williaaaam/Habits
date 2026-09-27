package com.williaaaam.habits.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.williaaaam.habits.service.GateAccessibilityService
import com.williaaaam.habits.usage.UsageStatsReader

data class PermissionState(
    val accessibility: Boolean,
    val usageAccess: Boolean,
    val notifications: Boolean,
    val batteryUnrestricted: Boolean,
) {
    companion object {
        fun read(context: Context) = PermissionState(
            accessibility = GateAccessibilityService.isEnabled(context),
            usageAccess = UsageStatsReader.hasPermission(context),
            notifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED,
            batteryUnrestricted = context.getSystemService(PowerManager::class.java)
                .isIgnoringBatteryOptimizations(context.packageName),
        )
    }
}

object SettingsIntents {
    fun accessibility() = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    fun usageAccess() = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun appInfo(context: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))

    @SuppressLint("BatteryLife")
    fun batteryOptimization(context: Context) =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
}

fun Context.launchSafely(intent: Intent) {
    runCatching { startActivity(intent) }
        .onFailure { runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) } }
}
