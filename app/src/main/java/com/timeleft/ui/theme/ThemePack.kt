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

fun appPalette(themePack: ThemePack, darkTheme: Boolean): AppPalette {
    return when (themePack) {
        ThemePack.NOIR -> {
            if (darkTheme) {
                AppPalette(
                    background = Color(0xFF08090B),
                    surface = Color(0xFF101318),
                    surfaceVariant = Color(0xFF171C23),
                    border = Color(0xFF2A3039),
                    textPrimary = Color(0xFFF5F7FA),
                    textSecondary = Color(0xFFA6B0BD),
                    accent = Color(0xFF9BE8FF),
                    ambientStart = Color(0xFF0A111A),
                    ambientMiddle = Color(0xFF111728),
                    ambientEnd = Color(0xFF0E0C1B)
                )
            } else {
                AppPalette(
                    background = Color(0xFFF6F8FB),
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFE7ECF2),
                    border = Color(0xFFD3DAE4),
                    textPrimary = Color(0xFF0E1116),
                    textSecondary = Color(0xFF556272),
                    accent = Color(0xFF005A86),
                    ambientStart = Color(0xFFEAF2FA),
                    ambientMiddle = Color(0xFFF4F7FB),
                    ambientEnd = Color(0xFFECECF8)
                )
            }
        }
        ThemePack.SOLAR -> {
            if (darkTheme) {
                AppPalette(
                    background = Color(0xFF130E08),
                    surface = Color(0xFF1C130B),
                    surfaceVariant = Color(0xFF2B1B10),
                    border = Color(0xFF443024),
                    textPrimary = Color(0xFFFFF4E6),
                    textSecondary = Color(0xFFD8BCA1),
                    accent = Color(0xFFFFB266),
                    ambientStart = Color(0xFF2A1707),
                    ambientMiddle = Color(0xFF3A1D0A),
                    ambientEnd = Color(0xFF5A2A10)
                )
            } else {
                AppPalette(
                    background = Color(0xFFFFF8F0),
                    surface = Color(0xFFFFFCF8),
                    surfaceVariant = Color(0xFFFBE7D0),
                    border = Color(0xFFE8C9A5),
                    textPrimary = Color(0xFF3E220C),
                    textSecondary = Color(0xFF7C5433),
                    accent = Color(0xFFB75B06),
                    ambientStart = Color(0xFFFFEBCF),
                    ambientMiddle = Color(0xFFFFF3E3),
                    ambientEnd = Color(0xFFFFDCAE)
                )
            }
        }
        ThemePack.OCEANIC -> {
            if (darkTheme) {
                AppPalette(
                    background = Color(0xFF041118),
                    surface = Color(0xFF0A1D27),
                    surfaceVariant = Color(0xFF102A37),
                    border = Color(0xFF22404F),
                    textPrimary = Color(0xFFEAF8FF),
                    textSecondary = Color(0xFFA6C5D3),
                    accent = Color(0xFF60D8FF),
                    ambientStart = Color(0xFF052634),
                    ambientMiddle = Color(0xFF073247),
                    ambientEnd = Color(0xFF0A1D32)
                )
            } else {
                AppPalette(
                    background = Color(0xFFF2FBFF),
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFDDF0F8),
                    border = Color(0xFFC3E1EE),
                    textPrimary = Color(0xFF06212F),
                    textSecondary = Color(0xFF3F6678),
                    accent = Color(0xFF00739E),
                    ambientStart = Color(0xFFDDF6FF),
                    ambientMiddle = Color(0xFFF1FBFF),
                    ambientEnd = Color(0xFFCBE8F4)
                )
            }
        }
        ThemePack.PAPER -> {
            if (darkTheme) {
                AppPalette(
                    background = Color(0xFF12110F),
                    surface = Color(0xFF1A1815),
                    surfaceVariant = Color(0xFF27231E),
                    border = Color(0xFF3A342D),
                    textPrimary = Color(0xFFF6F0E6),
                    textSecondary = Color(0xFFB7AB9B),
                    accent = Color(0xFFEFC16C),
                    ambientStart = Color(0xFF2A241A),
                    ambientMiddle = Color(0xFF1E1C16),
                    ambientEnd = Color(0xFF161412)
                )
            } else {
                AppPalette(
                    background = Color(0xFFF9F5EE),
                    surface = Color(0xFFFFFCF7),
                    surfaceVariant = Color(0xFFF1E8DA),
                    border = Color(0xFFE1D3BF),
                    textPrimary = Color(0xFF261F15),
                    textSecondary = Color(0xFF71614E),
                    accent = Color(0xFFA36E22),
                    ambientStart = Color(0xFFF7EEDB),
                    ambientMiddle = Color(0xFFFFFBF2),
                    ambientEnd = Color(0xFFE9DBC4)
                )
            }
        }
    }
}

fun themeRemainingColorDefaults(themePack: ThemePack): List<Color> {
    return when (themePack) {
        ThemePack.NOIR -> listOf(
            Color(0xFFFFFFFF), Color(0xFFB7F7FF), Color(0xFF9BE8FF), Color(0xFF9DABFF),
            Color(0xFFF2B3FF), Color(0xFFADEFB5), Color(0xFFFFC8A2), Color(0xFFFF9EA8)
        )
        ThemePack.SOLAR -> listOf(
            Color(0xFFFFE6BF), Color(0xFFFFB266), Color(0xFFFF875F), Color(0xFFE65F3D),
            Color(0xFFFFD46B), Color(0xFFFFF3BE), Color(0xFF7A4A21), Color(0xFFEDC27B)
        )
        ThemePack.OCEANIC -> listOf(
            Color(0xFFEAF8FF), Color(0xFFADEFFF), Color(0xFF60D8FF), Color(0xFF68FFEE),
            Color(0xFF8BD3FF), Color(0xFF9DB7FF), Color(0xFF92FFD4), Color(0xFFD1ECFF)
        )
        ThemePack.PAPER -> listOf(
            Color(0xFFF3E8D6), Color(0xFFDAB57E), Color(0xFFB88A55), Color(0xFFA36E22),
            Color(0xFFCFAF82), Color(0xFFE8D5B4), Color(0xFF8E6B42), Color(0xFF5A432A)
        )
    }
}

fun themeElapsedColorDefaults(themePack: ThemePack): List<Color> {
    return when (themePack) {
        ThemePack.NOIR -> listOf(
            Color(0xFF2A313B), Color(0xFF2A4254), Color(0xFF3D2F50),
            Color(0xFF274443), Color(0xFF4A3A2D), Color(0xFF433247)
        )
        ThemePack.SOLAR -> listOf(
            Color(0xFF4B3020), Color(0xFF5B2A1F), Color(0xFF5F3F25),
            Color(0xFF634C1E), Color(0xFF4D2F13), Color(0xFF5D4530)
        )
        ThemePack.OCEANIC -> listOf(
            Color(0xFF1B3542), Color(0xFF1E3F50), Color(0xFF244A56),
            Color(0xFF27423E), Color(0xFF2D3A59), Color(0xFF2A485C)
        )
        ThemePack.PAPER -> listOf(
            Color(0xFF594A37), Color(0xFF6A5034), Color(0xFF7A5D41),
            Color(0xFF504536), Color(0xFF65553F), Color(0xFF4F3F2D)
        )
    }
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
