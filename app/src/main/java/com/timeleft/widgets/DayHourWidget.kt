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

/**
 * Day widget rendered as an orbit timeline.
 */
class DayHourWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val card = style.cardColors(hueShift = 160f, saturationMul = 1.05f, valueMul = 1.02f, glowAlphaBoost = 1.2f)

        val totalHours = TimeCalculations.totalHoursInDay()
        val elapsedHours = TimeCalculations.hoursElapsedInDay()
        val remainingHours = TimeCalculations.hoursLeftInDay()
        val dayLabel = TimeCalculations.dayLabel()
        val percent = ((elapsedHours.toFloat() / totalHours.coerceAtLeast(1)) * 100f).toInt()

        val orbitBitmap = WidgetRenderer.renderOrbitDots(
            width = 760,
            height = 500,
            totalUnits = totalHours,
            elapsedUnits = elapsedHours,
            elapsedColor = style.elapsedColor,
            remainingColor = style.remainingColor,
            currentColor = style.currentColor,
            backgroundColor = 0x00000000,
            emphasizeEvery = 6
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
            DayHourWidgetContent(
                context = context,
                dayLabel = dayLabel,
                remainingHours = remainingHours,
                percent = percent,
                orbitBitmap = orbitBitmap,
                backgroundBitmap = backgroundBitmap,
                style = style
            )
        }
    }
}

@Composable
private fun DayHourWidgetContent(
    context: Context,
    dayLabel: String,
    remainingHours: Int,
    percent: Int,
    orbitBitmap: Bitmap,
    backgroundBitmap: Bitmap,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 130.dp || size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "DAY")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundBitmap
    ) {
        Column {
            WidgetHeader(
                title = "Day",
                badge = "$percent%",
                style = style,
                compact = compact
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            WidgetHeroMetric(
                value = "$remainingHours",
                label = if (compact) "hours left" else "$dayLabel hours left",
                style = style,
                compact = compact
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Image(
                provider = ImageProvider(orbitBitmap),
                contentDescription = "Day orbit progress",
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .padding(horizontal = 8.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            WidgetFooter(
                leading = if (compact) "Cycle" else dayLabel,
                trailing = if (compact) null else "Active hours",
                style = style,
                compact = compact
            )
        }
    }
}

class DayHourWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayHourWidget()
}
