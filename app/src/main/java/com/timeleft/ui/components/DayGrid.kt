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
import kotlin.math.ceil
import kotlin.math.min

/**
 * Day visualization: a grid of rounded blocks, one per active hour.
 *
 * Each block shows the hour label (e.g., "6a", "12p"). Past hours are dimmed,
 * the current hour glows red, future hours are bright. Arranged in 4 columns
 * for a clean, immediately readable layout.
 */
@Composable
fun DayGrid(
    totalHours: Int,
    elapsedHours: Int,
    startHour: Int,
    elapsedColor: Color,
    remainingColor: Color,
    currentIndicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val animationProgress = remember(totalHours, elapsedHours) { Animatable(0f) }
    LaunchedEffect(totalHours, elapsedHours) {
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
        if (totalHours <= 0 || canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        val cols = when {
            totalHours <= 6 -> 2
            totalHours <= 9 -> 3
            else -> 4
        }
        val rows = ceil(totalHours.toFloat() / cols).toInt()

        val gap = with(density) { 4.dp.toPx() }
        val cellW = (canvasWidth - (cols - 1) * gap) / cols
        val cellH = (canvasHeight - (rows - 1) * gap) / rows
        val cellSize = min(cellW, cellH)
        val cornerR = cellSize * 0.18f

        // Center grid
        val gridW = cols * cellSize + (cols - 1) * gap
        val gridH = rows * cellSize + (rows - 1) * gap
        val offsetX = (canvasWidth - gridW) / 2f
        val offsetY = (canvasHeight - gridH) / 2f

        for (i in 0 until totalHours) {
            val col = i % cols
            val row = i / cols
            val hour = (startHour + i) % 24

            val x = offsetX + col * (cellSize + gap)
            val y = offsetY + row * (cellSize + gap)

            val isElapsed = i < elapsedHours
            val isCurrent = i == elapsedHours
            val color = when {
                isCurrent -> currentIndicatorColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            val itemProgress = ((animationProgress.value - i * 0.02f) / 0.5f).coerceIn(0f, 1f)

            if (itemProgress > 0f) {
                val scale = itemProgress
                val w = cellSize * scale
                val h = cellSize * scale
                val cx = x + (cellSize - w) / 2f
                val cy = y + (cellSize - h) / 2f

                // Glow for current hour
                if (isCurrent) {
                    val glowAlpha = 0.08f + glowPulse.value * 0.08f
                    drawRoundRect(
                        color = currentIndicatorColor.copy(alpha = glowAlpha),
                        topLeft = Offset(x - gap / 2, y - gap / 2),
                        size = Size(cellSize + gap, cellSize + gap),
                        cornerRadius = CornerRadius(cornerR * 1.3f)
                    )
                }

                // Block
                drawRoundRect(
                    color = color,
                    topLeft = Offset(cx, cy),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(cornerR * scale)
                )

                // Hour label inside block
                if (itemProgress > 0.3f) {
                    val textAlpha = ((itemProgress - 0.3f) / 0.7f).coerceIn(0f, 1f)
                    val label = formatDayHour(hour)

                    val blockLum = color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f
                    val textArgb = if (blockLum > 0.5f) {
                        android.graphics.Color.argb((220 * textAlpha).toInt(), 0, 0, 0)
                    } else {
                        android.graphics.Color.argb((220 * textAlpha).toInt(), 255, 255, 255)
                    }

                    val textSize = cellSize * 0.26f
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        cx + w / 2f,
                        cy + h / 2f + textSize * 0.35f,
                        Paint().apply {
                            this.color = textArgb
                            this.textSize = textSize
                            this.textAlign = Paint.Align.CENTER
                            this.typeface = if (isCurrent)
                                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            else Typeface.DEFAULT
                            this.isAntiAlias = true
                        }
                    )
                }
            }
        }
    }
}

private fun formatDayHour(hour: Int): String = when {
    hour == 0 || hour == 24 -> "12a"
    hour < 12 -> "${hour}a"
    hour == 12 -> "12p"
    else -> "${hour - 12}p"
}
