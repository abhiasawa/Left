package com.timeleft.domain.models

enum class TimeUnit(val displayName: String) {
    YEAR("Year"),
    MONTH("Month"),
    WEEK("Week"),
    DAY("Day"),
    HOUR("Hour"),
    LIFE("Life");

    companion object {
        fun fromString(value: String): TimeUnit {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: YEAR
        }
    }
}
