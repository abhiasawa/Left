package com.timeleft.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Color schemes ────────────────────────────────────────────────────
// Each scheme maps the app's custom palette to Material 3 semantic slots.

private val DarkColorScheme = darkColorScheme(
    primary = DotRemainingDark,
    onPrimary = Black,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DotRemainingDark,
    secondary = AccentBlue,
    onSecondary = Black,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = AccentBlue,
    tertiary = AccentPurple,
    onTertiary = Black,
    background = Black,
    onBackground = DotRemainingDark,
    surface = Black,
    onSurface = DotRemainingDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = DotElapsedDark,
    outlineVariant = Color(0xFF2C2C2E),
)

private val LightColorScheme = lightColorScheme(
    primary = DotRemainingLight,
    onPrimary = White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = DotRemainingLight,
    secondary = AccentBlue,
    onSecondary = White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = AccentBlue,
    tertiary = AccentPurple,
    onTertiary = White,
    background = LightSurface,
    onBackground = DotRemainingLight,
    surface = White,
    onSurface = DotRemainingLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = DotElapsedLight,
    outlineVariant = Color(0xFFD1D1D6),
)

/**
 * Top-level theme wrapper for the entire app.
 *
 * Handles:
 * - Dark / light color scheme selection.
 * - Transparent status & navigation bars with matching icon tint.
 * - Custom [Typography] powered by the Inter font family.
 */
@Composable
fun TimeLeftTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        // Synchronize system-bar appearance with the current theme
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
