package com.williaaaam.habits.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun OnResume(action: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) action() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier) = HorizontalDivider(modifier, thickness = 1.dp, color = Palette.Divider)

/** A feed-style block: optional bold title, content, then a full-width hairline. */
@Composable
fun Section(title: String? = null, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            }
            content()
        }
        Hairline()
    }
}

/** The white "Post" pill: the one main action on a screen. */
@Composable
fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Palette.Text,
            contentColor = Palette.Black,
            disabledContainerColor = Palette.Text.copy(alpha = 0.5f),
            disabledContentColor = Palette.Black.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

/** The outlined "Following" pill for secondary actions. */
@Composable
fun OutlinedPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, small: Boolean = false) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(if (small) 32.dp else 44.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, Palette.PillOutline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Palette.Text),
        contentPadding = PaddingValues(horizontal = if (small) 16.dp else 20.dp),
    ) { Text(text, style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge) }
}

/** Round avatar with the habit's first letter; blue once done. */
@Composable
fun HabitAvatar(name: String, done: Boolean) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(if (done) Palette.Blue else Palette.RaisedHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(Icons.Default.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
        } else {
            Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Round check toggle: grey ring when off, filled blue with a tick when on. */
@Composable
fun RoundCheck(checked: Boolean, onChange: ((Boolean) -> Unit)?) {
    val base = Modifier.size(28.dp).clip(CircleShape)
    val clickable = if (onChange != null) base.clickable { onChange(!checked) } else base
    Box(
        if (checked) clickable.background(Palette.Blue) else clickable.border(2.dp, Palette.PillOutline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                Icons.Default.Check, contentDescription = "Done",
                tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun ThinProgress(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
        color = Palette.Blue,
        trackColor = Palette.Divider,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}
