package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ScreeneryPrimary,
    onPrimary = Color.White,
    primaryContainer = ScreeneryPillActiveBg,
    onPrimaryContainer = ScreeneryPrimaryDark,
    secondary = ScreenerySecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = ScreenerySecondary,
    tertiary = ScreeneryTertiary,
    onTertiary = Color.White,
    background = ScreeneryBg,
    onBackground = ScreeneryTextPrimary,
    surface = ScreenerySurface,
    onSurface = ScreeneryTextPrimary,
    surfaceVariant = ScreenerySurfaceVariant,
    onSurfaceVariant = ScreeneryTextSecondary,
    outline = ScreeneryBorder,
    error = ScreeneryRecordRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = ScreeneryPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = ScreenerySecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = ScreeneryTertiary,
    onTertiary = Color.White,
    background = ScreeneryDarkBg,
    onBackground = ScreeneryDarkTextPrimary,
    surface = ScreeneryDarkSurface,
    onSurface = ScreeneryDarkTextPrimary,
    surfaceVariant = ScreeneryDarkSurfaceVariant,
    onSurfaceVariant = ScreeneryDarkTextSecondary,
    outline = ScreeneryDarkBorder,
    error = ScreeneryRecordRed,
    onError = Color.White
)

val ScreeneryShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ScreeneryShapes,
        content = content
    )
}
