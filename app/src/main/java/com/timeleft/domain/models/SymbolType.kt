package com.timeleft.domain.models

enum class SymbolType(val displayName: String, val symbol: String) {
    DOT("Circle", "\u25CF"),
    STAR("Star", "\u2605"),
    HEART("Heart", "\u2665"),
    HEXAGON("Hexagon", "\u2B22"),
    SQUARE("Square", "\u25A0"),
    DIAMOND("Diamond", "\u25C6"),
    WORD("Number", "#");

    companion object {
        fun fromString(value: String): SymbolType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DOT
        }
    }
}
