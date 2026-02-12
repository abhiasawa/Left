package com.timeleft.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.timeleft.domain.models.SymbolType
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

object ShareHelper {

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
        width: Int = 1080,
        columns: Int = 18
    ): Bitmap {
        val dotRadius = 14f
        val spacing = 8f
        val cellSize = (dotRadius * 2) + spacing
        val padding = 60f
        val headerHeight = 120f

        val rows = ceil(totalUnits.toFloat() / columns).toInt()
        val gridHeight = rows * cellSize
        val height = (headerHeight + gridHeight + padding * 3).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(backgroundColor)

        // Header text
        val labelPaint = Paint().apply {
            color = remainingColor
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(label, padding, padding + 42f, labelPaint)

        val subtitlePaint = Paint().apply {
            color = remainingColor
            textSize = 32f
            alpha = 150
            isAntiAlias = true
        }
        val subtitleWidth = subtitlePaint.measureText(remainingText)
        canvas.drawText(remainingText, width - padding - subtitleWidth, padding + 42f, subtitlePaint)

        // Grid
        val gridOffsetX = (width - (columns * cellSize - spacing)) / 2f
        val gridOffsetY = headerHeight + padding

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (i in 0 until totalUnits) {
            val col = i % columns
            val row = i / columns
            val cx = gridOffsetX + col * cellSize + dotRadius
            val cy = gridOffsetY + row * cellSize + dotRadius

            val isElapsed = i < elapsedUnits
            val isCurrent = i == elapsedUnits

            paint.color = when {
                isCurrent -> currentColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            when (symbolType) {
                SymbolType.DOT -> canvas.drawCircle(cx, cy, dotRadius, paint)
                SymbolType.SQUARE -> canvas.drawRect(
                    cx - dotRadius, cy - dotRadius,
                    cx + dotRadius, cy + dotRadius, paint
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
                    val textPaint = Paint().apply {
                        color = paint.color
                        textSize = dotRadius * 1.2f
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }
                    canvas.drawText(
                        (i + 1).toString(), cx,
                        cy + dotRadius * 0.4f, textPaint
                    )
                }
            }
        }

        // Watermark
        val watermarkPaint = Paint().apply {
            color = remainingColor
            textSize = 24f
            alpha = 80
            isAntiAlias = true
        }
        val watermark = "TimeLeft"
        val wmWidth = watermarkPaint.measureText(watermark)
        canvas.drawText(watermark, (width - wmWidth) / 2f, height - padding / 2f, watermarkPaint)

        return bitmap
    }
}
