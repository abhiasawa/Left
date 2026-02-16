package com.timeleft.widgets

fun darkDotWidgetStyle(): WidgetVisualStyle {
    return WidgetVisualStyle(
        cardStart = 0xFF060608.toInt(),
        cardEnd = 0xFF060608.toInt(),
        cardGlow = 0xFF0F172A.toInt(),
        cardBorder = 0xFF27272A.toInt(),
        textPrimary = 0xFFF5F5F5.toInt(),
        textSecondary = 0xFFA1A1AA.toInt(),
        elapsedColor = 0xFF3F3F46.toInt(),
        remainingColor = 0xFFFFFFFF.toInt(),
        currentColor = 0xFFFF3B30.toInt()
    )
}
