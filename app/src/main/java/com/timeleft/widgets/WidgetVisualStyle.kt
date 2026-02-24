package com.timeleft.widgets

import com.timeleft.data.preferences.UserPreferencesData

private const val WIDGET_CARD_BASE = 0xFF09090B.toInt()
private const val WIDGET_CARD_BORDER = 0xFF23232A.toInt()
private const val WIDGET_TEXT_PRIMARY = 0xFFF5F5F5.toInt()
private const val WIDGET_TEXT_SECONDARY = 0xFFA1A1AA.toInt()
private const val WIDGET_REMAINING = 0xFFE4E4E7.toInt()
private const val WIDGET_ELAPSED = 0xFF5A5A5A.toInt()

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
    val elapsed = parseUserColor(preferences.elapsedColor, WIDGET_ELAPSED)
    val remaining = parseUserColor(preferences.remainingColor, WIDGET_REMAINING)
    val current = parseUserColor(preferences.currentIndicatorColor, remaining)
    return WidgetVisualStyle(
        cardStart = WIDGET_CARD_BASE,
        cardEnd = WIDGET_CARD_BASE,
        cardGlow = 0x00000000,
        cardBorder = WIDGET_CARD_BORDER,
        textPrimary = WIDGET_TEXT_PRIMARY,
        textSecondary = WIDGET_TEXT_SECONDARY,
        elapsedColor = elapsed,
        remainingColor = remaining,
        currentColor = current
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

private fun parseUserColor(value: String, fallback: Int): Int {
    return try {
        android.graphics.Color.parseColor(value)
    } catch (_: IllegalArgumentException) {
        fallback
    }
}
