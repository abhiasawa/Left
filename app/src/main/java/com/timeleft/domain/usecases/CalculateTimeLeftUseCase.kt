package com.timeleft.domain.usecases

import com.timeleft.domain.models.TimeUnit
import com.timeleft.util.TimeCalculations

data class TimeLeftResult(
    val total: Int,
    val elapsed: Int,
    val remaining: Int,
    val label: String,
    val unitLabel: String
)

class CalculateTimeLeftUseCase {

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
