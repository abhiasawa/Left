package com.timeleft.domain.models

import java.time.LocalDate

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
    val totalDays: Int
        get() = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt()

    val elapsedDays: Int
        get() {
            val now = LocalDate.now()
            return when {
                now.isBefore(startDate) -> 0
                now.isAfter(endDate) -> totalDays
                else -> java.time.temporal.ChronoUnit.DAYS.between(startDate, now).toInt()
            }
        }

    val remainingDays: Int
        get() = totalDays - elapsedDays

    val progress: Float
        get() = if (totalDays > 0) elapsedDays.toFloat() / totalDays else 0f

    val isActive: Boolean
        get() {
            val now = LocalDate.now()
            return !now.isBefore(startDate) && !now.isAfter(endDate)
        }

    val isFuture: Boolean
        get() = LocalDate.now().isBefore(startDate)

    val isPast: Boolean
        get() = LocalDate.now().isAfter(endDate)
}
