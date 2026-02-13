package com.timeleft.widgets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.ui.theme.ThemePack
import com.timeleft.ui.theme.appPalette

data class WidgetVisualStyle(
    val cardStart: Int,
    val cardEnd: Int,
    val cardGlow: Int,
    val cardBorder: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val elapsedColor: Int,
    val remainingColor: Int,
    val currentColor: Int
)

fun widgetVisualStyle(preferences: UserPreferencesData): WidgetVisualStyle {
    val themePack = ThemePack.fromString(preferences.themePack)
    val palette = appPalette(themePack, preferences.darkMode)
    return WidgetVisualStyle(
        cardStart = palette.surface.toArgb(),
        cardEnd = palette.background.toArgb(),
        cardGlow = palette.accent.copy(alpha = if (preferences.darkMode) 0.26f else 0.16f).toArgb(),
        cardBorder = palette.border.copy(alpha = if (preferences.darkMode) 0.62f else 0.82f).toArgb(),
        textPrimary = palette.textPrimary.toArgb(),
        textSecondary = palette.textSecondary.toArgb(),
        elapsedColor = parseColorInt(preferences.elapsedColor, fallback = palette.border),
        remainingColor = parseColorInt(preferences.remainingColor, fallback = palette.textPrimary),
        currentColor = parseColorInt(preferences.currentIndicatorColor, fallback = palette.accent)
    )
}

private fun parseColorInt(hex: String, fallback: Color): Int {
    return try {
        android.graphics.Color.parseColor(hex)
    } catch (_: Exception) {
        fallback.toArgb()
    }
}
