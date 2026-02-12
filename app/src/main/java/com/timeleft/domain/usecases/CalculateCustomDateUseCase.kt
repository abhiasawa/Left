package com.timeleft.domain.usecases

import com.timeleft.domain.models.CustomDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class CustomDateResult(
    val name: String,
    val totalDays: Int,
    val elapsedDays: Int,
    val remainingDays: Int,
    val progress: Float,
    val displayText: String
)

class CalculateCustomDateUseCase {

    operator fun invoke(customDate: CustomDate): CustomDateResult {
        val now = LocalDate.now()

        return when {
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
