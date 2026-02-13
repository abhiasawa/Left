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
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.timeleft.MainActivity
import com.timeleft.util.TimeCalculations

/**
 * Home screen widget that shows how many hours remain in the current day
 * using a dot grid. Each dot represents one hour of the 24-hour day.
 */
class DayHourWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val totalHours = TimeCalculations.totalHoursInDay()
        val elapsedHours = TimeCalculations.hoursElapsedInDay()
        val remainingHours = TimeCalculations.hoursLeftInDay()
        val dayLabel = TimeCalculations.dayLabel()

        val dayGridBitmap = WidgetRenderer.renderDotGrid(
            width = 600,
            height = 300,
            totalUnits = totalHours,
            elapsedUnits = elapsedHours,
            elapsedColor = 0xFF333333.toInt(),
            remainingColor = 0xFFFFFFFF.toInt(),
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0x00000000,
            columns = 8
        )

        provideContent {
            DayHourWidgetContent(
                context = context,
                dayLabel = dayLabel,
                remainingHours = remainingHours,
                dayGridBitmap = dayGridBitmap
            )
        }
    }
}

/** Glance composable layout for the day/hour progress widget. */
@Composable
private fun DayHourWidgetContent(
    context: Context,
    dayLabel: String,
    remainingHours: Int,
    dayGridBitmap: Bitmap
) {
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "DAY")
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
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Image(
                provider = ImageProvider(dayGridBitmap),
                contentDescription = "Day progress",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.Fit
            )
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayLabel,
                    style = TextStyle(
                        color = ColorProvider(Color.White, Color.White),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "$remainingHours hours left",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/** Broadcast receiver that binds [DayHourWidget] to the Android widget framework. */
class DayHourWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayHourWidget()
}
