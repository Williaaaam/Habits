package com.williaaaam.habits.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

private enum class Tab(val label: String, val icon: ImageVector) {
    Today("Today", Icons.Default.Home),
    Progress("Progress", Icons.Default.DateRange),
    Habits("Habits", Icons.AutoMirrored.Filled.List),
    Apps("Apps", Icons.Default.Lock),
    Setup("Setup", Icons.Default.Settings),
}

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val firstTab = if (PermissionState.read(this).accessibility) Tab.Today else Tab.Setup
        setContent {
            HabitsTheme {
                var tab by rememberSaveable { mutableStateOf(firstTab) }
                OnResume { vm.refresh() }
                Scaffold(
                    topBar = { TopAppBar(title = { Text(if (tab == Tab.Today) "Habits" else if (tab == Tab.Apps) "Blocked apps" else tab.label) }) },
                    bottomBar = {
                        NavigationBar {
                            Tab.entries.forEach { t ->
                                NavigationBarItem(
                                    selected = tab == t,
                                    onClick = { tab = t },
                                    icon = { Icon(t.icon, contentDescription = null) },
                                    label = { Text(t.label) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (tab) {
                            Tab.Today -> TodayScreen(
                                vm,
                                openSetup = { tab = Tab.Setup },
                                openHabits = { tab = Tab.Habits },
                                openApps = { tab = Tab.Apps },
                            )
                            Tab.Progress -> ProgressScreen(vm)
                            Tab.Habits -> HabitsScreen(vm)
                            Tab.Apps -> AppsScreen(vm)
                            Tab.Setup -> SetupScreen(vm)
                        }
                    }
                }
            }
        }
    }
}
