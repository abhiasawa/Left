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
            elapsedColor = 0xFF333333.toInt(),
            remainingColor = 0xFFFFFFFF.toInt(),
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0x00000000,
            columns = 7
        )

        provideContent {
            MonthWidgetContent(
                context = context,
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
    context: Context,
    monthName: String,
    remaining: Int,
    gridBitmap: Bitmap
) {
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "MONTH")
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
                provider = ImageProvider(gridBitmap),
                contentDescription = "Month progress",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.Fit
            )
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthName,
                    style = TextStyle(
                        color = ColorProvider(Color.White, Color.White),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "$remaining days left",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/** Broadcast receiver that binds [MonthProgressWidget] to the Android widget framework. */
class MonthProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthProgressWidget()
}
