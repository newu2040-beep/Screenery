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
        onPrimary = if (pastelTheme == PastelTheme.SWISS_MINIMAL) Color.White else Color.White,
        primaryContainer = container,
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = container,
        onSecondaryContainer = secondary,
        tertiary = secondary,
        onTertiary = Color.White,
        tertiaryContainer = container,
        onTertiaryContainer = primary,
        background = bg,
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = if (pastelTheme == PastelTheme.SWISS_MINIMAL) Color(0xFFF4F4F5) else Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF64748B),
        surfaceContainer = Color(0xFFFFFFFF),
        surfaceContainerHigh = Color(0xFFF8FAFC),
        surfaceContainerHighest = Color(0xFFF1F5F9),
        surfaceContainerLow = Color(0xFFFDFDFE),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        outline = if (pastelTheme == PastelTheme.SWISS_MINIMAL) Color(0xFFE4E4E7) else Color(0xFFE2E8F0),
        outlineVariant = Color(0xFFCBD5E1),
        error = ScreeneryRecordRed,
        onError = Color.White
    )
}

fun createPastelDarkColorScheme(pastelTheme: PastelTheme): ColorScheme {
    val primary = Color(pastelTheme.darkPrimaryColor)
    val secondary = Color(pastelTheme.secondaryColor)
    val container = Color(pastelTheme.darkContainerColor)

    val (bg, surface, surfaceVar, outlineColor) = when (pastelTheme) {
        PastelTheme.MIDNIGHT_OLED -> Quad(
            Color(0xFF000000), // True pitch black for OLED displays
            Color(0xFF09090B),
            Color(0xFF18181B),
            Color(0xFF27272A)
        )
        PastelTheme.CYBER_NEON -> Quad(
            Color(0xFF080512),
            Color(0xFF120C26),
            Color(0xFF1E1438),
            Color(0xFF3B1E6D)
        )
        PastelTheme.EMERALD_MATRIX -> Quad(
            Color(0xFF030D08),
            Color(0xFF081810),
            Color(0xFF0D251A),
            Color(0xFF123B29)
        )
        PastelTheme.SWISS_MINIMAL -> Quad(
            Color(0xFF09090B),
            Color(0xFF141416),
            Color(0xFF202024),
            Color(0xFF2E2E33)
        )
        PastelTheme.NORDIC_ICE -> Quad(
            Color(0xFF041017),
            Color(0xFF081C26),
            Color(0xFF0E2A38),
            Color(0xFF173E52)
        )
        PastelTheme.SUNSET_AURORA -> Quad(
            Color(0xFF120603),
            Color(0xFF1F0C07),
            Color(0xFF2E130B),
            Color(0xFF4D1E12)
        )
        else -> Quad(
            Color(0xFF0A0E1A),
            Color(0xFF121829),
            Color(0xFF1E293B),
            Color(0xFF334155)
        )
    }

    return darkColorScheme(
        primary = primary,
        onPrimary = if (pastelTheme == PastelTheme.SWISS_MINIMAL) Color.Black else Color.Black,
        primaryContainer = container,
        onPrimaryContainer = Color(0xFFF8FAFC),
        secondary = secondary,
        onSecondary = Color.Black,
        secondaryContainer = container,
        onSecondaryContainer = Color(0xFFF1F5F9),
        tertiary = secondary,
        onTertiary = Color.Black,
        tertiaryContainer = container,
        onTertiaryContainer = Color(0xFFF3E8FF),
        background = bg,
        onBackground = Color(0xFFF8FAFC),
        surface = surface,
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = surfaceVar,
        onSurfaceVariant = Color(0xFF94A3B8),
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceVar,
        surfaceContainerHighest = outlineColor,
        surfaceContainerLow = bg,
        surfaceContainerLowest = bg,
        outline = outlineColor,
        outlineVariant = Color(0xFF475569),
        error = ScreeneryRecordRed,
        onError = Color.White
    )
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

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
