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
import androidx.glance.LocalSize
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
import com.timeleft.data.preferences.UserPreferencesRepository
import com.timeleft.util.TimeCalculations
import kotlinx.coroutines.flow.first

/**
 * Home screen widget showing the current month's progress as a 7-column dot grid.
 * The 7-column layout mirrors a calendar week, making it intuitive
 * to see how far through the month we are.
 */
class MonthProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)

        val total = TimeCalculations.totalDaysInMonth()
        val elapsed = TimeCalculations.daysElapsedInMonth()
        val remaining = TimeCalculations.daysLeftInMonth()
        val monthName = TimeCalculations.monthLabel()

        val gridBitmap = WidgetRenderer.renderDotGrid(
            width = 500,
            height = 400,
            totalUnits = total,
            elapsedUnits = elapsed,
            elapsedColor = style.elapsedColor,
            remainingColor = style.remainingColor,
            currentColor = style.currentColor,
            backgroundColor = 0x00000000,
            columns = 7
        )
        val backgroundBitmap = WidgetRenderer.renderAtmosphericCard(
            width = 900,
            height = 900,
            startColor = style.cardStart,
            endColor = style.cardEnd,
            glowColor = style.cardGlow,
            borderColor = style.cardBorder
        )

        provideContent {
            MonthWidgetContent(
                context = context,
                monthName = monthName,
                remaining = remaining,
                gridBitmap = gridBitmap,
                backgroundBitmap = backgroundBitmap,
                style = style
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
    gridBitmap: Bitmap,
    backgroundBitmap: Bitmap,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 130.dp || size.height < 130.dp
    val fontSize = if (compact) 10.sp else 11.sp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "MONTH")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(openAppIntent))
    ) {
        Image(
            provider = ImageProvider(backgroundBitmap),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(10.dp),
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
                        color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = if (compact) "$remaining left" else "$remaining days left",
                    style = TextStyle(
                        color = ColorProvider(Color(style.textSecondary), Color(style.textSecondary)),
                        fontSize = fontSize
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
