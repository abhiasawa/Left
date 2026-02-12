package com.timeleft.domain.usecases

import com.timeleft.util.TimeCalculations
import java.time.LocalDate

data class LifeExpectancyResult(
    val yearsLived: Int,
    val expectedLifespan: Int,
    val yearsRemaining: Int,
    val progressPercent: Float
)

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
