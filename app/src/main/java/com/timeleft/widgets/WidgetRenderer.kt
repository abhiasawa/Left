package com.timeleft.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Atlas renderer for widget visuals.
 */
object WidgetRenderer {

    fun renderAtlasCard(
        width: Int,
        height: Int,
        startColor: Int,
        endColor: Int,
        glowColor: Int,
        borderColor: Int,
        cornerRadius: Float = 42f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                startColor,
                endColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, basePaint)

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.22f,
                height * 0.2f,
                maxOf(width, height) * 0.7f,
                adjustAlpha(glowColor, 0.7f),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)

        val contourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (min(width, height) * 0.0042f).coerceAtLeast(1.6f)
            color = adjustAlpha(borderColor, 0.26f)
        }

        for (i in 0..5) {
            val inset = i * min(width, height) * 0.085f
            val contourRect = RectF(
                inset,
                inset * 0.7f,
                width - inset * 0.6f,
                height - inset
            )
            canvas.drawArc(contourRect, -70f + i * 9f, 120f + i * 10f, false, contourPaint)
        }

        val meridianPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (min(width, height) * 0.0025f).coerceAtLeast(1.2f)
            color = adjustAlpha(borderColor, 0.22f)
        }
        for (i in 0..4) {
            val x = width * (0.16f + i * 0.17f)
            val path = Path().apply {
                moveTo(x, -height * 0.1f)
                cubicTo(
                    x - width * 0.1f,
                    height * 0.2f,
                    x + width * 0.1f,
                    height * 0.7f,
                    x,
                    height * 1.05f
                )
            }
            canvas.drawPath(path, meridianPaint)
        }

        val grainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = adjustAlpha(borderColor, 0.2f)
        }
        for (x in 0 until width step 13) {
            for (y in 0 until height step 13) {
                val pseudoRandom = (x * 17 + y * 31 + width * 3 + height * 7) % 29
                if (pseudoRandom < 3) {
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 0.9f, grainPaint)
                }
            }
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = adjustAlpha(borderColor, 0.75f)
        }
        canvas.drawRoundRect(
            RectF(1f, 1f, width - 1f, height - 1f),
            cornerRadius,
            cornerRadius,
            borderPaint
        )

        return bitmap
    }

    fun renderAtlasDotField(
        width: Int,
        height: Int,
        totalUnits: Int,
        elapsedUnits: Int,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int,
        columns: Int = 0,
        emphasizeBand: Boolean = true
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        if (totalUnits <= 0) return bitmap

        val cols = if (columns > 0) columns else optimalColumns(width.toFloat(), height.toFloat(), totalUnits)
        val rows = ceil(totalUnits.toFloat() / cols).toInt().coerceAtLeast(1)

        val cellW = width.toFloat() / cols
        val cellH = height.toFloat() / rows
        val cell = min(cellW, cellH)
        val radius = cell * 0.35f
        val offsetX = (width - cols * cell) / 2f
        val offsetY = (height - rows * cell) / 2f

        if (emphasizeBand) {
            val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = (cell * 0.24f).coerceAtLeast(3f)
                color = adjustAlpha(currentColor, 0.12f)
            }
            val bandRow = (elapsedUnits / cols).coerceIn(0, rows - 1)
            val y = offsetY + bandRow * cell + cell / 2f
            canvas.drawLine(offsetX, y, offsetX + cols * cell, y, bandPaint)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        for (i in 0 until totalUnits) {
            val col = i % cols
            val row = i / cols
            val cx = offsetX + col * cell + cell / 2f
            val cy = offsetY + row * cell + cell / 2f

            paint.color = when {
                i == elapsedUnits -> currentColor
                i < elapsedUnits -> elapsedColor
                else -> remainingColor
            }

            shadowPaint.color = paint.color
            shadowPaint.alpha = if (i == elapsedUnits) 112 else 56
            canvas.drawCircle(cx, cy + radius * 0.3f, radius * 1.2f, shadowPaint)
            canvas.drawCircle(cx, cy, radius, paint)
        }

        return bitmap
    }

    fun renderAtlasCalendarField(
        width: Int,
        height: Int,
        totalDays: Int,
        elapsedDays: Int,
        startOffset: Int,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        if (totalDays <= 0) return bitmap

        val cols = 7
        val totalSlots = startOffset + totalDays
        val rows = ceil(totalSlots.toFloat() / cols).toInt().coerceAtLeast(1)
        val cellW = width.toFloat() / cols
        val cellH = height.toFloat() / rows
        val cell = min(cellW, cellH)
        val radius = cell * 0.34f
        val offsetX = (width - cols * cell) / 2f
        val offsetY = (height - rows * cell) / 2f

        val ridgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (cell * 0.08f).coerceAtLeast(1.4f)
            color = adjustAlpha(currentColor, 0.2f)
        }
        for (r in 0 until rows) {
            val y = offsetY + r * cell + cell * 0.5f
            canvas.drawLine(offsetX, y, offsetX + cols * cell, y, ridgePaint)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        for (day in 0 until totalDays) {
            val slot = startOffset + day
            val col = slot % cols
            val row = slot / cols
            val cx = offsetX + col * cell + cell / 2f
            val cy = offsetY + row * cell + cell / 2f

            paint.color = when {
                day == elapsedDays -> currentColor
                day < elapsedDays -> elapsedColor
                else -> remainingColor
            }

            shadow.color = paint.color
            shadow.alpha = if (day == elapsedDays) 115 else 52
            canvas.drawCircle(cx, cy + radius * 0.28f, radius * 1.15f, shadow)
            canvas.drawCircle(cx, cy, radius, paint)
        }

        return bitmap
    }

    fun renderAtlasOrbitField(
        width: Int,
        height: Int,
        totalUnits: Int,
        elapsedUnits: Int,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int,
        emphasizeEvery: Int = 6
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
        if (totalUnits <= 0) return bitmap

        val cx = width / 2f
        val cy = height / 2f
        val maxDim = min(width, height).toFloat()
        val radius = maxDim * 0.36f

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (maxDim * 0.016f).coerceAtLeast(2f)
            color = adjustAlpha(remainingColor, 0.18f)
        }
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(rect, -65f, 310f, false, ringPaint)

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        val minor = maxDim * 0.023f
        val major = minor * 1.5f

        for (i in 0 until totalUnits) {
            val angle = Math.toRadians((i * 360.0 / totalUnits) - 90.0)
            val x = cx + (radius * cos(angle)).toFloat()
            val y = cy + (radius * sin(angle)).toFloat()
            val r = if (emphasizeEvery > 0 && i % emphasizeEvery == 0) major else minor

            dotPaint.color = when {
                i == elapsedUnits -> currentColor
                i < elapsedUnits -> elapsedColor
                else -> remainingColor
            }

            shadow.color = dotPaint.color
            shadow.alpha = if (i == elapsedUnits) 118 else 52
            canvas.drawCircle(x, y + r * 0.3f, r * 1.18f, shadow)
            canvas.drawCircle(x, y, r, dotPaint)
        }

        return bitmap
    }

    fun renderAtlasRingField(
        size: Int,
        progress: Float,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int,
        strokeWidth: Float = 14f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        val p = progress.coerceIn(0f, 1f)

        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = adjustAlpha(elapsedColor, 0.8f)
        }
        val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth * 1.02f
            strokeCap = Paint.Cap.ROUND
            color = remainingColor
        }
        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth * 1.9f
            strokeCap = Paint.Cap.ROUND
            color = adjustAlpha(currentColor, 0.35f)
        }

        val pad = strokeWidth / 2f + 6f
        val rect = RectF(pad, pad, size - pad, size - pad)

        canvas.drawArc(rect, 0f, 360f, false, basePaint)
        canvas.drawArc(rect, -90f, p * 360f, false, haloPaint)
        canvas.drawArc(rect, -90f, p * 360f, false, activePaint)

        val angle = Math.toRadians((p * 360f - 90f).toDouble())
        val cx = size / 2f + (rect.width() / 2f * cos(angle)).toFloat()
        val cy = size / 2f + (rect.height() / 2f * sin(angle)).toFloat()
        val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = currentColor
        }
        canvas.drawCircle(cx, cy, strokeWidth * 0.68f, pointPaint)

        return bitmap
    }

    fun renderAtlasBarcodeField(
        width: Int,
        height: Int,
        totalUnits: Int,
        elapsedUnits: Int,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        if (totalUnits <= 0) return bitmap

        val barSlot = width.toFloat() / totalUnits
        val barW = barSlot * 0.86f

        val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (height * 0.006f).coerceAtLeast(1.4f)
            color = adjustAlpha(currentColor, 0.2f)
        }
        for (i in 1..4) {
            val y = height * (i / 5f)
            canvas.drawLine(0f, y, width.toFloat(), y, horizonPaint)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        for (i in 0 until totalUnits) {
            val left = i * barSlot
            val top = if (i < elapsedUnits) height * 0.44f else 0f
            paint.color = if (i < elapsedUnits) elapsedColor else remainingColor
            canvas.drawRect(left, top, left + barW, height.toFloat(), paint)
        }

        if (elapsedUnits in 0 until totalUnits) {
            val x = elapsedUnits * barSlot + barSlot / 2f
            val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = currentColor
            }
            canvas.drawRect(x - 2f, 0f, x + 2f, height.toFloat(), markerPaint)
            canvas.drawCircle(x, 8f, 7f, markerPaint)
        }

        return bitmap
    }

    // ---- Backwards-compatible wrappers used by current widget files ----

    fun renderAtmosphericCard(
        width: Int,
        height: Int,
        startColor: Int,
        endColor: Int,
        glowColor: Int,
        borderColor: Int,
        cornerRadius: Float = 42f
    ): Bitmap = renderAtlasCard(width, height, startColor, endColor, glowColor, borderColor, cornerRadius)

    fun renderDotGrid(
        width: Int,
        height: Int,
        totalUnits: Int,
        elapsedUnits: Int,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int,
        columns: Int = 0,
        dotRadiusPx: Float = 0f,
        spacingPx: Float = 0f
    ): Bitmap = renderAtlasDotField(
        width = width,
        height = height,
        totalUnits = totalUnits,
        elapsedUnits = elapsedUnits,
        elapsedColor = elapsedColor,
        remainingColor = remainingColor,
        currentColor = currentColor,
        backgroundColor = backgroundColor,
        columns = columns,
        emphasizeBand = true
    )

    fun renderMonthCalendarDots(
        width: Int,
        height: Int,
        totalDays: Int,
        elapsedDays: Int,
        startOffset: Int,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int
    ): Bitmap = renderAtlasCalendarField(
        width,
        height,
        totalDays,
        elapsedDays,
        startOffset,
        elapsedColor,
        remainingColor,
        currentColor,
        backgroundColor
    )

    fun renderOrbitDots(
        width: Int,
        height: Int,
        totalUnits: Int,
        elapsedUnits: Int,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int,
        emphasizeEvery: Int = 6
    ): Bitmap = renderAtlasOrbitField(
        width,
        height,
        totalUnits,
        elapsedUnits,
        elapsedColor,
        remainingColor,
        currentColor,
        backgroundColor,
        emphasizeEvery
    )

    fun renderProgressRing(
        size: Int,
        progress: Float,
        elapsedColor: Int,
        remainingColor: Int,
        backgroundColor: Int,
        strokeWidth: Float = 12f
    ): Bitmap = renderAtlasRingField(
        size = size,
        progress = progress,
        elapsedColor = elapsedColor,
        remainingColor = remainingColor,
        currentColor = remainingColor,
        backgroundColor = backgroundColor,
        strokeWidth = strokeWidth
    )

    fun renderBarcode(
        width: Int,
        height: Int,
        totalUnits: Int,
        elapsedUnits: Int,
        elapsedColor: Int,
        remainingColor: Int,
        currentColor: Int,
        backgroundColor: Int
    ): Bitmap = renderAtlasBarcodeField(
        width,
        height,
        totalUnits,
        elapsedUnits,
        elapsedColor,
        remainingColor,
        currentColor,
        backgroundColor
    )

    private fun optimalColumns(availW: Float, availH: Float, totalUnits: Int): Int {
        val optimalCols = sqrt(totalUnits.toDouble() * availW / availH).toInt().coerceIn(1, totalUnits)
        var bestCols = optimalCols
        var bestScore = -1f

        for (delta in -4..4) {
            val cols = (optimalCols + delta).coerceIn(1, totalUnits)
            val rows = ceil(totalUnits.toFloat() / cols).toInt()
            val cellW = availW / cols
            val cellH = availH / rows
            val cell = min(cellW, cellH)
            val utilization = (cols * cell * rows * cell) / (availW * availH)
            val lastRowCount = totalUnits % cols
            val penalty = if (lastRowCount in 1 until (cols * 0.25f).toInt()) 0.15f else 0f
            val score = utilization - penalty
            if (score > bestScore) {
                bestScore = score
                bestCols = cols
            }
        }
        return bestCols
    }

    private fun adjustAlpha(color: Int, alphaFraction: Float): Int {
        val alpha = (Color.alpha(color) * alphaFraction).toInt().coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}
