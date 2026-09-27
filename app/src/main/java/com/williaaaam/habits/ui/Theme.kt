package com.williaaaam.habits.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.williaaaam.habits.R

/** Grimy stencil letters for headings. */
val Punk = FontFamily(Font(R.font.rubik_dirt))

/** Typewriter-ish mono for everything else. */
val Mono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)

private val Grey900 = Color(0xFF111111)
private val Grey850 = Color(0xFF161616)
private val Grey800 = Color(0xFF1C1C1C)
private val Grey750 = Color(0xFF222222)
private val Grey700 = Color(0xFF2A2A2A)
private val Grey600 = Color(0xFF333333)
private val Grey500 = Color(0xFF555555)
private val Grey400 = Color(0xFF8A8A8A)
private val Grey200 = Color(0xFFBDBDBD)
private val Grey100 = Color(0xFFE0E0E0)

private val Colors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Grey800,
    onPrimaryContainer = Color.White,
    secondary = Grey400,
    onSecondary = Color.Black,
    secondaryContainer = Grey700,
    onSecondaryContainer = Color.White,
    tertiary = Grey200,
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Grey800,
    onSurfaceVariant = Grey200,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Grey900,
    surfaceContainer = Grey850,
    surfaceContainerHigh = Grey800,
    surfaceContainerHighest = Grey750,
    outline = Grey500,
    outlineVariant = Grey600,
    error = Grey100,
    onError = Color.Black,
    errorContainer = Grey700,
    onErrorContainer = Color.White,
)

private val Type = Typography().let { t ->
    fun TextStyle.punk() = copy(fontFamily = Punk, fontWeight = FontWeight.Normal)
    fun TextStyle.mono() = copy(fontFamily = Mono)
    Typography(
        displayLarge = t.displayLarge.punk(),
        displayMedium = t.displayMedium.punk(),
        displaySmall = t.displaySmall.punk(),
        headlineLarge = t.headlineLarge.punk(),
        headlineMedium = t.headlineMedium.punk(),
        headlineSmall = t.headlineSmall.punk(),
        titleLarge = t.titleLarge.punk(),
        titleMedium = t.titleMedium.punk(),
        titleSmall = t.titleSmall.mono().copy(fontWeight = FontWeight.Bold),
        bodyLarge = t.bodyLarge.mono(),
        bodyMedium = t.bodyMedium.mono(),
        bodySmall = t.bodySmall.mono(),
        labelLarge = t.labelLarge.mono().copy(fontWeight = FontWeight.Bold),
        labelMedium = t.labelMedium.mono(),
        labelSmall = t.labelSmall.mono(),
    )
}

private val Corners = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(6.dp),
)

/** Black, white and grey only — on purpose, in light and dark mode alike. */
@Composable
fun HabitsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, shapes = Corners, content = content)
}
