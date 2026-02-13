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
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Hour visualization: 60 dots arranged in a circle like a clock face.
 *
 * Each dot represents one minute. Elapsed minutes are dimmed, the current
 * minute pulses red, remaining minutes are bright. Every 5th dot is larger
 * (like hour markers on a clock). The ring fills the available space,
 * connecting to the app's dot language in a circular form.
 */
@Composable
fun HourClock(
    totalMinutes: Int,
    elapsedMinutes: Int,
    elapsedColor: Color,
    remainingColor: Color,
    currentIndicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember(totalMinutes, elapsedMinutes) { Animatable(0f) }
    LaunchedEffect(totalMinutes, elapsedMinutes) {
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
        if (totalMinutes <= 0 || canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        val maxDim = min(canvasWidth, canvasHeight)
        val ringRadius = maxDim / 2f * 0.80f
        val dotRadius = maxDim * 0.020f
        val bigDotRadius = dotRadius * 1.6f

        for (i in 0 until totalMinutes) {
            val angle = Math.toRadians((i * 360.0 / totalMinutes) - 90.0)

            val isElapsed = i < elapsedMinutes
            val isCurrent = i == elapsedMinutes
            val color = when {
                isCurrent -> currentIndicatorColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            val itemProgress = ((animationProgress.value - i * 0.005f) / 0.5f).coerceIn(0f, 1f)
            val isHourMark = i % 5 == 0
            val r = (if (isHourMark) bigDotRadius else dotRadius) * itemProgress

            val cx = center.x + ringRadius * cos(angle).toFloat()
            val cy = center.y + ringRadius * sin(angle).toFloat()

            if (r > 0f) {
                // Glow for current minute
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
