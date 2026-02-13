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

private fun darkScheme(themePack: ThemePack) = appPalette(themePack, darkTheme = true).let { palette ->
    darkColorScheme(
        primary = palette.accent,
        onPrimary = palette.background,
        primaryContainer = palette.surfaceVariant,
        onPrimaryContainer = palette.textPrimary,
        secondary = palette.textPrimary,
        onSecondary = palette.background,
        secondaryContainer = palette.surfaceVariant,
        onSecondaryContainer = palette.textPrimary,
        tertiary = palette.accent,
        onTertiary = palette.background,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceVariant,
        onSurfaceVariant = palette.textSecondary,
        outline = palette.border,
        outlineVariant = palette.border.copy(alpha = 0.6f),
    )
}

private fun lightScheme(themePack: ThemePack) = appPalette(themePack, darkTheme = false).let { palette ->
    lightColorScheme(
        primary = palette.accent,
        onPrimary = palette.surface,
        primaryContainer = palette.surfaceVariant,
        onPrimaryContainer = palette.textPrimary,
        secondary = palette.textPrimary,
        onSecondary = palette.surface,
        secondaryContainer = palette.surfaceVariant,
        onSecondaryContainer = palette.textPrimary,
        tertiary = palette.accent,
        onTertiary = palette.surface,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceVariant,
        onSurfaceVariant = palette.textSecondary,
        outline = palette.border,
        outlineVariant = palette.border.copy(alpha = 0.6f),
    )
}

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
    themePack: ThemePack = ThemePack.NOIR,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkScheme(themePack) else lightScheme(themePack)

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
