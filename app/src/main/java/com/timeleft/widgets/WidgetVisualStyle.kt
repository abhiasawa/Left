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

data class WidgetCardColors(
    val start: Int,
    val end: Int,
    val glow: Int,
    val border: Int
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

fun WidgetVisualStyle.cardColors(
    hueShift: Float,
    saturationMul: Float = 1f,
    valueMul: Float = 1f,
    glowAlphaBoost: Float = 1f
): WidgetCardColors {
    return WidgetCardColors(
        start = shiftColor(cardStart, hueShift, saturationMul, valueMul),
        end = shiftColor(cardEnd, hueShift * 0.7f, saturationMul * 0.95f, valueMul * 0.92f),
        glow = shiftColor(cardGlow, hueShift, saturationMul * 1.1f, valueMul * 1.08f, glowAlphaBoost),
        border = shiftColor(cardBorder, hueShift * 0.5f, saturationMul, valueMul)
    )
}

private fun shiftColor(
    color: Int,
    hueShift: Float,
    saturationMul: Float,
    valueMul: Float,
    alphaMul: Float = 1f
): Int {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color, hsv)
    hsv[0] = (hsv[0] + hueShift + 360f) % 360f
    hsv[1] = (hsv[1] * saturationMul).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * valueMul).coerceIn(0f, 1f)
    val alpha = (android.graphics.Color.alpha(color) * alphaMul).toInt().coerceIn(0, 255)
    return android.graphics.Color.HSVToColor(alpha, hsv)
}

private fun parseColorInt(hex: String, fallback: Color): Int {
    return try {
        android.graphics.Color.parseColor(hex)
    } catch (_: Exception) {
        fallback.toArgb()
    }
}
