package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.AppThemeMode
import com.example.data.model.PastelTheme

fun createPastelLightColorScheme(pastelTheme: PastelTheme): ColorScheme {
    val primary = Color(pastelTheme.primaryColor)
    val secondary = Color(pastelTheme.secondaryColor)
    val bg = Color(pastelTheme.lightBgColor)
    val container = Color(pastelTheme.lightContainerColor)

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = container,
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = container,
        onSecondaryContainer = secondary,
        tertiary = secondary,
        onTertiary = Color.White,
        background = bg,
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF64748B),
        outline = Color(0xFFE2E8F0),
        error = ScreeneryRecordRed,
        onError = Color.White
    )
}

fun createPastelDarkColorScheme(pastelTheme: PastelTheme): ColorScheme {
    val primary = Color(pastelTheme.darkPrimaryColor)
    val secondary = Color(pastelTheme.secondaryColor)
    val container = Color(pastelTheme.darkContainerColor)

    return darkColorScheme(
        primary = primary,
        onPrimary = Color.Black,
        primaryContainer = container,
        onPrimaryContainer = Color(0xFFF8FAFC),
        secondary = secondary,
        onSecondary = Color.Black,
        secondaryContainer = container,
        onSecondaryContainer = Color(0xFFF1F5F9),
        tertiary = secondary,
        onTertiary = Color.Black,
        background = Color(0xFF0A0E1A),
        onBackground = Color(0xFFF8FAFC),
        surface = Color(0xFF121829),
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFF1E293B),
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = Color(0xFF334155),
        error = ScreeneryRecordRed,
        onError = Color.White
    )
}

val ScreeneryShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    pastelTheme: PastelTheme = PastelTheme.LAVENDER,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) {
        createPastelDarkColorScheme(pastelTheme)
    } else {
        createPastelLightColorScheme(pastelTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ScreeneryShapes,
        content = content
    )
}
