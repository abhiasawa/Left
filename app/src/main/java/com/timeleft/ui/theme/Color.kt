package com.timeleft.ui.theme

import androidx.compose.ui.graphics.Color

// ── Dark theme palette (default) ─────────────────────────────────────
val Black = Color(0xFF000000)
val DarkSurface = Color(0xFF0A0A0A)
val DarkSurfaceVariant = Color(0xFF111111)
val DotElapsedDark = Color(0xFF333333)
val DotRemainingDark = Color(0xFFFFFFFF)
val AccentRed = Color(0xFFFF3B30)
val AccentBlue = Color(0xFF007AFF)
val AccentGreen = Color(0xFF34C759)
val AccentOrange = Color(0xFFFF9500)
val AccentPurple = Color(0xFFAF52DE)
val AccentPink = Color(0xFFFF2D55)
val AccentYellow = Color(0xFFFFCC00)
val AccentTeal = Color(0xFF5AC8FA)

// ── Light theme palette ───────────────────────────────────────────────
val White = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF5F5F5)
val LightSurfaceVariant = Color(0xFFE8E8E8)
val DotElapsedLight = Color(0xFFD0D0D0)
val DotRemainingLight = Color(0xFF1A1A1A)

// ── Text colors ──────────────────────────────────────────────────────
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF8E8E93)
val TextPrimaryLight = Color(0xFF000000)
val TextSecondaryLight = Color(0xFF6C6C70)

// ── Navigation bar ───────────────────────────────────────────────────
val NavBarDark = Color(0xFF1C1C1E)
val NavBarLight = Color(0xFFF2F2F7)

// ── Preset swatches shown in the Settings color picker ───────────────
/** Colors available for the "remaining" dots. */
val PresetColors = listOf(
    Color(0xFFFFFFFF), // White
    Color(0xFFFF3B30), // Red
    Color(0xFFFF9500), // Orange
    Color(0xFFFFCC00), // Yellow
    Color(0xFF34C759), // Green
    Color(0xFF5AC8FA), // Teal
    Color(0xFF007AFF), // Blue
    Color(0xFFAF52DE), // Purple
    Color(0xFFFF2D55), // Pink
    Color(0xFF8E8E93), // Gray
)

/** Colors available for the "elapsed" dots (intentionally darker/muted). */
val PresetElapsedColors = listOf(
    Color(0xFF333333), // Dark gray (default)
    Color(0xFF4A1A1A), // Dark red
    Color(0xFF4A3A1A), // Dark orange
    Color(0xFF1A3A1A), // Dark green
    Color(0xFF1A2A3A), // Dark blue
    Color(0xFF2A1A3A), // Dark purple
)
