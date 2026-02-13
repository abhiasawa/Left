package com.timeleft.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.min

/**
 * Month visualization: a familiar 7-column calendar grid.
 *
 * Days are rendered as rounded cells with day numbers. Past days are dimmed,
 * the current day glows red, future days are bright. Monday-first layout
 * with faint weekday headers. Immediately recognizable as a calendar.
 */
@Composable
fun MonthCalendar(
    totalDays: Int,
    elapsedDays: Int,
    elapsedColor: Color,
    remainingColor: Color,
    currentIndicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val now = LocalDate.now()
    val firstOfMonth = now.withDayOfMonth(1)
    // Monday-first: MONDAY=0 offset, SUNDAY=6 offset
    val startOffset = firstOfMonth.dayOfWeek.value - 1

    val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")

    val animationProgress = remember(totalDays, elapsedDays) { Animatable(0f) }
    LaunchedEffect(totalDays, elapsedDays) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    val glowPulse = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        glowPulse.animateTo(
            1f,
            infiniteRepeatable(tween(1200), RepeatMode.Reverse)
        )
    }

    Canvas(modifier = modifier.fillMaxWidth()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        if (totalDays <= 0 || canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        val cols = 7
        val totalSlots = startOffset + totalDays
        val dataRows = ceil(totalSlots.toFloat() / cols).toInt()
        val totalRows = dataRows + 1 // +1 for header

        val gap = with(density) { 3.dp.toPx() }
        val cellW = (canvasWidth - (cols - 1) * gap) / cols
        val cellH = (canvasHeight - (totalRows - 1) * gap) / totalRows
        val cellSize = min(cellW, cellH)
        val cornerR = cellSize * 0.22f

        // Center grid
        val gridW = cols * cellSize + (cols - 1) * gap
        val gridH = totalRows * cellSize + (totalRows - 1) * gap
        val offsetX = (canvasWidth - gridW) / 2f
        val offsetY = (canvasHeight - gridH) / 2f

        // Header row (M T W T F S S)
        val headerPaint = Paint().apply {
            color = remainingColor.copy(alpha = 0.2f).toArgb()
            textSize = cellSize * 0.32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        for (col in 0 until cols) {
            val cx = offsetX + col * (cellSize + gap) + cellSize / 2f
            val cy = offsetY + cellSize / 2f + headerPaint.textSize * 0.35f
            drawContext.canvas.nativeCanvas.drawText(dayHeaders[col], cx, cy, headerPaint)
        }

        // Day cells
        for (day in 1..totalDays) {
            val slot = startOffset + (day - 1)
            val col = slot % cols
            val row = slot / cols + 1

            val x = offsetX + col * (cellSize + gap)
            val y = offsetY + row * (cellSize + gap)

            val dayIndex = day - 1
            val isElapsed = dayIndex < elapsedDays
            val isCurrent = dayIndex == elapsedDays
            val color = when {
                isCurrent -> currentIndicatorColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            val itemProgress = ((animationProgress.value - dayIndex * 0.012f) / 0.5f).coerceIn(0f, 1f)

            if (itemProgress > 0f) {
                // Glow for current day
                if (isCurrent) {
                    val glowAlpha = 0.08f + glowPulse.value * 0.08f
                    drawRoundRect(
                        color = currentIndicatorColor.copy(alpha = glowAlpha),
                        topLeft = Offset(x - gap, y - gap),
                        size = Size(cellSize + gap * 2, cellSize + gap * 2),
                        cornerRadius = CornerRadius(cornerR * 1.4f)
                    )
                }

                // Cell background
                val bgAlpha = when {
                    isCurrent -> 0.3f * itemProgress
                    isElapsed -> 0.08f * itemProgress
                    else -> 0.12f * itemProgress
                }
                drawRoundRect(
                    color = color.copy(alpha = bgAlpha),
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize),
                    cornerRadius = CornerRadius(cornerR)
                )

                // Day number
                if (itemProgress > 0.15f) {
                    val textAlpha = ((itemProgress - 0.15f) / 0.85f).coerceIn(0f, 1f)
                    val dayPaint = Paint().apply {
                        this.color = color.copy(alpha = textAlpha).toArgb()
                        this.textSize = cellSize * 0.38f
                        this.textAlign = Paint.Align.CENTER
                        this.typeface = if (isCurrent)
                            Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        else
                            Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        this.isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        day.toString(),
                        x + cellSize / 2f,
                        y + cellSize / 2f + dayPaint.textSize * 0.35f,
                        dayPaint
                    )
                }
            }
        }
    }
}
