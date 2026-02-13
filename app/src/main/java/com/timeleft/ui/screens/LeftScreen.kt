package com.timeleft.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.domain.models.SymbolType
import com.timeleft.domain.models.TimeUnit
import com.timeleft.ui.components.DayGrid
import com.timeleft.ui.components.DotGrid
import com.timeleft.ui.components.HourClock
import com.timeleft.ui.components.MonthCalendar
import com.timeleft.ui.components.TimeSelector
import com.timeleft.ui.components.WeekView
import com.timeleft.util.TimeCalculations
import java.time.LocalDate

/**
 * Main screen — the visualization IS the interface.
 *
 * Layout: gear icon top-right, TimeSelector glass strip, adaptive visualization
 * filling ~60-65% of height, whisper-quiet caption row at bottom.
 * Swipe horizontally to switch time units.
 */
@Composable
fun LeftScreen(
    preferences: UserPreferencesData,
    selectedUnit: TimeUnit,
    onUnitSelected: (TimeUnit) -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elapsedColor = parseColor(preferences.elapsedColor)
    val remainingColor = parseColor(preferences.remainingColor)
    val currentIndicatorColor = parseColor(preferences.currentIndicatorColor)
    val symbolType = SymbolType.fromString(preferences.symbolType)

    val hasLifeData = preferences.birthDate != null
    val showLifeOption = hasLifeData

    val availableUnits = remember(showLifeOption) {
        if (showLifeOption) TimeUnit.entries else TimeUnit.entries.filter { it != TimeUnit.LIFE }
    }

    val timeData = remember(selectedUnit, preferences) {
        getTimeData(selectedUnit, preferences)
    }

    // Swipe gesture state
    val haptic = LocalHapticFeedback.current
    var swipeDelta by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 100f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(selectedUnit, availableUnits) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val currentIndex = availableUnits.indexOf(selectedUnit)
                        if (swipeDelta < -swipeThreshold && currentIndex < availableUnits.lastIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUnitSelected(availableUnits[currentIndex + 1])
                        } else if (swipeDelta > swipeThreshold && currentIndex > 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUnitSelected(availableUnits[currentIndex - 1])
                        }
                        swipeDelta = 0f
                    },
                    onDragCancel = { swipeDelta = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeDelta += dragAmount
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top row: settings gear icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Time unit selector — glass strip
            TimeSelector(
                selectedUnit = selectedUnit,
                onUnitSelected = onUnitSelected,
                showLifeOption = showLifeOption
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Visualization — the hero. Adapts per time unit.
            AnimatedContent(
                targetState = Triple(timeData.total, timeData.elapsed, selectedUnit),
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(200))
                },
                label = "visualization",
                modifier = Modifier.weight(1f)
            ) { (total, elapsed, unit) ->
                when (unit) {
                    TimeUnit.MONTH -> MonthCalendar(
                        totalDays = total,
                        elapsedDays = elapsed,
                        elapsedColor = elapsedColor,
                        remainingColor = remainingColor,
                        currentIndicatorColor = currentIndicatorColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    TimeUnit.WEEK -> WeekView(
                        elapsedDays = elapsed,
                        elapsedColor = elapsedColor,
                        remainingColor = remainingColor,
                        currentIndicatorColor = currentIndicatorColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    TimeUnit.DAY -> DayGrid(
                        totalHours = total,
                        elapsedHours = elapsed,
                        startHour = preferences.activeHoursStart,
                        elapsedColor = elapsedColor,
                        remainingColor = remainingColor,
                        currentIndicatorColor = currentIndicatorColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    TimeUnit.HOUR -> HourClock(
                        totalMinutes = total,
                        elapsedMinutes = elapsed,
                        elapsedColor = elapsedColor,
                        remainingColor = remainingColor,
                        currentIndicatorColor = currentIndicatorColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    )
                    TimeUnit.LIFE -> DotGrid(
                        totalUnits = total,
                        elapsedUnits = elapsed,
                        symbolType = symbolType,
                        elapsedColor = elapsedColor,
                        remainingColor = remainingColor,
                        currentIndicatorColor = currentIndicatorColor,
                        columns = 52,
                        fillAvailableSpace = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp)
                    )
                    else -> DotGrid(
                        totalUnits = total,
                        elapsedUnits = elapsed,
                        symbolType = symbolType,
                        elapsedColor = elapsedColor,
                        remainingColor = remainingColor,
                        currentIndicatorColor = currentIndicatorColor,
                        fillAvailableSpace = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    )
                }
            }

            // Caption row — whisper-quiet labels at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeData.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedContent(
                        targetState = timeData.remainingText,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "remainingText"
                    ) { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
        }

    }
}

/** Presentation model consumed by [LeftScreen] to render the caption and grid. */
data class TimeData(
    val total: Int,
    val elapsed: Int,
    val label: String,
    val remainingText: String,
    val progressPercent: Float
)

/** Maps a [TimeUnit] + user preferences into a [TimeData] snapshot. */
fun getTimeData(unit: TimeUnit, preferences: UserPreferencesData): TimeData {
    return when (unit) {
        TimeUnit.LIFE -> {
            val birthDate = preferences.birthDate ?: LocalDate.of(1990, 1, 1)
            val lifespan = preferences.expectedLifespan
            val totalWeeks = lifespan * 52
            val elapsedWeeks = TimeCalculations.lifeWeeksElapsed(birthDate)
            val remainingYears = TimeCalculations.lifeYearsRemaining(birthDate, lifespan)
            TimeData(totalWeeks, elapsedWeeks, "Life", "$remainingYears yrs left",
                if (totalWeeks > 0) (elapsedWeeks.toFloat() / totalWeeks) * 100f else 0f)
        }
        TimeUnit.YEAR -> {
            val total = TimeCalculations.totalDaysInYear()
            val elapsed = TimeCalculations.daysElapsedInYear()
            val remaining = TimeCalculations.daysLeftInYear()
            TimeData(total, elapsed, TimeCalculations.yearLabel(), "$remaining left",
                (elapsed.toFloat() / total) * 100f)
        }
        TimeUnit.MONTH -> {
            val total = TimeCalculations.totalDaysInMonth()
            val elapsed = TimeCalculations.daysElapsedInMonth()
            val remaining = TimeCalculations.daysLeftInMonth()
            TimeData(total, elapsed, TimeCalculations.monthLabel(), "$remaining left",
                (elapsed.toFloat() / total) * 100f)
        }
        TimeUnit.WEEK -> {
            val total = TimeCalculations.totalDaysInWeek()
            val elapsed = TimeCalculations.daysElapsedInWeek()
            val remaining = TimeCalculations.daysLeftInWeek()
            TimeData(total, elapsed, TimeCalculations.weekLabel(), "$remaining left",
                (elapsed.toFloat() / total) * 100f)
        }
        TimeUnit.DAY -> {
            val start = preferences.activeHoursStart
            val end = preferences.activeHoursEnd
            val total = TimeCalculations.totalHoursInDay(start, end)
            val elapsed = TimeCalculations.hoursElapsedInDay(start)
            val remaining = TimeCalculations.hoursLeftInDay(start, end)
            TimeData(total, elapsed, TimeCalculations.dayLabel(), "$remaining left",
                if (total > 0) (elapsed.toFloat() / total) * 100f else 0f)
        }
        TimeUnit.HOUR -> {
            val total = TimeCalculations.totalMinutesInHour()
            val elapsed = TimeCalculations.minutesElapsedInHour()
            val remaining = TimeCalculations.minutesLeftInHour()
            TimeData(total, elapsed, TimeCalculations.hourLabel(), "$remaining left",
                (elapsed.toFloat() / total) * 100f)
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.White
    }
}
