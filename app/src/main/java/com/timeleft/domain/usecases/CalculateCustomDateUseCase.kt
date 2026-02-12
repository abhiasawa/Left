package com.timeleft.domain.usecases

import com.timeleft.domain.models.CustomDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Presentation-ready data for a single custom countdown/count-up event.
 *
 * @property displayText Contextual label shown on the card (e.g. "42 days left", "Completed").
 */
data class CustomDateResult(
    val name: String,
    val totalDays: Int,
    val elapsedDays: Int,
    val remainingDays: Int,
    val progress: Float,
    val displayText: String
)

/**
 * Resolves a [CustomDate] into a [CustomDateResult] depending on whether the
 * event is counting up, hasn't started yet, is active, or has already passed.
 */
class CalculateCustomDateUseCase {

    operator fun invoke(customDate: CustomDate): CustomDateResult {
        val now = LocalDate.now()

        return when {
            // Count-up: counts days *since* the start date
            customDate.isCountUp -> {
                val daysSince = ChronoUnit.DAYS.between(customDate.startDate, now).toInt()
                CustomDateResult(
                    name = customDate.name,
                    totalDays = daysSince,
                    elapsedDays = daysSince,
                    remainingDays = 0,
                    progress = 1f,
                    displayText = "${abs(daysSince)} days since"
                )
            }
            customDate.isFuture -> {
                val daysUntil = ChronoUnit.DAYS.between(now, customDate.startDate).toInt()
                CustomDateResult(
                    name = customDate.name,
                    totalDays = customDate.totalDays,
                    elapsedDays = 0,
                    remainingDays = daysUntil + customDate.totalDays,
                    progress = 0f,
                    displayText = "Starts in $daysUntil days"
                )
            }
            customDate.isPast -> {
                CustomDateResult(
                    name = customDate.name,
                    totalDays = customDate.totalDays,
                    elapsedDays = customDate.totalDays,
                    remainingDays = 0,
                    progress = 1f,
                    displayText = "Completed"
                )
            }
            else -> {
                CustomDateResult(
                    name = customDate.name,
                    totalDays = customDate.totalDays,
                    elapsedDays = customDate.elapsedDays,
                    remainingDays = customDate.remainingDays,
                    progress = customDate.progress,
                    displayText = "${customDate.remainingDays} days left"
                )
            }
        }
    }
}
