package com.timeleft.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import com.timeleft.MainActivity
import com.timeleft.data.preferences.UserPreferencesRepository
import com.timeleft.util.TimeCalculations
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Month widget with true calendar-offset rendering.
 */
class MonthProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val card = style.cardColors(hueShift = 42f, saturationMul = 1.04f, valueMul = 1.06f, glowAlphaBoost = 1.22f)

        val total = TimeCalculations.totalDaysInMonth()
        val elapsed = TimeCalculations.daysElapsedInMonth()
        val remaining = TimeCalculations.daysLeftInMonth()
        val monthName = TimeCalculations.monthLabel()
        val percent = ((elapsed.toFloat() / total) * 100f).toInt()

        val firstOfMonth = LocalDate.now().withDayOfMonth(1)
        val startOffset = firstOfMonth.dayOfWeek.value - 1

        val calendarBitmap = WidgetRenderer.renderMonthCalendarDots(
            width = 760,
            height = 480,
            totalDays = total,
            elapsedDays = elapsed,
            startOffset = startOffset,
            elapsedColor = style.elapsedColor,
            remainingColor = style.remainingColor,
            currentColor = style.currentColor,
            backgroundColor = 0x00000000
        )
        val backgroundBitmap = WidgetRenderer.renderAtmosphericCard(
            width = 1080,
            height = 1080,
            startColor = card.start,
            endColor = card.end,
            glowColor = card.glow,
            borderColor = card.border
        )

        provideContent {
            MonthWidgetContent(
                context = context,
                monthName = monthName,
                remaining = remaining,
                percent = percent,
                calendarBitmap = calendarBitmap,
                backgroundBitmap = backgroundBitmap,
                style = style
            )
        }
    }
}

@Composable
private fun MonthWidgetContent(
    context: Context,
    monthName: String,
    remaining: Int,
    percent: Int,
    calendarBitmap: Bitmap,
    backgroundBitmap: Bitmap,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 130.dp || size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "MONTH")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundBitmap
    ) {
        Column {
            WidgetHeader(
                title = monthName,
                badge = "$percent%",
                style = style,
                compact = compact
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Image(
                provider = ImageProvider(calendarBitmap),
                contentDescription = "Month calendar progress",
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = GlanceModifier.height(5.dp))
            WidgetFooter(
                leading = if (compact) "$remaining left" else "$remaining days left",
                trailing = "Calendar",
                style = style,
                compact = compact
            )
        }
    }
}

class MonthProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthProgressWidget()
}
