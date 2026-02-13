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
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Day visualization: dots arranged in a circular ring, one per hour.
 *
 * Each dot represents one active hour. Elapsed hours are dimmed,
 * the current hour pulses red, remaining hours are bright. Every 6th
 * dot is larger as a quadrant marker (midnight/6am/noon/6pm).
 * Mirrors the HourClock's circular language at a different time scale.
 */
@Composable
fun DayGrid(
    totalHours: Int,
    elapsedHours: Int,
    startHour: Int = 0,
    elapsedColor: Color,
    remainingColor: Color,
    currentIndicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember(totalHours, elapsedHours) { Animatable(0f) }
    LaunchedEffect(totalHours, elapsedHours) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            1f,
            tween(durationMillis = 680, easing = FastOutSlowInEasing)
        )
    }

    val glowPulse = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        glowPulse.animateTo(
            1f,
            infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse)
        )
    }

    Canvas(modifier = modifier.fillMaxWidth()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        if (totalHours <= 0 || canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        val maxDim = min(canvasWidth, canvasHeight)
        val ringRadius = maxDim / 2f * 0.80f
        val dotRadius = maxDim * 0.028f
        val bigDotRadius = dotRadius * 1.6f

        for (i in 0 until totalHours) {
            // Start from 12-o'clock position (-90 degrees), go clockwise
            val angle = Math.toRadians((i * 360.0 / totalHours) - 90.0)

            val isElapsed = i < elapsedHours
            val isCurrent = i == elapsedHours
            val color = when {
                isCurrent -> currentIndicatorColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            val itemProgress = ((animationProgress.value - i * 0.008f) / 0.66f).coerceIn(0f, 1f)
            // Every 6th hour is a quadrant marker (larger dot)
            val isQuadrant = i % 6 == 0
            val r = (if (isQuadrant) bigDotRadius else dotRadius) * itemProgress

            val cx = center.x + ringRadius * cos(angle).toFloat()
            val cy = center.y + ringRadius * sin(angle).toFloat()

            if (r > 0f) {
                if (isCurrent) {
                    val glowAlpha = 0.15f + glowPulse.value * 0.15f
                    drawCircle(
                        color = currentIndicatorColor.copy(alpha = glowAlpha * 0.5f),
                        radius = r * 3.5f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = currentIndicatorColor.copy(alpha = glowAlpha),
                        radius = r * 2f,
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
