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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.williaaaam.habits.data.Habit
import com.williaaaam.habits.domain.BlockReason
import com.williaaaam.habits.domain.Format
import com.williaaaam.habits.habitsApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Full-screen lock shown on top of a blocked app. Back always goes to the home screen. */
class BlockActivity : ComponentActivity() {

    private var blockedPackage by mutableStateOf("")
    private var reason by mutableStateOf(BlockReason.NO_CREDIT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent(intent)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goHome()
        })
        setContent {
            HabitsTheme {
                Surface(Modifier.fillMaxSize()) {
                    BlockContent(
                        packageName = blockedPackage,
                        reason = reason,
                        goHome = ::goHome,
                        openHabits = ::openHabits,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readIntent(intent)
    }

    private fun readIntent(intent: Intent) {
        blockedPackage = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        reason = intent.getStringExtra(EXTRA_REASON)
            ?.let { runCatching { BlockReason.valueOf(it) }.getOrNull() }
            ?: BlockReason.NO_CREDIT
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        finish()
    }

    private fun openHabits() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        )
        finish()
    }

    companion object {
        private const val EXTRA_PACKAGE = "package"
        private const val EXTRA_REASON = "reason"

        fun intent(context: Context, packageName: String, reason: BlockReason): Intent =
            Intent(context, BlockActivity::class.java)
                .putExtra(EXTRA_PACKAGE, packageName)
                .putExtra(EXTRA_REASON, reason.name)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}

@Composable
private fun BlockContent(
    packageName: String,
    reason: BlockReason,
    goHome: () -> Unit,
    openHabits: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.habitsApp
    val scope = rememberCoroutineScope()
    val label = remember(packageName) { InstalledApps.label(context, packageName) }
    val icon = remember(packageName) { InstalledApps.icon(context.packageManager, packageName) }
    val habits by app.repository.habits.collectAsState(initial = emptyList())
    val active by app.repository.activeSession.collectAsState(initial = null)

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(32.dp))
        if (icon != null) {
            Image(icon, contentDescription = null, modifier = Modifier.size(72.dp))
        } else {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(72.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("$label is locked", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))

        val session = active
        if (reason == BlockReason.HABIT_RUNNING && session != null) {
            Text(
                "You're doing ${session.habitName} (${Format.clock(now - session.startedAt)}). " +
                    "Finish it first — your apps unlock when you stop the timer.",
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = openHabits, modifier = Modifier.fillMaxWidth()) { Text("Back to my timer") }
        } else {
            Text(
                "You're out of app time for today. Do a habit to earn some:",
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            if (habits.isEmpty()) {
                Button(onClick = openHabits, modifier = Modifier.fillMaxWidth()) { Text("Add a habit") }
            }
            habits.forEach { habit: Habit ->
                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            app.startHabit(habit)
                            openHabits()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Start ${habit.name}")
                        Text("${habit.habitMinutes} min → ${habit.rewardMinutes} min")
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = goHome, modifier = Modifier.fillMaxWidth()) { Text("Go to home screen") }
    }
}
