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
import androidx.compose.ui.unit.sp
import com.williaaaam.habits.R

/** X-style dark palette: pure black, soft white, grey secondary text, hairline dividers, one blue. */
object Palette {
    val Black = Color(0xFF000000)
    val Text = Color(0xFFE7E9EA)
    val Secondary = Color(0xFF71767B)
    val Divider = Color(0xFF2F3336)
    val Raised = Color(0xFF16181C)
    val RaisedHigh = Color(0xFF202327)
    val PillOutline = Color(0xFF536471)
    val Blue = Color(0xFF1D9BF0)
    val Red = Color(0xFFF4212E)
}

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

private val Colors = darkColorScheme(
    primary = Palette.Blue,
    onPrimary = Color.White,
    primaryContainer = Palette.Blue,
    onPrimaryContainer = Color.White,
    secondary = Palette.Secondary,
    onSecondary = Palette.Black,
    secondaryContainer = Palette.Divider,
    onSecondaryContainer = Palette.Text,
    tertiary = Palette.Blue,
    onTertiary = Color.White,
    background = Palette.Black,
    onBackground = Palette.Text,
    surface = Palette.Black,
    onSurface = Palette.Text,
    surfaceVariant = Palette.Raised,
    onSurfaceVariant = Palette.Secondary,
    surfaceContainerLowest = Palette.Black,
    surfaceContainerLow = Palette.Black,
    surfaceContainer = Palette.Raised,
    surfaceContainerHigh = Palette.Raised,
    surfaceContainerHighest = Palette.RaisedHigh,
    outline = Palette.Divider,
    outlineVariant = Palette.Divider,
    error = Palette.Red,
    onError = Color.White,
    errorContainer = Palette.Raised,
    onErrorContainer = Palette.Text,
)

private fun inter(size: Int, weight: FontWeight, line: Int = size + 5, tracking: Double = 0.0) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

private val Type = Typography(
    displayLarge = inter(56, FontWeight.ExtraBold, 60, -1.5),
    displayMedium = inter(44, FontWeight.ExtraBold, 48, -1.0),
    displaySmall = inter(34, FontWeight.ExtraBold, 40, -0.5),
    headlineLarge = inter(30, FontWeight.ExtraBold, 36),
    headlineMedium = inter(24, FontWeight.ExtraBold, 30),
    headlineSmall = inter(20, FontWeight.Bold, 26),
    titleLarge = inter(20, FontWeight.ExtraBold, 24),
    titleMedium = inter(17, FontWeight.Bold, 22),
    titleSmall = inter(15, FontWeight.Bold, 20),
    bodyLarge = inter(17, FontWeight.Normal, 24),
    bodyMedium = inter(15, FontWeight.Normal, 20),
    bodySmall = inter(13, FontWeight.Normal, 16),
    labelLarge = inter(15, FontWeight.Bold, 20),
    labelMedium = inter(13, FontWeight.Medium, 16),
    labelSmall = inter(11, FontWeight.Medium, 14),
)

private val Corners = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

@Composable
fun HabitsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, shapes = Corners, content = content)
}
