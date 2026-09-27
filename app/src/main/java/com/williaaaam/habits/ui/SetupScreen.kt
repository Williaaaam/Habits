package com.williaaaam.habits.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SetupScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val permissions by vm.permissions.collectAsStateWithLifecycle()
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        vm.refresh()
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Three switches and you're set. Nothing leaves your phone.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Step(
            title = "1. Blocker",
            done = permissions.accessibility,
            body = "Accessibility → Installed/Downloaded apps → Habits → ON. This is what locks apps.",
            action = "Open accessibility",
            onAction = { context.launchSafely(SettingsIntents.accessibility()) },
        ) {
            if (!permissions.accessibility && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Text(
                    "Greyed out? App info → ⋮ → Allow restricted settings, then try again.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(onClick = { context.launchSafely(SettingsIntents.appInfo(context)) }) {
                    Text("Open app info")
                }
            }
        }

        Step(
            title = "2. Notifications",
            done = permissions.notifications,
            body = "For the timer and Pomodoro alerts.",
            action = "Allow",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        )

        Step(
            title = "3. Battery",
            done = permissions.batteryUnrestricted,
            body = "Stops your phone from killing the blocker in the background.",
            action = "Don't optimize",
            onAction = { context.launchSafely(SettingsIntents.batteryOptimization(context)) },
        )
    }
}

@Composable
private fun Step(
    title: String,
    done: Boolean,
    body: String,
    action: String,
    onAction: () -> Unit,
    extra: @Composable () -> Unit = {},
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (done) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (done) "Done" else "Not done",
                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            )
            Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp))
        }
        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(body, style = MaterialTheme.typography.bodyMedium)
            if (!done) {
                Button(onClick = onAction) { Text(action) }
                extra()
            }
        }
    }
}
