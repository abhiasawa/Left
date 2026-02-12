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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Typeface
import com.timeleft.domain.models.SymbolType
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * Core visualization: renders a grid of symbols representing elapsed vs. remaining time.
 *
 * Features:
 * - Supports 7 symbol shapes (dot, square, diamond, star, heart, hexagon, number).
 * - Staggered spring entrance animation when data changes.
 * - Pulsing glow highlight on the "current" (next-to-fill) unit.
 * - Auto-computes column count from available width when [columns] is 0.
 *
 * @param totalUnits            Total symbols in the grid.
 * @param elapsedUnits          How many symbols are "filled" (elapsed).
 * @param symbolType            Shape to draw for each unit.
 * @param elapsedColor          Color for filled symbols.
 * @param remainingColor        Color for unfilled symbols.
 * @param currentIndicatorColor Highlight color for the next symbol to fill.
 * @param columns               Fixed column count, or 0 to auto-fit.
 */
@Composable
fun DotGrid(
    totalUnits: Int,
    elapsedUnits: Int,
    symbolType: SymbolType,
    elapsedColor: Color,
    remainingColor: Color,
    currentIndicatorColor: Color = Color(0xFFFF3B30),
    columns: Int = 0,
    dotSize: Dp = 12.dp,
    spacing: Dp = 4.dp,
    showCurrentIndicator: Boolean = true,
    animateOnChange: Boolean = true,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val dotSizePx = with(density) { dotSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }

    // Staggered entrance animation with spring physics
    val animationProgress = remember(totalUnits, elapsedUnits) { Animatable(0f) }
    LaunchedEffect(totalUnits, elapsedUnits) {
        if (animateOnChange) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }

    // Pulsing glow for current indicator
    val glowPulse = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        glowPulse.animateTo(
            1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Canvas(
        modifier = modifier.fillMaxWidth()
    ) {
        val canvasWidth = size.width
        val effectiveColumns = if (columns > 0) columns else {
            val cellSize = dotSizePx + spacingPx
            ((canvasWidth + spacingPx) / cellSize).toInt().coerceAtLeast(1)
        }

        val cellWidth = dotSizePx + spacingPx
        val totalWidth = effectiveColumns * cellWidth - spacingPx
        val offsetX = (canvasWidth - totalWidth) / 2f

        for (i in 0 until totalUnits) {
            val col = i % effectiveColumns
            val row = i / effectiveColumns

            val cx = offsetX + col * cellWidth + dotSizePx / 2f
            val cy = row * cellWidth + dotSizePx / 2f

            val isElapsed = i < elapsedUnits
            val isCurrent = showCurrentIndicator && i == elapsedUnits
            val baseColor = if (isElapsed) elapsedColor else remainingColor
            val color = if (isCurrent) currentIndicatorColor else baseColor

            // Stagger: later items start animating slightly after earlier ones
            val itemProgress = if (animateOnChange) {
                val delay = i.toFloat() / totalUnits
                ((animationProgress.value - delay * 0.3f) / 0.7f).coerceIn(0f, 1f)
            } else 1f

            val scale = itemProgress
            val radius = (dotSizePx / 2f) * scale

            if (radius > 0f) {
                // Draw glow behind current indicator
                if (isCurrent) {
                    val glowAlpha = 0.15f + glowPulse.value * 0.15f
                    val glowRadius = radius * (1.6f + glowPulse.value * 0.6f)
                    drawCircle(
                        color = currentIndicatorColor.copy(alpha = glowAlpha * 0.5f),
                        radius = glowRadius,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = currentIndicatorColor.copy(alpha = glowAlpha),
                        radius = radius * (1.3f + glowPulse.value * 0.2f),
                        center = Offset(cx, cy)
                    )
                }

                when (symbolType) {
                    SymbolType.DOT -> {
                        drawCircle(
                            color = color,
                            radius = radius,
                            center = Offset(cx, cy)
                        )
                    }
                    SymbolType.SQUARE -> {
                        drawRect(
                            color = color,
                            topLeft = Offset(cx - radius, cy - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }
                    SymbolType.DIAMOND -> {
                        drawDiamond(color, cx, cy, radius)
                    }
                    SymbolType.STAR -> {
                        drawStar(color, cx, cy, radius)
                    }
                    SymbolType.HEART -> {
                        drawHeart(color, cx, cy, radius)
                    }
                    SymbolType.HEXAGON -> {
                        drawHexagon(color, cx, cy, radius)
                    }
                    SymbolType.WORD -> {
                        val text = (i + 1).toString()
                        val textSize = radius * 1.2f
                        drawContext.canvas.nativeCanvas.drawText(
                            text,
                            cx,
                            cy + textSize * 0.35f,
                            Paint().apply {
                                this.color = color.toArgb()
                                this.textSize = textSize
                                this.textAlign = Paint.Align.CENTER
                                this.typeface = Typeface.DEFAULT_BOLD
                                this.isAntiAlias = true
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Shape drawing helpers ─────────────────────────────────────────────
// Each function draws a single symbol centered at (cx, cy) with the given radius.

private fun DrawScope.drawDiamond(color: Color, cx: Float, cy: Float, radius: Float) {
    val path = Path().apply {
        moveTo(cx, cy - radius)
        lineTo(cx + radius, cy)
        lineTo(cx, cy + radius)
        lineTo(cx - radius, cy)
        close()
    }
    drawPath(path, color, style = Fill)
}

private fun DrawScope.drawStar(color: Color, cx: Float, cy: Float, radius: Float) {
    val path = Path()
    val outerRadius = radius
    val innerRadius = radius * 0.4f
    val points = 5

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = Math.toRadians((i * 360.0 / (points * 2)) - 90.0)
        val x = cx + (r * cos(angle)).toFloat()
        val y = cy + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

private fun DrawScope.drawHeart(color: Color, cx: Float, cy: Float, radius: Float) {
    val path = Path().apply {
        val size = radius * 2
        val topY = cy - radius * 0.5f

        moveTo(cx, cy + radius * 0.7f)

        cubicTo(
            cx - size, cy - radius * 0.2f,
            cx - size * 0.5f, topY - radius,
            cx, topY
        )
        cubicTo(
            cx + size * 0.5f, topY - radius,
            cx + size, cy - radius * 0.2f,
            cx, cy + radius * 0.7f
        )
        close()
    }
    drawPath(path, color, style = Fill)
}

private fun DrawScope.drawHexagon(color: Color, cx: Float, cy: Float, radius: Float) {
    val path = Path()
    for (i in 0 until 6) {
        val angle = Math.toRadians(60.0 * i - 30.0)
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

/** Pre-calculates grid height in dp so the parent can reserve space before drawing. */
fun calculateGridHeight(
    totalUnits: Int,
    columns: Int,
    dotSizeDp: Float,
    spacingDp: Float
): Float {
    val rows = ceil(totalUnits.toFloat() / columns).toInt()
    val cellSize = dotSizeDp + spacingDp
    return rows * cellSize - spacingDp
}
