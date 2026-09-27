package com.williaaaam.habits.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.williaaaam.habits.domain.Format
import com.williaaaam.habits.domain.HabitType
import com.williaaaam.habits.habitsApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Full-screen lock shown on top of a blocked app. Back always goes to the home screen. */
class BlockActivity : ComponentActivity() {

    private var blockedPackage by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockedPackage = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })
        setContent {
            HabitsTheme {
                Surface(Modifier.fillMaxSize(), color = Palette.Black) {
                    BlockContent(packageName = blockedPackage, goHome = ::goHome, openApp = ::openApp)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        blockedPackage = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        finish()
    }

    private fun openApp() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
        finish()
    }

    /** Once everything is done, hand the user back to the app they were trying to open. */
    private fun openBlockedApp() {
        packageManager.getLaunchIntentForPackage(blockedPackage)?.let { startActivity(it) }
        finish()
    }

    @Composable
    private fun BlockContent(packageName: String, goHome: () -> Unit, openApp: () -> Unit) {
        val context = LocalContext.current
        val app = context.habitsApp
        val scope = rememberCoroutineScope()
        val label = remember(packageName) { InstalledApps.label(context, packageName) }
        val icon = remember(packageName) { InstalledApps.icon(context.packageManager, packageName) }
        val inputs by app.repository.todayInputs().collectAsState(initial = null)

        var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
        LaunchedEffect(Unit) {
            while (true) {
                now = System.currentTimeMillis()
                delay(1_000)
            }
        }
        val state = inputs?.at(now)

        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(32.dp))
            if (icon != null) {
                Image(icon, contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape))
            } else {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(20.dp))

            if (state != null && state.complete) {
                Text("Unlocked", style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center)
                Text(
                    "You did everything today.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Palette.Secondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
                PillButton("Open $label", onClick = ::openBlockedApp, modifier = Modifier.fillMaxWidth())
            } else {
                Text("$label is locked", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (state == null) "" else "${state.doneCount} of ${state.habits.size} habits done. Do the work, then scroll.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Palette.Secondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                Hairline()
                state?.habits?.forEach { status ->
                    val habit = status.habit
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HabitAvatar(habit.name, status.done)
                        Column(Modifier.weight(1f)) {
                            Text(habit.name, style = MaterialTheme.typography.titleSmall)
                            if (habit.type == HabitType.TIMER) {
                                Text(
                                    "${Format.minutes(status.seconds)} of ${habit.goalMinutes}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Palette.Secondary,
                                )
                                if (!status.done) ThinProgress(status.fraction, Modifier.padding(top = 8.dp))
                            }
                        }
                        when {
                            habit.type == HabitType.CHECK -> RoundCheck(status.checked) { checked ->
                                scope.launch { app.setChecked(habit, checked) }
                            }
                            status.done -> RoundCheck(true, null)
                            state?.active?.habitId == habit.id -> OutlinedPill("Running", onClick = openApp, small = true)
                            state?.active == null -> OutlinedPill("Start", small = true, onClick = {
                                scope.launch {
                                    app.startTimer(habit, pomodoro = false)
                                    openApp()
                                }
                            })
                        }
                    }
                    Hairline()
                }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedPill("Go home", onClick = goHome, modifier = Modifier.fillMaxWidth())
        }
    }

    companion object {
        private const val EXTRA_PACKAGE = "package"

        fun intent(context: Context, packageName: String): Intent =
            Intent(context, BlockActivity::class.java)
                .putExtra(EXTRA_PACKAGE, packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
