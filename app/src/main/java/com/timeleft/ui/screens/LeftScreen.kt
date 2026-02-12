package com.timeleft.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.domain.models.SymbolType
import com.timeleft.domain.models.TimeUnit
import com.timeleft.ui.components.DotGrid
import com.timeleft.ui.components.TimeSelector
import com.timeleft.ui.components.calculateGridHeight
import com.timeleft.util.TimeCalculations
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeftScreen(
    preferences: UserPreferencesData,
    selectedUnit: TimeUnit,
    onUnitSelected: (TimeUnit) -> Unit,
    onShareClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elapsedColor = parseColor(preferences.elapsedColor)
    val remainingColor = parseColor(preferences.remainingColor)
    val currentIndicatorColor = parseColor(preferences.currentIndicatorColor)
    val symbolType = SymbolType.fromString(preferences.symbolType)

    val hasLifeData = preferences.birthDate != null
    val showLifeOption = hasLifeData

    val timeData = remember(selectedUnit, preferences) {
        getTimeData(selectedUnit, preferences)
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.toFloat()
    val dotSize = 12f
    val spacing = 4f
    val horizontalPadding = 24f
    val availableWidth = screenWidthDp - (horizontalPadding * 2)
    val cellSize = dotSize + spacing
    val autoColumns = ((availableWidth + spacing) / cellSize).toInt().coerceAtLeast(1)
    val gridHeight = calculateGridHeight(timeData.total, autoColumns, dotSize, spacing)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeData.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
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
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Normal
                        )
                    }

                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time unit selector
            TimeSelector(
                selectedUnit = selectedUnit,
                onUnitSelected = onUnitSelected,
                showLifeOption = showLifeOption
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dot Grid
            AnimatedContent(
                targetState = Triple(timeData.total, timeData.elapsed, selectedUnit),
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(200))
                },
                label = "dotGrid"
            ) { (total, elapsed, _) ->
                DotGrid(
                    totalUnits = total,
                    elapsedUnits = elapsed,
                    symbolType = symbolType,
                    elapsedColor = elapsedColor,
                    remainingColor = remainingColor,
                    currentIndicatorColor = currentIndicatorColor,
                    dotSize = dotSize.dp,
                    spacing = spacing.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight.dp)
                        .padding(horizontal = horizontalPadding.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress text at bottom
            Text(
                text = "${String.format("%.1f", timeData.progressPercent)}% elapsed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom nav
        }
    }
}

data class TimeData(
    val total: Int,
    val elapsed: Int,
    val label: String,
    val remainingText: String,
    val progressPercent: Float
)

fun getTimeData(unit: TimeUnit, preferences: UserPreferencesData): TimeData {
    return when (unit) {
        TimeUnit.YEAR -> {
            val total = TimeCalculations.totalDaysInYear()
            val elapsed = TimeCalculations.daysElapsedInYear()
            val remaining = TimeCalculations.daysLeftInYear()
            TimeData(
                total = total,
                elapsed = elapsed,
                label = TimeCalculations.yearLabel(),
                remainingText = "$remaining days left",
                progressPercent = (elapsed.toFloat() / total) * 100f
            )
        }
        TimeUnit.MONTH -> {
            val total = TimeCalculations.totalDaysInMonth()
            val elapsed = TimeCalculations.daysElapsedInMonth()
            val remaining = TimeCalculations.daysLeftInMonth()
            TimeData(
                total = total,
                elapsed = elapsed,
                label = TimeCalculations.monthLabel(),
                remainingText = "$remaining days left",
                progressPercent = (elapsed.toFloat() / total) * 100f
            )
        }
        TimeUnit.WEEK -> {
            val total = TimeCalculations.totalDaysInWeek()
            val elapsed = TimeCalculations.daysElapsedInWeek()
            val remaining = TimeCalculations.daysLeftInWeek()
            TimeData(
                total = total,
                elapsed = elapsed,
                label = TimeCalculations.weekLabel(),
                remainingText = "$remaining days left",
                progressPercent = (elapsed.toFloat() / total) * 100f
            )
        }
        TimeUnit.DAY -> {
            val start = preferences.activeHoursStart
            val end = preferences.activeHoursEnd
            val total = TimeCalculations.totalHoursInDay(start, end)
            val elapsed = TimeCalculations.hoursElapsedInDay(start)
            val remaining = TimeCalculations.hoursLeftInDay(start, end)
            TimeData(
                total = total,
                elapsed = elapsed,
                label = TimeCalculations.dayLabel(),
                remainingText = "$remaining hours left",
                progressPercent = if (total > 0) (elapsed.toFloat() / total) * 100f else 0f
            )
        }
        TimeUnit.HOUR -> {
            val total = TimeCalculations.totalMinutesInHour()
            val elapsed = TimeCalculations.minutesElapsedInHour()
            val remaining = TimeCalculations.minutesLeftInHour()
            TimeData(
                total = total,
                elapsed = elapsed,
                label = TimeCalculations.hourLabel(),
                remainingText = "$remaining min left",
                progressPercent = (elapsed.toFloat() / total) * 100f
            )
        }
        TimeUnit.LIFE -> {
            val birthDate = preferences.birthDate ?: LocalDate.of(1990, 1, 1)
            val lifespan = preferences.expectedLifespan
            val elapsed = TimeCalculations.lifeYearsElapsed(birthDate)
            val remaining = TimeCalculations.lifeYearsRemaining(birthDate, lifespan)
            TimeData(
                total = lifespan,
                elapsed = elapsed,
                label = "Life",
                remainingText = "$remaining years left",
                progressPercent = TimeCalculations.lifeProgress(birthDate, lifespan) * 100f
            )
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
