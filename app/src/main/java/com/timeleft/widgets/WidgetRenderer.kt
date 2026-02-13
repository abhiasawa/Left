package com.timeleft.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.ceil
import kotlin.math.sqrt

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
     * When [columns] is 0, the optimal column count is calculated automatically
     * to best fill the available bitmap area. When [dotRadiusPx] is 0, the dot
     * radius is derived from the cell size (88% fill ratio) for tight, dense packing.
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
        dotRadiusPx: Float = 0f,
        spacingPx: Float = 0f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        if (totalUnits <= 0) return bitmap

        val availW = width.toFloat()
        val availH = height.toFloat()

        // Determine column count: explicit or auto-optimized
        val effectiveCols = if (columns > 0) columns else findOptimalColumns(availW, availH, totalUnits)
        val rows = ceil(totalUnits.toFloat() / effectiveCols).toInt()

        // Calculate cell size so the grid fills the bitmap
        val cellW = availW / effectiveCols
        val cellH = availH / rows
        val cellSize = minOf(cellW, cellH)

        // Dot radius: 38% of cell = 76% diameter fill — clean separation between dots
        val effectiveRadius = if (dotRadiusPx > 0f) dotRadiusPx else cellSize * 0.38f

        // Center the grid within the bitmap
        val gridW = effectiveCols * cellSize
        val gridH = rows * cellSize
        val offsetX = (availW - gridW) / 2f
        val offsetY = (availH - gridH) / 2f

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (i in 0 until totalUnits) {
            val col = i % effectiveCols
            val row = i / effectiveCols

            val cx = offsetX + col * cellSize + cellSize / 2f
            val cy = offsetY + row * cellSize + cellSize / 2f

            paint.color = when {
                i == elapsedUnits -> currentColor
                i < elapsedUnits -> elapsedColor
                else -> remainingColor
            }

            canvas.drawCircle(cx, cy, effectiveRadius, paint)
        }

        return bitmap
    }

    /**
     * Finds the column count that maximizes space utilization for a grid of [totalUnits]
     * dots within a [availW] x [availH] rectangle. Uses the mathematical optimum
     * cols = sqrt(N * W/H) as a starting point and searches nearby values.
     */
    private fun findOptimalColumns(availW: Float, availH: Float, totalUnits: Int): Int {
        val optimalCols = sqrt(totalUnits.toDouble() * availW / availH).toInt()
            .coerceIn(1, totalUnits)

        var bestCols = optimalCols
        var bestScore = -1f

        for (delta in -4..4) {
            val cols = (optimalCols + delta).coerceIn(1, totalUnits)
            val rows = ceil(totalUnits.toFloat() / cols).toInt()

            val cellW = availW / cols
            val cellH = availH / rows
            val cell = minOf(cellW, cellH)

            val gridW = cols * cell
            val gridH = rows * cell
            val utilization = (gridW * gridH) / (availW * availH)

            // Penalize very sparse last rows (less than 25% full)
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

    /**
     * Renders a barcode-style visualization where each vertical bar represents a time unit.
     * Elapsed bars are drawn at half height to visually recede. A red "now" marker line
     * is drawn at the current position, transforming the static barcode into a living clock.
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
        // Bars fill 85% of their slot, leaving 15% gap for individual bar definition
        val barDrawWidth = barWidth * 0.85f
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (i in 0 until totalUnits) {
            paint.color = when {
                i < elapsedUnits -> elapsedColor
                else -> remainingColor
            }

            val left = i * barWidth
            // Elapsed bars are half-height so they visually recede
            val barH = if (i < elapsedUnits) height * 0.5f else height.toFloat()
            val top = height - barH
            canvas.drawRect(left, top, left + barDrawWidth, height.toFloat(), paint)
        }

        // Red "now" marker line at the current position — the pulse of the barcode
        if (elapsedUnits in 0 until totalUnits) {
            val markerX = elapsedUnits * barWidth + barWidth / 2f
            val markerWidth = (width * 0.003f).coerceAtLeast(2f)

            // Vertical marker line
            paint.color = currentColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(
                markerX - markerWidth / 2f, 0f,
                markerX + markerWidth / 2f, height.toFloat(),
                paint
            )

            // Small position badge circle at the top
            val badgeRadius = markerWidth * 3f
            canvas.drawCircle(markerX, badgeRadius + 2f, badgeRadius, paint)
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
