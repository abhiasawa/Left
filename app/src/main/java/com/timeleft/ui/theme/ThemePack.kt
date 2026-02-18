package com.timeleft.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.timeleft.domain.models.TimeUnit

/**
 * Curated visual directions for the app. Persisted as uppercase string in DataStore.
 */
enum class ThemePack(val storageKey: String, val title: String) {
    NOIR("NOIR", "Noir"),
    SOLAR("SOLAR", "Solar"),
    OCEANIC("OCEANIC", "Oceanic"),
    PAPER("PAPER", "Paper");

    companion object {
        fun fromString(value: String): ThemePack {
            return entries.firstOrNull { it.storageKey.equals(value, ignoreCase = true) } ?: NOIR
        }
    }
}

data class AppPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val ambientStart: Color,
    val ambientMiddle: Color,
    val ambientEnd: Color
)

/**
 * Shared elapsed-dot color tuned for readability against both app backgrounds.
 */
@Suppress("UNUSED_PARAMETER")
fun elapsedDotColor(themePack: ThemePack, darkTheme: Boolean): Color {
    return if (darkTheme) Color(0xFF5A5A5A) else Color(0xFF8F8779)
}

@Suppress("UNUSED_PARAMETER")
fun appPalette(themePack: ThemePack, darkTheme: Boolean): AppPalette {
    // Enforce a strict monochrome visual language independent of theme pack selection.
    if (darkTheme) {
        return AppPalette(
            background = Color(0xFF050505),
            surface = Color(0xFF0C0C0C),
            surfaceVariant = Color(0xFF151515),
            border = Color(0xFF343434),
            textPrimary = Color(0xFFEDE9DF),
            textSecondary = Color(0xFF9A958C),
            accent = Color(0xFFEDE9DF),
            ambientStart = Color(0xFF0A0A0A),
            ambientMiddle = Color(0xFF111111),
            ambientEnd = Color(0xFF1A1A1A)
        )
    }
    return AppPalette(
        background = Color(0xFFF4F1EA),
        surface = Color(0xFFFBF8F1),
        surfaceVariant = Color(0xFFE8E2D6),
        border = Color(0xFFB8B1A5),
        textPrimary = Color(0xFF101010),
        textSecondary = Color(0xFF5E5A52),
        accent = Color(0xFF101010),
        ambientStart = Color(0xFFF1EBDF),
        ambientMiddle = Color(0xFFF8F5EE),
        ambientEnd = Color(0xFFE2DBCF)
    )
}

@Suppress("UNUSED_PARAMETER")
fun themeRemainingColorDefaults(themePack: ThemePack): List<Color> {
    return listOf(Color(0xFFEDE9DF))
}

@Suppress("UNUSED_PARAMETER")
fun themeElapsedColorDefaults(themePack: ThemePack): List<Color> {
    return listOf(Color(0xFF5A5A5A), Color(0xFF8F8779))
}

fun ambientBrush(themePack: ThemePack, darkTheme: Boolean, unit: TimeUnit): Brush {
    val palette = appPalette(themePack, darkTheme)
    val intensity = when (unit) {
        TimeUnit.LIFE -> 1f
        TimeUnit.YEAR -> 0.9f
        TimeUnit.MONTH -> 0.8f
        TimeUnit.WEEK -> 0.72f
        TimeUnit.DAY -> 0.65f
        TimeUnit.HOUR -> 0.62f
    }
    return Brush.radialGradient(
        colors = listOf(
            palette.ambientMiddle.copy(alpha = 0.96f),
            palette.ambientStart.copy(alpha = 0.75f * intensity),
            palette.ambientEnd.copy(alpha = 0.52f * intensity),
            palette.background
        )
    )
}
