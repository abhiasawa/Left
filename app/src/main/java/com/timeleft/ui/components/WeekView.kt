package com.timeleft.ui.components

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.min

/**
 * Week visualization: 7 large dots in a horizontal row.
 *
 * Each dot represents one day (Monday through Sunday). Elapsed days
 * are dimmed, the current day pulses red, remaining days are bright.
 * Purely visual — no day names, no text. The count of 7 is universally
 * recognizable as a week.
 */
@Composable
fun WeekView(
    elapsedDays: Int,
    elapsedColor: Color,
    remainingColor: Color,
    currentIndicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val totalDays = 7

    val animationProgress = remember(elapsedDays) { Animatable(0f) }
    LaunchedEffect(elapsedDays) {
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
        if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        // Size dots so they fit in a row with comfortable spacing
        val maxDotDiameter = canvasWidth / (totalDays + (totalDays - 1) * 0.4f)
        val dotRadius = min(maxDotDiameter / 2f, canvasHeight * 0.25f)
        val spacing = dotRadius * 0.8f

        val totalWidth = totalDays * dotRadius * 2 + (totalDays - 1) * spacing
        val startX = (canvasWidth - totalWidth) / 2f + dotRadius
        val centerY = canvasHeight / 2f

        for (i in 0 until totalDays) {
            val cx = startX + i * (dotRadius * 2 + spacing)
            val cy = centerY

            val isElapsed = i < elapsedDays
            val isCurrent = i == elapsedDays
            val color = when {
                isCurrent -> currentIndicatorColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            val itemProgress = ((animationProgress.value - i * 0.08f) / 0.5f).coerceIn(0f, 1f)
            val r = dotRadius * itemProgress

            if (r > 0f) {
                if (isCurrent) {
                    val glowAlpha = 0.15f + glowPulse.value * 0.15f
                    drawCircle(
                        color = currentIndicatorColor.copy(alpha = glowAlpha * 0.5f),
                        radius = r * 2.2f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = currentIndicatorColor.copy(alpha = glowAlpha),
                        radius = r * 1.5f,
                        center = Offset(cx, cy)
                    )
                }

                drawCircle(
                    color = color,
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
