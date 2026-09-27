package com.williaaaam.habits.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

private enum class Tab(val label: String, val icon: ImageVector) {
    Today("Today", Icons.Default.Home),
    Habits("Habits", Icons.AutoMirrored.Filled.List),
    Apps("Apps", Icons.Default.Lock),
    Setup("Setup", Icons.Default.Settings),
}

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light status/nav bar icons on the black app, whatever the system theme is.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        val firstTab = if (PermissionState.read(this).accessibility) Tab.Today else Tab.Setup
        setContent {
            HabitsTheme {
                var tab by rememberSaveable { mutableStateOf(firstTab) }
                OnResume { vm.refresh() }
                Scaffold(
                    containerColor = Palette.Black,
                    topBar = {
                        Column {
                            CenterAlignedTopAppBar(
                                title = { Text(if (tab == Tab.Today) "Habits" else tab.label, style = MaterialTheme.typography.titleLarge) },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Palette.Black),
                            )
                            Hairline()
                        }
                    },
                    bottomBar = {
                        Column {
                            Hairline()
                            NavigationBar(containerColor = Palette.Black, tonalElevation = 0.dp) {
                                Tab.entries.forEach { t ->
                                    NavigationBarItem(
                                        selected = tab == t,
                                        onClick = { tab = t },
                                        icon = { Icon(t.icon, contentDescription = t.label) },
                                        alwaysShowLabel = false,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Palette.Text,
                                            unselectedIconColor = Palette.Secondary,
                                            indicatorColor = Color.Transparent,
                                        ),
                                    )
                                }
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
                            )
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
