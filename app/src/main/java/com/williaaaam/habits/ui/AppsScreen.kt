package com.williaaaam.habits.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppsScreen(vm: MainViewModel) {
    LaunchedEffect(Unit) { vm.loadInstalledApps() }
    val installed by vm.installedApps.collectAsStateWithLifecycle()
    val blocked by vm.blockedApps.collectAsStateWithLifecycle()
    val blockedSet = remember(blocked) { blocked.mapTo(HashSet()) { it.packageName } }
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        TextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Palette.Secondary) },
            placeholder = { Text("Search apps", color = Palette.Secondary) },
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Palette.RaisedHigh,
                unfocusedContainerColor = Palette.RaisedHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Palette.Blue,
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp, 16.dp, 4.dp),
        )
        Text(
            "${blocked.size} locked until your habits are done",
            style = MaterialTheme.typography.bodySmall,
            color = Palette.Secondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Hairline()
        val apps = installed
        if (apps == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Palette.Blue) }
        } else {
            // Blocked apps first, then alphabetical.
            val shown = apps
                .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
                .sortedBy { it.packageName !in blockedSet }
            LazyColumn(Modifier.fillMaxSize()) {
                items(shown, key = { it.packageName }) { app ->
                    val checked = app.packageName in blockedSet
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.setBlocked(app, !checked) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val icon = app.icon
                        if (icon != null) {
                            Image(icon, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape))
                        } else {
                            Spacer(Modifier.size(40.dp))
                        }
                        Text(app.label, Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.titleSmall)
                        RoundCheck(checked) { vm.setBlocked(app, it) }
                    }
                }
            }
        }
    }
}
