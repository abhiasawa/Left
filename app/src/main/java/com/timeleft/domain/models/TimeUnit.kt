package com.timeleft.domain.models

/**
 * Represents the available time granularity levels the user can view.
 *
 * Each unit maps to a different progress visualization on the main screen,
 * from fine-grained (HOUR) to full lifespan (LIFE).
 *
 * @property displayName User-facing label shown in the [TimeSelector] pill bar.
 */
enum class TimeUnit(val displayName: String) {
    YEAR("Year"),
    MONTH("Month"),
    WEEK("Week"),
    DAY("Day"),
    HOUR("Hour"),
    LIFE("Life");

    companion object {
        /** Parses a persisted string back to a [TimeUnit], defaulting to [YEAR]. */
        fun fromString(value: String): TimeUnit {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: YEAR
        }
    }
}
