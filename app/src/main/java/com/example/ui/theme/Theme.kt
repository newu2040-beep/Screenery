package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.data.model.PastelTheme

// CompositionLocal to track if Compact UI mode is active
val LocalCompactMode = compositionLocalOf { false }

@Immutable
data class ExpressiveDimensions(
    val isCompact: Boolean,
    val screenPadding: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val sectionSpacing: Dp,
    val heroHeight: Dp,
    val iconSizeLarge: Dp,
    val iconSizeMedium: Dp,
    val iconSizeSmall: Dp,
    val buttonHeight: Dp,
    val chipHeight: Dp,
    val cornerRadiusLarge: Dp,
    val cornerRadiusMedium: Dp,
    val cornerRadiusSmall: Dp
)

val LocalExpressiveDimens = compositionLocalOf {
    ExpressiveDimensions(
        isCompact = false,
        screenPadding = 16.dp,
        cardPadding = 16.dp,
        itemSpacing = 12.dp,
        sectionSpacing = 20.dp,
        heroHeight = 220.dp,
        iconSizeLarge = 48.dp,
        iconSizeMedium = 24.dp,
        iconSizeSmall = 18.dp,
        buttonHeight = 56.dp,
        chipHeight = 36.dp,
        cornerRadiusLarge = 28.dp,
        cornerRadiusMedium = 20.dp,
        cornerRadiusSmall = 12.dp
    )
}

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
        tertiaryContainer = Color(0xFFF3E8FF),
        onTertiaryContainer = Color(0xFF6B21A8),
        background = bg,
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF64748B),
        surfaceContainer = Color(0xFFFFFFFF),
        surfaceContainerHigh = Color(0xFFF8FAFC),
        surfaceContainerHighest = Color(0xFFF1F5F9),
        surfaceContainerLow = Color(0xFFFDFDFE),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        outline = Color(0xFFE2E8F0),
        outlineVariant = Color(0xFFCBD5E1),
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
        tertiaryContainer = Color(0xFF3B0764),
        onTertiaryContainer = Color(0xFFF3E8FF),
        background = Color(0xFF0A0E1A),
        onBackground = Color(0xFFF8FAFC),
        surface = Color(0xFF121829),
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFF1E293B),
        onSurfaceVariant = Color(0xFF94A3B8),
        surfaceContainer = Color(0xFF141C30),
        surfaceContainerHigh = Color(0xFF1B243B),
        surfaceContainerHighest = Color(0xFF24304D),
        surfaceContainerLow = Color(0xFF0E1424),
        surfaceContainerLowest = Color(0xFF080C17),
        outline = Color(0xFF334155),
        outlineVariant = Color(0xFF475569),
        error = ScreeneryRecordRed,
        onError = Color.White
    )
}

// Material You Expressive Shapes
val ScreeneryShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

val ExpressivePillShape = CircleShape

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    pastelTheme: PastelTheme = PastelTheme.LAVENDER,
    isCompactMode: Boolean = false,
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

    val dimensions = remember(isCompactMode) {
        if (isCompactMode) {
            ExpressiveDimensions(
                isCompact = true,
                screenPadding = 12.dp,
                cardPadding = 12.dp,
                itemSpacing = 8.dp,
                sectionSpacing = 14.dp,
                heroHeight = 175.dp,
                iconSizeLarge = 40.dp,
                iconSizeMedium = 20.dp,
                iconSizeSmall = 16.dp,
                buttonHeight = 48.dp,
                chipHeight = 30.dp,
                cornerRadiusLarge = 22.dp,
                cornerRadiusMedium = 16.dp,
                cornerRadiusSmall = 10.dp
            )
        } else {
            ExpressiveDimensions(
                isCompact = false,
                screenPadding = 16.dp,
                cardPadding = 16.dp,
                itemSpacing = 12.dp,
                sectionSpacing = 20.dp,
                heroHeight = 220.dp,
                iconSizeLarge = 48.dp,
                iconSizeMedium = 24.dp,
                iconSizeSmall = 18.dp,
                buttonHeight = 56.dp,
                chipHeight = 36.dp,
                cornerRadiusLarge = 28.dp,
                cornerRadiusMedium = 20.dp,
                cornerRadiusSmall = 14.dp
            )
        }
    }

    CompositionLocalProvider(
        LocalCompactMode provides isCompactMode,
        LocalExpressiveDimens provides dimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ExpressiveTypography,
            shapes = ScreeneryShapes,
            content = content
        )
    }
}
