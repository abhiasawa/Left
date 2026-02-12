package com.timeleft.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

/** Per-particle state: all values are randomised once and then immutable. */
private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float,
    val wobbleSpeed: Float,
    val wobbleAmount: Float
)

private val confettiColors = listOf(
    Color(0xFFFF3B30),
    Color(0xFFFF9500),
    Color(0xFFFFCC00),
    Color(0xFF34C759),
    Color(0xFF007AFF),
    Color(0xFFAF52DE),
    Color(0xFFFF2D55),
    Color(0xFF5AC8FA)
)

/**
 * Full-screen confetti burst triggered on milestone progress (25%, 50%, 75%).
 *
 * 60 coloured rectangles fall from the top of the screen with gravity,
 * sine-wave wobble, and individual rotation. The animation runs for 2.5 s
 * then calls [onComplete] to let the parent dismiss it.
 */
@Composable
fun ConfettiAnimation(
    show: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!show) return

    val progress = remember { Animatable(0f) }
    val particles = remember {
        List(60) {
            ConfettiParticle(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.3f - 0.1f,
                speed = 0.4f + Random.nextFloat() * 0.6f,
                size = 4f + Random.nextFloat() * 8f,
                color = confettiColors.random(),
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                wobbleSpeed = 2f + Random.nextFloat() * 4f,
                wobbleAmount = 10f + Random.nextFloat() * 20f
            )
        }
    }

    LaunchedEffect(show) {
        progress.snapTo(0f)
        progress.animateTo(
            1f,
            animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
        )
        onComplete()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value

        particles.forEach { p ->
            val currentY = p.startY + p.speed * t * 1.5f
            if (currentY > 1.2f) return@forEach

            val wobble = sin(t * p.wobbleSpeed * Math.PI.toFloat() * 2) * p.wobbleAmount / w
            val currentX = p.x + wobble
            val alpha = (1f - (t * 0.6f)).coerceIn(0f, 1f)

            val px = currentX * w
            val py = currentY * h

            rotate(
                degrees = p.rotation + p.rotationSpeed * t,
                pivot = Offset(px, py)
            ) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(px - p.size / 2, py - p.size / 2),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
