package com.timeleft.widgets

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.timeleft.util.TimeCalculations
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Home screen widget that visualizes the year's progress as a barcode.
 * Each vertical bar is one day; elapsed bars are shorter and dimmed to
 * create a distinctive barcode-like pattern.
 */
class YearBarcodeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val totalDays = TimeCalculations.totalDaysInYear()
        val elapsed = TimeCalculations.daysElapsedInYear()
        val remaining = TimeCalculations.daysLeftInYear()
        val dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE d MMM yyyy"))
        val percent = (elapsed.toFloat() / totalDays * 100).toInt()

        val barcodeBitmap = WidgetRenderer.renderBarcode(
            width = 1000,
            height = 300,
            totalUnits = totalDays,
            elapsedUnits = elapsed,
            elapsedColor = 0xFF3A3A3A.toInt(),
            remainingColor = 0xFFFFFFFF.toInt(),
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0xFF000000.toInt()
        )

        provideContent {
            BarcodeWidgetContent(
                dateText = dateText,
                remaining = remaining,
                percent = percent,
                barcodeBitmap = barcodeBitmap
            )
        }
    }
}

/** Glance composable layout for the barcode year widget. */
@Composable
private fun BarcodeWidgetContent(
    dateText: String,
    remaining: Int,
    percent: Int,
    barcodeBitmap: Bitmap
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.Black, Color.Black))
            .padding(8.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateText,
                    style = TextStyle(
                        color = ColorProvider(Color.White, Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "$percent%",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                        fontSize = 13.sp
                    )
                )
            }

            Text(
                text = "$remaining days left",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Image(
                provider = ImageProvider(barcodeBitmap),
                contentDescription = "Year progress barcode",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

/** Broadcast receiver that binds [YearBarcodeWidget] to the Android widget framework. */
class YearBarcodeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearBarcodeWidget()
}
