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

/**
 * Home screen widget showing the current month's progress as a 7-column dot grid.
 * The 7-column layout mirrors a calendar week, making it intuitive
 * to see how far through the month we are.
 */
class MonthProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val total = TimeCalculations.totalDaysInMonth()
        val elapsed = TimeCalculations.daysElapsedInMonth()
        val remaining = TimeCalculations.daysLeftInMonth()
        val monthName = TimeCalculations.monthLabel()

        val gridBitmap = WidgetRenderer.renderDotGrid(
            width = 500,
            height = 400,
            totalUnits = total,
            elapsedUnits = elapsed,
            elapsedColor = 0xFF3A3A3A.toInt(),
            remainingColor = 0xFFFFFFFF.toInt(),
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0xFF000000.toInt(),
            columns = 7
        )

        provideContent {
            MonthWidgetContent(
                monthName = monthName,
                remaining = remaining,
                gridBitmap = gridBitmap
            )
        }
    }
}

/** Glance composable layout for the month progress widget. */
@Composable
private fun MonthWidgetContent(
    monthName: String,
    remaining: Int,
    gridBitmap: Bitmap
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.Black, Color.Black))
            .padding(8.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = monthName,
                style = TextStyle(
                    color = ColorProvider(Color.White, Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "$remaining days left",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Image(
                provider = ImageProvider(gridBitmap),
                contentDescription = "Month progress",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/** Broadcast receiver that binds [MonthProgressWidget] to the Android widget framework. */
class MonthProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthProgressWidget()
}
