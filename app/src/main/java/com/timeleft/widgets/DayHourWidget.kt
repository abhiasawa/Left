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
 * Home screen widget that shows how many hours remain in the current day
 * using a dot grid. Each dot represents one hour of the 24-hour day.
 * Also computes minute-level data for potential future use.
 */
class DayHourWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val totalHours = TimeCalculations.totalHoursInDay()
        val elapsedHours = TimeCalculations.hoursElapsedInDay()
        val remainingHours = TimeCalculations.hoursLeftInDay()
        val dayLabel = TimeCalculations.dayLabel()

        // Minute-level data computed but only the day grid is rendered currently
        val totalMinutes = TimeCalculations.totalMinutesInHour()
        val elapsedMinutes = TimeCalculations.minutesElapsedInHour()
        val remainingMinutes = TimeCalculations.minutesLeftInHour()
        val hourLabel = TimeCalculations.hourLabel()

        val dayGridBitmap = WidgetRenderer.renderDotGrid(
            width = 600,
            height = 300,
            totalUnits = totalHours,
            elapsedUnits = elapsedHours,
            elapsedColor = 0xFF3A3A3A.toInt(),
            remainingColor = 0xFFFFFFFF.toInt(),
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0xFF000000.toInt(),
            columns = 8
        )

        val hourGridBitmap = WidgetRenderer.renderDotGrid(
            width = 600,
            height = 300,
            totalUnits = totalMinutes,
            elapsedUnits = elapsedMinutes,
            elapsedColor = 0xFF3A3A3A.toInt(),
            remainingColor = 0xFFFFFFFF.toInt(),
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0xFF000000.toInt(),
            columns = 10
        )

        provideContent {
            DayHourWidgetContent(
                dayLabel = dayLabel,
                remainingHours = remainingHours,
                hourLabel = hourLabel,
                remainingMinutes = remainingMinutes,
                dayGridBitmap = dayGridBitmap
            )
        }
    }
}

/** Glance composable layout for the day/hour progress widget. */
@Composable
private fun DayHourWidgetContent(
    dayLabel: String,
    remainingHours: Int,
    hourLabel: String,
    remainingMinutes: Int,
    dayGridBitmap: Bitmap
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
                text = dayLabel,
                style = TextStyle(
                    color = ColorProvider(Color.White, Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "$remainingHours hours left",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Image(
                provider = ImageProvider(dayGridBitmap),
                contentDescription = "Day progress",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/** Broadcast receiver that binds [DayHourWidget] to the Android widget framework. */
class DayHourWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayHourWidget()
}
