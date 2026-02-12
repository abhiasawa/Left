package com.timeleft.domain.models

import java.time.LocalDate

/**
 * User-created countdown/count-up event displayed on the "Ahead" screen.
 *
 * Computed properties ([elapsedDays], [progress], etc.) are derived from the
 * current system date so the UI always reflects real-time status.
 *
 * @property id         Auto-generated Room primary key.
 * @property name       User-given label (e.g. "Vacation", "Birthday").
 * @property startDate  First day of the tracked period.
 * @property endDate    Last day of the tracked period.
 * @property colorHex   Hex color string for the progress indicator.
 * @property symbolType Shape used when rendering the dot grid for this event.
 * @property isCountUp  If true, counts days *since* start instead of days *until* end.
 * @property createdAt  Epoch millis when the event was created (used for sort order).
 */
data class CustomDate(
    val id: Int = 0,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val colorHex: String = "#FFFFFF",
    val symbolType: SymbolType = SymbolType.DOT,
    val isCountUp: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Total span of the event in days. */
    val totalDays: Int
        get() = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt()

    /** Days elapsed so far — clamped to [0, totalDays]. */
    val elapsedDays: Int
        get() {
            val now = LocalDate.now()
            return when {
                now.isBefore(startDate) -> 0
                now.isAfter(endDate) -> totalDays
                else -> java.time.temporal.ChronoUnit.DAYS.between(startDate, now).toInt()
            }
        }

    /** Days remaining until [endDate]. */
    val remainingDays: Int
        get() = totalDays - elapsedDays

    /** Completion ratio in the range [0f, 1f]. */
    val progress: Float
        get() = if (totalDays > 0) elapsedDays.toFloat() / totalDays else 0f

    /** True when today falls within [startDate]..[endDate]. */
    val isActive: Boolean
        get() {
            val now = LocalDate.now()
            return !now.isBefore(startDate) && !now.isAfter(endDate)
        }

    /** True when the event hasn't started yet. */
    val isFuture: Boolean
        get() = LocalDate.now().isBefore(startDate)

    /** True when the event has already ended. */
    val isPast: Boolean
        get() = LocalDate.now().isAfter(endDate)
}
