package com.timeleft.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders bitmap-based visual representations (dot grids, barcodes, progress rings)
 * used by home screen widgets. All rendering is done off-screen via [Canvas] so the
 * resulting [Bitmap] can be displayed in Glance widget RemoteViews.
 */
object WidgetRenderer {

    /**
     * Renders a grid of dots where each dot represents one time unit.
     * Elapsed dots are dimmed, the current unit is highlighted, and remaining dots are bright.
     *
     * @param columns If 0, columns are auto-calculated to fill the available width.
     */
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
        dotRadiusPx: Float = 8f,
        spacingPx: Float = 4f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        // Each cell = diameter + spacing; used to compute layout dimensions
        val cellSize = dotRadiusPx * 2 + spacingPx
        val effectiveColumns = if (columns > 0) columns else {
            // Auto-fit as many columns as the width allows
            ((width + spacingPx) / cellSize).toInt().coerceAtLeast(1)
        }

        val rows = ceil(totalUnits.toFloat() / effectiveColumns).toInt()
        val gridWidth = effectiveColumns * cellSize - spacingPx
        val gridHeight = rows * cellSize - spacingPx
        // Center the grid within the bitmap
        val offsetX = (width - gridWidth) / 2f
        val offsetY = (height - gridHeight) / 2f

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (i in 0 until totalUnits) {
            val col = i % effectiveColumns
            val row = i / effectiveColumns

            val cx = offsetX + col * cellSize + dotRadiusPx
            val cy = offsetY + row * cellSize + dotRadiusPx

            paint.color = when {
                i == elapsedUnits -> currentColor
                i < elapsedUnits -> elapsedColor
                else -> remainingColor
            }

            canvas.drawCircle(cx, cy, dotRadiusPx, paint)
        }

        return bitmap
    }

    /**
     * Renders a barcode-style visualization where each vertical bar represents a time unit.
     * Elapsed bars are drawn at half height to create a visual distinction from remaining bars.
     */
    fun renderBarcode(
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

        val barWidth = width.toFloat() / totalUnits
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (i in 0 until totalUnits) {
            paint.color = when {
                i == elapsedUnits -> currentColor
                i < elapsedUnits -> elapsedColor
                else -> remainingColor
            }

            val left = i * barWidth
            // Elapsed bars are half-height so they visually recede
            val barH = if (i < elapsedUnits) height * 0.5f else height.toFloat()
            val top = height - barH
            canvas.drawRect(left, top, left + barWidth - 1f, height.toFloat(), paint)
        }

        return bitmap
    }

    /**
     * Renders a circular progress ring. The arc starts at 12-o'clock (-90 degrees)
     * and sweeps clockwise proportional to [progress] (0f..1f).
     */
    fun renderProgressRing(
        size: Int,
        progress: Float,
        elapsedColor: Int,
        remainingColor: Int,
        backgroundColor: Int,
        strokeWidth: Float = 12f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }

        val padding = strokeWidth / 2 + 4
        val rect = RectF(padding, padding, size - padding, size - padding)

        // Full-circle background track
        paint.color = elapsedColor
        canvas.drawArc(rect, 0f, 360f, false, paint)

        // Foreground arc drawn from 12-o'clock position (-90 deg)
        paint.color = remainingColor
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(rect, -90f, progress * 360f, false, paint)

        return bitmap
    }
}
