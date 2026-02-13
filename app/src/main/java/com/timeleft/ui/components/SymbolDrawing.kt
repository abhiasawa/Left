package com.timeleft.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.timeleft.domain.models.SymbolType
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared symbol renderer for timeline components.
 */
fun DrawScope.drawTimelineSymbol(
    symbolType: SymbolType,
    color: Color,
    cx: Float,
    cy: Float,
    radius: Float,
    index: Int
) {
    when (symbolType) {
        SymbolType.DOT -> {
            drawCircle(color = color, radius = radius, center = Offset(cx, cy))
        }
        SymbolType.SQUARE -> {
            drawRect(
                color = color,
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
        SymbolType.DIAMOND -> {
            val path = Path().apply {
                moveTo(cx, cy - radius)
                lineTo(cx + radius, cy)
                lineTo(cx, cy + radius)
                lineTo(cx - radius, cy)
                close()
            }
            drawPath(path, color, style = Fill)
        }
        SymbolType.STAR -> {
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
        SymbolType.HEART -> {
            val path = Path().apply {
                val size = radius * 2
                val topY = cy - radius * 0.5f
                moveTo(cx, cy + radius * 0.7f)
                cubicTo(cx - size, cy - radius * 0.2f, cx - size * 0.5f, topY - radius, cx, topY)
                cubicTo(cx + size * 0.5f, topY - radius, cx + size, cy - radius * 0.2f, cx, cy + radius * 0.7f)
                close()
            }
            drawPath(path, color, style = Fill)
        }
        SymbolType.HEXAGON -> {
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
        SymbolType.WORD -> {
            val text = (index + 1).toString()
            val textSize = radius * 1.2f
            drawContext.canvas.nativeCanvas.drawText(
                text, cx, cy + textSize * 0.35f,
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
