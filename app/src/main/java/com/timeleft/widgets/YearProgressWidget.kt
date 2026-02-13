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
 * Home screen widget that displays the current year's progress as a dot grid.
 * Each dot represents one day; elapsed days are dimmed, today is red,
 * and remaining days are white.
 */
class YearProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)

        val totalDays = TimeCalculations.totalDaysInYear()
        val elapsed = TimeCalculations.daysElapsedInYear()
        val remaining = TimeCalculations.daysLeftInYear()
        val year = TimeCalculations.yearLabel()

        val gridBitmap = WidgetRenderer.renderDotGrid(
            width = 800,
            height = 500,
            totalUnits = totalDays,
            elapsedUnits = elapsed,
            elapsedColor = style.elapsedColor,
            remainingColor = style.remainingColor,
            currentColor = style.currentColor,
            backgroundColor = 0x00000000,
        )
        val backgroundBitmap = WidgetRenderer.renderAtmosphericCard(
            width = 1000,
            height = 1000,
            startColor = style.cardStart,
            endColor = style.cardEnd,
            glowColor = style.cardGlow,
            borderColor = style.cardBorder
        )

        provideContent {
            YearWidgetContent(
                context = context,
                year = year,
                remaining = remaining,
                gridBitmap = gridBitmap,
                backgroundBitmap = backgroundBitmap,
                style = style
            )
        }
    }
}

/** Glance composable layout for the year progress widget. */
@Composable
private fun YearWidgetContent(
    context: Context,
    year: String,
    remaining: Int,
    gridBitmap: Bitmap,
    backgroundBitmap: Bitmap,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 130.dp || size.height < 130.dp
    val fontSize = if (compact) 10.sp else 11.sp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "YEAR")
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
                contentDescription = "Year progress grid",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.Fit
            )
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = year,
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

/** Broadcast receiver that binds [YearProgressWidget] to the Android widget framework. */
class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearProgressWidget()
}
