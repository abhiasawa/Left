package com.timeleft.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.timeleft.domain.models.SymbolType
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.min

/**
 * Month visualization: a 7-column dot grid matching the calendar layout.
 *
 * Each dot represents one day. The grid starts at the correct day-of-week
 * offset so the shape reads as a familiar calendar. No text, no numbers,
 * no headers — purely visual. Elapsed days are dimmed, current day pulses,
 * remaining days are bright.
 */
@Composable
fun MonthCalendar(
    totalDays: Int,
    elapsedDays: Int,
    symbolType: SymbolType,
    elapsedColor: Color,
    remainingColor: Color,
    currentIndicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val now = LocalDate.now()
    val firstOfMonth = now.withDayOfMonth(1)
    // Monday-first: MONDAY=0 offset, SUNDAY=6 offset
    val startOffset = firstOfMonth.dayOfWeek.value - 1

    val animationProgress = remember(totalDays, elapsedDays) { Animatable(0f) }
    LaunchedEffect(totalDays, elapsedDays) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            1f,
            tween(durationMillis = 760, easing = FastOutSlowInEasing)
        )
    }

    val glowPulse = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        glowPulse.animateTo(
            1f,
            infiniteRepeatable(tween(1450, easing = FastOutSlowInEasing), RepeatMode.Reverse)
        )
    }

    Canvas(modifier = modifier.fillMaxWidth()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        if (totalDays <= 0 || canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        val cols = 7
        val totalSlots = startOffset + totalDays
        val rows = ceil(totalSlots.toFloat() / cols).toInt()

        val cellW = canvasWidth / cols
        val cellH = canvasHeight / rows
        val cellSize = min(cellW, cellH)
        val dotRadius = cellSize * 0.36f

        // Center the grid
        val gridW = cols * cellSize
        val gridH = rows * cellSize
        val offsetX = (canvasWidth - gridW) / 2f
        val offsetY = (canvasHeight - gridH) / 2f

        for (day in 1..totalDays) {
            val slot = startOffset + (day - 1)
            val col = slot % cols
            val row = slot / cols

            val cx = offsetX + col * cellSize + cellSize / 2f
            val cy = offsetY + row * cellSize + cellSize / 2f

            val dayIndex = day - 1
            val isElapsed = dayIndex < elapsedDays
            val isCurrent = dayIndex == elapsedDays
            val color = when {
                isCurrent -> currentIndicatorColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            val itemProgress = ((animationProgress.value - dayIndex * 0.01f) / 0.62f).coerceIn(0f, 1f)
            val radius = dotRadius * itemProgress

            if (radius > 0f) {
                if (isCurrent) {
                    val glowAlpha = 0.15f + glowPulse.value * 0.15f
                    drawCircle(
                        color = currentIndicatorColor.copy(alpha = glowAlpha * 0.5f),
                        radius = radius * 2.2f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = currentIndicatorColor.copy(alpha = glowAlpha),
                        radius = radius * 1.5f,
                        center = Offset(cx, cy)
                    )
                }

                drawTimelineSymbol(
                    symbolType = symbolType,
                    color = color,
                    cx = cx,
                    cy = cy,
                    radius = radius,
                    index = dayIndex
                )
            }
        }
    }
}
