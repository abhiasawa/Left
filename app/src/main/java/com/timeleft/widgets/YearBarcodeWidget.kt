package com.timeleft.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
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
import com.timeleft.MainActivity
import com.timeleft.util.TimeCalculations
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Barcode widget with a red "now" marker line.
 * Compact date info at top, barcode fills the middle, caption at bottom.
 */
class YearBarcodeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val totalDays = TimeCalculations.totalDaysInYear()
        val elapsed = TimeCalculations.daysElapsedInYear()
        val remaining = TimeCalculations.daysLeftInYear()
        val percent = (elapsed.toFloat() / totalDays * 100).toInt()

        val now = LocalDate.now()
        val dayOfWeek = now.format(DateTimeFormatter.ofPattern("EEE"))
        val dayNum = now.dayOfMonth.toString()
        val month = now.format(DateTimeFormatter.ofPattern("MMM"))
        val year = now.year.toString()

        val barcodeBitmap = WidgetRenderer.renderBarcode(
            width = 1000,
            height = 300,
            totalUnits = totalDays,
            elapsedUnits = elapsed,
            elapsedColor = 0xFF333333.toInt(),
            remainingColor = 0xFFFFFFFF.toInt(),
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0x00000000
        )

        provideContent {
            BarcodeWidgetContent(
                context = context,
                dayOfWeek = dayOfWeek,
                dayNum = dayNum,
                month = month,
                year = year,
                remaining = remaining,
                percent = percent,
                barcodeBitmap = barcodeBitmap
            )
        }
    }
}

@Composable
private fun BarcodeWidgetContent(
    context: Context,
    dayOfWeek: String,
    dayNum: String,
    month: String,
    year: String,
    remaining: Int,
    percent: Int,
    barcodeBitmap: Bitmap
) {
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "YEAR")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(ColorProvider(Color(0xD91C1C1E), Color(0xD91C1C1E)))
            .padding(10.dp)
            .clickable(actionStartActivity(openAppIntent))
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Compact date header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dayOfWeek $dayNum",
                    style = TextStyle(
                        color = ColorProvider(Color.White, Color.White),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "  $month  $year",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF666666), Color(0xFF666666)),
                        fontSize = 11.sp
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "$percent%",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF666666), Color(0xFF666666)),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            // Barcode fills the middle
            Image(
                provider = ImageProvider(barcodeBitmap),
                contentDescription = "Year progress barcode",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            // Bottom caption
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = year,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF666666), Color(0xFF666666)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "$remaining days left",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF666666), Color(0xFF666666)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

class YearBarcodeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearBarcodeWidget()
}
