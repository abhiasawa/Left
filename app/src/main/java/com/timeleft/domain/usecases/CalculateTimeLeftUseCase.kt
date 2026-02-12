package com.timeleft.domain.usecases

import com.timeleft.domain.models.TimeUnit
import com.timeleft.util.TimeCalculations

/**
 * Holds the computed progress for a single time unit.
 *
 * @property total     Total units in the period (e.g. 365 days in a year).
 * @property elapsed   Units already passed.
 * @property remaining Units still to come.
 * @property label     Human-readable header (e.g. "2026", "February", "Week 7").
 * @property unitLabel Pluralized unit name for display (e.g. "days", "hours", "min").
 */
data class TimeLeftResult(
    val total: Int,
    val elapsed: Int,
    val remaining: Int,
    val label: String,
    val unitLabel: String
)

/**
 * Calculates how much time has elapsed / remains for a given [TimeUnit].
 *
 * Callable as a function: `CalculateTimeLeftUseCase()(TimeUnit.YEAR)`.
 */
class CalculateTimeLeftUseCase {

    /**
     * @param timeUnit         Granularity to calculate for.
     * @param activeHoursStart Start of the user's active window (DAY mode only).
     * @param activeHoursEnd   End of the user's active window (DAY mode only).
     */
    operator fun invoke(
        timeUnit: TimeUnit,
        activeHoursStart: Int = 0,
        activeHoursEnd: Int = 24
    ): TimeLeftResult {
        return when (timeUnit) {
            TimeUnit.YEAR -> TimeLeftResult(
                total = TimeCalculations.totalDaysInYear(),
                elapsed = TimeCalculations.daysElapsedInYear(),
                remaining = TimeCalculations.daysLeftInYear(),
                label = TimeCalculations.yearLabel(),
                unitLabel = "days"
            )
            TimeUnit.MONTH -> TimeLeftResult(
                total = TimeCalculations.totalDaysInMonth(),
                elapsed = TimeCalculations.daysElapsedInMonth(),
                remaining = TimeCalculations.daysLeftInMonth(),
                label = TimeCalculations.monthLabel(),
                unitLabel = "days"
            )
            TimeUnit.WEEK -> TimeLeftResult(
                total = TimeCalculations.totalDaysInWeek(),
                elapsed = TimeCalculations.daysElapsedInWeek(),
                remaining = TimeCalculations.daysLeftInWeek(),
                label = TimeCalculations.weekLabel(),
                unitLabel = "days"
            )
            TimeUnit.DAY -> TimeLeftResult(
                total = TimeCalculations.totalHoursInDay(activeHoursStart, activeHoursEnd),
                elapsed = TimeCalculations.hoursElapsedInDay(activeHoursStart),
                remaining = TimeCalculations.hoursLeftInDay(activeHoursStart, activeHoursEnd),
                label = TimeCalculations.dayLabel(),
                unitLabel = "hours"
            )
            TimeUnit.HOUR -> TimeLeftResult(
                total = TimeCalculations.totalMinutesInHour(),
                elapsed = TimeCalculations.minutesElapsedInHour(),
                remaining = TimeCalculations.minutesLeftInHour(),
                label = TimeCalculations.hourLabel(),
                unitLabel = "min"
            )
            TimeUnit.LIFE -> TimeLeftResult(
                total = 80,
                elapsed = 0,
                remaining = 80,
                label = "Life",
                unitLabel = "years"
            )
        }
    }
}
