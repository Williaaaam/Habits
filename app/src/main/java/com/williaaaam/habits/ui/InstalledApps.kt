package com.williaaaam.habits.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class InstalledApp(val packageName: String, val label: String, val icon: ImageBitmap?)

object InstalledApps {
    /** Every app with a launcher icon, except this one. Slow-ish: call off the main thread. */
    fun load(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(launcher, 0)
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != context.packageName }
            .map { pkg ->
                InstalledApp(pkg, label(context, pkg), icon(pm, pkg))
            }
            .sortedBy { it.label.lowercase() }
    }

    fun label(context: Context, packageName: String): String = runCatching {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)

    fun icon(pm: PackageManager, packageName: String): ImageBitmap? = runCatching {
        pm.getApplicationIcon(packageName).toBitmap(96, 96).asImageBitmap()
    }.getOrNull()
}
