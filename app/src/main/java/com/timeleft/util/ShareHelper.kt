package com.timeleft.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.timeleft.domain.models.SymbolType
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Generates a shareable image of the current progress and launches the Android share sheet.
 */
object ShareHelper {

    /**
     * Creates a snapshot image and opens the system share sheet.
     * The image is saved temporarily in the app's cache directory.
     */
    fun shareTimeLeft(
        context: Context,
        totalUnits: Int,
        elapsedUnits: Int,
        label: String,
        remainingText: String,
        symbolType: SymbolType,
        elapsedColor: Int,
        remainingColor: Int,
        backgroundColor: Int,
        currentColor: Int
    ) {
        val bitmap = renderGrid(
            totalUnits = totalUnits,
            elapsedUnits = elapsedUnits,
            label = label,
            remainingText = remainingText,
            symbolType = symbolType,
            elapsedColor = elapsedColor,
            remainingColor = remainingColor,
            backgroundColor = backgroundColor,
            currentColor = currentColor
        )

        val file = File(context.cacheDir, "timeleft_share.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "$label — $remainingText\nShared from TimeLeft")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share TimeLeft"))
    }

    /**
     * Renders a cinematic, screenshot-friendly grid snapshot.
     */
    fun renderGrid(
        totalUnits: Int,
        elapsedUnits: Int,
        label: String,
        remainingText: String,
        symbolType: SymbolType,
        elapsedColor: Int,
        remainingColor: Int,
        backgroundColor: Int,
        currentColor: Int,
        width: Int = 1440,
        columns: Int = 0
    ): Bitmap {
        val usableWidth = width * 0.86f
        val effectiveColumns = computeColumns(totalUnits, usableWidth, columns)
        val cellSize = (usableWidth / effectiveColumns).coerceIn(32f, 56f)
        val dotRadius = cellSize * 0.34f
        val spacing = cellSize * 0.16f
        val drawCell = dotRadius * 2f + spacing

        val rows = ceil(totalUnits.toFloat() / effectiveColumns).toInt().coerceAtLeast(1)
        val gridHeight = rows * drawCell

        val sidePadding = width * 0.08f
        val headerTop = 98f
        val headerHeight = 170f
        val panelPadding = 28f
        val bottomPadding = 110f

        val height = (headerTop + headerHeight + gridHeight + panelPadding * 2f + bottomPadding).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Atmospheric background for social/award screenshots.
        val bgStart = shiftColor(backgroundColor, 1.18f)
        val bgEnd = shiftColor(backgroundColor, 0.82f)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                bgStart,
                bgEnd,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.RadialGradient(
                width * 0.28f,
                height * 0.24f,
                max(width, height) * 0.72f,
                withAlpha(currentColor, 85),
                android.graphics.Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)

        // Hero panel to keep composition stable across screen ratios.
        val panelTop = headerTop + 24f
        val panelRect = RectF(
            sidePadding,
            panelTop,
            width - sidePadding,
            panelTop + headerHeight + gridHeight + panelPadding * 2
        )
        val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(shiftColor(backgroundColor, 1.32f), 205)
        }
        val panelStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = withAlpha(remainingColor, 64)
        }
        canvas.drawRoundRect(panelRect, 34f, 34f, panelPaint)
        canvas.drawRoundRect(panelRect, 34f, 34f, panelStroke)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = remainingColor
            textSize = 62f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.02f
        }
        canvas.drawText(label, panelRect.left + panelPadding, panelRect.top + 74f, labelPaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(remainingColor, 190)
            textSize = 37f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val subtitleWidth = subtitlePaint.measureText(remainingText)
        canvas.drawText(
            remainingText,
            panelRect.right - panelPadding - subtitleWidth,
            panelRect.top + 74f,
            subtitlePaint
        )

        val gridWidth = effectiveColumns * drawCell - spacing
        val gridOffsetX = (width - gridWidth) / 2f
        val gridOffsetY = panelRect.top + headerHeight + panelPadding

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (i in 0 until totalUnits) {
            val col = i % effectiveColumns
            val row = i / effectiveColumns
            val cx = gridOffsetX + col * drawCell + dotRadius
            val cy = gridOffsetY + row * drawCell + dotRadius

            val isElapsed = i < elapsedUnits
            val isCurrent = i == elapsedUnits

            paint.color = when {
                isCurrent -> currentColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            shadowPaint.color = paint.color
            shadowPaint.alpha = if (isCurrent) 110 else 64
            canvas.drawCircle(cx, cy + dotRadius * 0.32f, dotRadius * 1.2f, shadowPaint)

            when (symbolType) {
                SymbolType.DOT -> canvas.drawCircle(cx, cy, dotRadius, paint)
                SymbolType.SQUARE -> canvas.drawRect(
                    cx - dotRadius,
                    cy - dotRadius,
                    cx + dotRadius,
                    cy + dotRadius,
                    paint
                )

                SymbolType.DIAMOND -> {
                    val path = android.graphics.Path().apply {
                        moveTo(cx, cy - dotRadius)
                        lineTo(cx + dotRadius, cy)
                        lineTo(cx, cy + dotRadius)
                        lineTo(cx - dotRadius, cy)
                        close()
                    }
                    canvas.drawPath(path, paint)
                }

                SymbolType.STAR -> {
                    val path = android.graphics.Path()
                    val outer = dotRadius
                    val inner = dotRadius * 0.4f
                    for (j in 0 until 10) {
                        val r = if (j % 2 == 0) outer else inner
                        val angle = Math.toRadians((j * 36.0) - 90.0)
                        val x = cx + (r * cos(angle)).toFloat()
                        val y = cy + (r * sin(angle)).toFloat()
                        if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    canvas.drawPath(path, paint)
                }

                SymbolType.HEXAGON -> {
                    val path = android.graphics.Path()
                    for (j in 0 until 6) {
                        val angle = Math.toRadians(60.0 * j - 30.0)
                        val x = cx + (dotRadius * cos(angle)).toFloat()
                        val y = cy + (dotRadius * sin(angle)).toFloat()
                        if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    canvas.drawPath(path, paint)
                }

                SymbolType.HEART -> {
                    canvas.drawCircle(cx, cy, dotRadius * 0.7f, paint)
                }

                SymbolType.WORD -> {
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = paint.color
                        textSize = dotRadius * 1.2f
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    canvas.drawText((i + 1).toString(), cx, cy + dotRadius * 0.4f, textPaint)
                }
            }
        }

        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(remainingColor, 130)
            textSize = 27f
            letterSpacing = 0.05f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val watermark = "LEFT"
        val wmWidth = watermarkPaint.measureText(watermark)
        canvas.drawText(watermark, (width - wmWidth) / 2f, height - 42f, watermarkPaint)

        return bitmap
    }

    private fun computeColumns(totalUnits: Int, usableWidth: Float, providedColumns: Int): Int {
        if (providedColumns > 0) return providedColumns
        if (totalUnits <= 0) return 12

        val estimate = sqrt((totalUnits * (usableWidth / 920f)).toDouble()).toInt()
        return estimate.coerceIn(9, min(28, max(9, totalUnits)))
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        val a = alpha.coerceIn(0, 255)
        return android.graphics.Color.argb(
            a,
            android.graphics.Color.red(color),
            android.graphics.Color.green(color),
            android.graphics.Color.blue(color)
        )
    }

    private fun shiftColor(color: Int, factor: Float): Int {
        val r = (android.graphics.Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (android.graphics.Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (android.graphics.Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return android.graphics.Color.rgb(r, g, b)
    }
}
