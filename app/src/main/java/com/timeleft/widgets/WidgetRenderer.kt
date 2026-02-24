package com.timeleft.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
        val cardColor = blendColors(startColor, endColor, 0.5f)

        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = cardColor
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, basePaint)

        val resolvedBorder = if (Color.alpha(borderColor) > 0) borderColor else adjustAlpha(glowColor, 0.35f)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = adjustAlpha(resolvedBorder, 0.92f)
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
        emphasizeBand: Boolean = true,
        drawShadow: Boolean = true
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        if (totalUnits <= 0) return bitmap

        val cols = if (columns > 0) columns else optimalColumns(width.toFloat(), height.toFloat(), totalUnits)
        val rows = ceil(totalUnits.toFloat() / cols).toInt().coerceAtLeast(1)

        val cellW = width.toFloat() / cols
        val cellH = height.toFloat() / rows
        val radius = min(cellW, cellH) * 0.44f

        if (emphasizeBand) {
            val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = (min(cellW, cellH) * 0.22f).coerceAtLeast(3f)
                color = adjustAlpha(currentColor, 0.12f)
            }
            val bandRow = (elapsedUnits / cols).coerceIn(0, rows - 1)
            val y = bandRow * cellH + cellH / 2f
            canvas.drawLine(0f, y, width.toFloat(), y, bandPaint)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val shadowPaint = if (drawShadow) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        } else {
            null
        }
        for (i in 0 until totalUnits) {
            val col = i % cols
            val row = i / cols
            val cx = col * cellW + cellW / 2f
            val cy = row * cellH + cellH / 2f

            paint.color = when {
                i == elapsedUnits -> currentColor
                i < elapsedUnits -> elapsedColor
                else -> remainingColor
            }

            if (shadowPaint != null) {
                shadowPaint.color = paint.color
                shadowPaint.alpha = if (i == elapsedUnits) 112 else 56
                canvas.drawCircle(cx, cy + radius * 0.2f, radius * 1.08f, shadowPaint)
            }
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
        val radius = min(cellW, cellH) * 0.43f

        val ridgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (min(cellW, cellH) * 0.08f).coerceAtLeast(1.4f)
            color = adjustAlpha(currentColor, 0.2f)
        }
        for (r in 0 until rows) {
            val y = r * cellH + cellH * 0.5f
            canvas.drawLine(0f, y, width.toFloat(), y, ridgePaint)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        for (day in 0 until totalDays) {
            val slot = startOffset + day
            val col = slot % cols
            val row = slot / cols
            val cx = col * cellW + cellW / 2f
            val cy = row * cellH + cellH / 2f

            paint.color = when {
                day == elapsedDays -> currentColor
                day < elapsedDays -> elapsedColor
                else -> remainingColor
            }

            shadow.color = paint.color
            shadow.alpha = if (day == elapsedDays) 115 else 52
            canvas.drawCircle(cx, cy + radius * 0.2f, radius * 1.08f, shadow)
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
        emphasizeEvery: Int = 6,
        drawShadow: Boolean = true
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
        if (totalUnits <= 0) return bitmap

        val cx = width / 2f
        val cy = height / 2f
        val maxDim = min(width, height).toFloat()
        val radius = maxDim * 0.42f

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (maxDim * 0.016f).coerceAtLeast(2f)
            color = adjustAlpha(remainingColor, 0.18f)
        }
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(rect, -65f, 310f, false, ringPaint)

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val shadow = if (drawShadow) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        } else {
            null
        }
        val minor = maxDim * 0.027f
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

            if (shadow != null) {
                shadow.color = dotPaint.color
                shadow.alpha = if (i == elapsedUnits) 118 else 52
                canvas.drawCircle(x, y + r * 0.3f, r * 1.18f, shadow)
            }
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
        emphasizeBand = true,
        drawShadow = true
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
        emphasizeEvery,
        drawShadow = true
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

    private fun blendColors(first: Int, second: Int, ratio: Float): Int {
        val clamped = ratio.coerceIn(0f, 1f)
        val inv = 1f - clamped
        val a = (Color.alpha(first) * inv + Color.alpha(second) * clamped).toInt()
        val r = (Color.red(first) * inv + Color.red(second) * clamped).toInt()
        val g = (Color.green(first) * inv + Color.green(second) * clamped).toInt()
        val b = (Color.blue(first) * inv + Color.blue(second) * clamped).toInt()
        return Color.argb(a, r, g, b)
    }
}
