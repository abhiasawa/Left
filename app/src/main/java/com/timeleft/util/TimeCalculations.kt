package com.timeleft.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.temporal.ChronoUnit

object TimeCalculations {

    // --- Year ---

    fun totalDaysInYear(year: Int = Year.now().value): Int {
        return if (Year.of(year).isLeap) 366 else 365
    }

    fun daysElapsedInYear(): Int {
        return LocalDate.now().dayOfYear - 1
    }

    fun daysLeftInYear(): Int {
        return totalDaysInYear() - daysElapsedInYear() - 1
    }

    fun yearLabel(): String {
        return Year.now().value.toString()
    }

    // --- Month ---

    fun totalDaysInMonth(): Int {
        val now = LocalDate.now()
        return now.lengthOfMonth()
    }

    fun daysElapsedInMonth(): Int {
        return LocalDate.now().dayOfMonth - 1
    }

    fun daysLeftInMonth(): Int {
        return totalDaysInMonth() - daysElapsedInMonth() - 1
    }

    fun monthLabel(): String {
        val now = LocalDate.now()
        return now.month.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    // --- Week ---

    fun totalDaysInWeek(): Int = 7

    fun daysElapsedInWeek(): Int {
        val now = LocalDate.now()
        return now.dayOfWeek.value - DayOfWeek.MONDAY.value
    }

    fun daysLeftInWeek(): Int {
        return totalDaysInWeek() - daysElapsedInWeek() - 1
    }

    fun weekLabel(): String {
        val now = LocalDate.now()
        return "Week ${(now.dayOfYear - 1) / 7 + 1}"
    }

    // --- Day ---

    fun totalHoursInDay(activeStart: Int = 0, activeEnd: Int = 24): Int {
        return activeEnd - activeStart
    }

    fun hoursElapsedInDay(activeStart: Int = 0): Int {
        val now = LocalDateTime.now()
        val elapsed = now.hour - activeStart
        return elapsed.coerceIn(0, totalHoursInDay(activeStart))
    }

    fun hoursLeftInDay(activeStart: Int = 0, activeEnd: Int = 24): Int {
        val now = LocalDateTime.now()
        val remaining = activeEnd - now.hour - 1
        return remaining.coerceIn(0, totalHoursInDay(activeStart, activeEnd))
    }

    fun dayLabel(): String {
        val now = LocalDate.now()
        return now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    // --- Hour ---

    fun totalMinutesInHour(): Int = 60

    fun minutesElapsedInHour(): Int {
        return LocalDateTime.now().minute
    }

    fun minutesLeftInHour(): Int {
        return 60 - minutesElapsedInHour() - 1
    }

    fun hourLabel(): String {
        val now = LocalDateTime.now()
        val hour = now.hour
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$displayHour $amPm"
    }

    // --- Life ---

    fun lifeYearsElapsed(birthDate: LocalDate): Int {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now()).toInt()
    }

    fun lifeYearsRemaining(birthDate: LocalDate, expectedLifespan: Int): Int {
        val lived = lifeYearsElapsed(birthDate)
        return (expectedLifespan - lived).coerceAtLeast(0)
    }

    fun lifeProgress(birthDate: LocalDate, expectedLifespan: Int): Float {
        val lived = lifeYearsElapsed(birthDate)
        return if (expectedLifespan > 0) (lived.toFloat() / expectedLifespan).coerceIn(0f, 1f) else 0f
    }

    // --- Life Expectancy by Country/Gender ---

    fun estimateLifeExpectancy(gender: String, country: String): Int {
        val table = mapOf(
            "Japan" to Pair(81, 87),
            "Switzerland" to Pair(82, 85),
            "Australia" to Pair(81, 85),
            "Sweden" to Pair(81, 84),
            "Canada" to Pair(80, 84),
            "France" to Pair(79, 85),
            "Germany" to Pair(79, 83),
            "United Kingdom" to Pair(79, 83),
            "United States" to Pair(76, 81),
            "China" to Pair(75, 79),
            "Brazil" to Pair(72, 79),
            "Mexico" to Pair(72, 78),
            "India" to Pair(69, 72),
            "Russia" to Pair(68, 78),
            "Nigeria" to Pair(54, 56),
            "South Korea" to Pair(80, 86),
            "Italy" to Pair(81, 85),
            "Spain" to Pair(80, 86),
            "Netherlands" to Pair(80, 83),
            "Norway" to Pair(81, 84),
        )
        val default = Pair(75, 80) // male, female
        val (male, female) = table[country] ?: default
        return when (gender.lowercase()) {
            "male" -> male
            "female" -> female
            else -> (male + female) / 2
        }
    }

    val availableCountries: List<String> = listOf(
        "Australia", "Brazil", "Canada", "China", "France",
        "Germany", "India", "Italy", "Japan", "Mexico",
        "Netherlands", "Nigeria", "Norway", "Russia",
        "South Korea", "Spain", "Sweden", "Switzerland",
        "United Kingdom", "United States"
    )

    // --- Custom Date ---

    fun daysUntil(targetDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), targetDate)
    }

    fun daysSince(pastDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(pastDate, LocalDate.now())
    }
}
