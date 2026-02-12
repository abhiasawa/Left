package com.timeleft.domain.usecases

import com.timeleft.util.TimeCalculations
import java.time.LocalDate

/**
 * Result of a life expectancy calculation.
 *
 * @property yearsLived       How many full years the user has been alive.
 * @property expectedLifespan Estimated total years (country/gender-based or manual).
 * @property yearsRemaining   Years left to reach the expected lifespan.
 * @property progressPercent  0–100 value for the life progress bar.
 */
data class LifeExpectancyResult(
    val yearsLived: Int,
    val expectedLifespan: Int,
    val yearsRemaining: Int,
    val progressPercent: Float
)

/**
 * Computes life progress from birth date and demographic data.
 *
 * If no [manualLifespan] is provided, falls back to a statistical
 * estimate based on [gender] and [country].
 */
class CalculateLifeExpectancyUseCase {

    operator fun invoke(
        birthDate: LocalDate,
        gender: String,
        country: String,
        manualLifespan: Int? = null
    ): LifeExpectancyResult {
        val expectedLifespan = manualLifespan
            ?: TimeCalculations.estimateLifeExpectancy(gender, country)
        val yearsLived = TimeCalculations.lifeYearsElapsed(birthDate)
        val yearsRemaining = TimeCalculations.lifeYearsRemaining(birthDate, expectedLifespan)
        val progress = TimeCalculations.lifeProgress(birthDate, expectedLifespan)

        return LifeExpectancyResult(
            yearsLived = yearsLived,
            expectedLifespan = expectedLifespan,
            yearsRemaining = yearsRemaining,
            progressPercent = progress * 100f
        )
    }
}
