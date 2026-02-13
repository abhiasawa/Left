package com.timeleft.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import com.timeleft.MainActivity
import com.timeleft.data.preferences.UserPreferencesRepository
import com.timeleft.util.TimeCalculations
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Time Atlas: Month panel as a true calendar terrain.
 */
class MonthProgressWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val card = style.cardColors(hueShift = 52f, saturationMul = 1.06f, valueMul = 1.1f, glowAlphaBoost = 1.26f)

        val total = TimeCalculations.totalDaysInMonth()
        val elapsed = TimeCalculations.daysElapsedInMonth()
        val remaining = TimeCalculations.daysLeftInMonth()
        val monthName = TimeCalculations.monthLabel()
        val percent = ((elapsed.toFloat() / total.coerceAtLeast(1)) * 100f).toInt()

        val firstOfMonth = LocalDate.now().withDayOfMonth(1)
        val startOffset = firstOfMonth.dayOfWeek.value - 1

        val backgrounds = BitmapVariants(
            square = WidgetRenderer.renderAtlasCard(1080, 1080, card.start, card.end, card.glow, card.border),
            wide = WidgetRenderer.renderAtlasCard(1500, 900, card.start, card.end, card.glow, card.border),
            tall = WidgetRenderer.renderAtlasCard(900, 1500, card.start, card.end, card.glow, card.border)
        )
        val fields = BitmapVariants(
            square = WidgetRenderer.renderAtlasCalendarField(880, 560, total, elapsed, startOffset, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000),
            wide = WidgetRenderer.renderAtlasCalendarField(1260, 430, total, elapsed, startOffset, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000),
            tall = WidgetRenderer.renderAtlasCalendarField(740, 980, total, elapsed, startOffset, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000)
        )

        provideContent {
            MonthWidgetContent(
                context = context,
                monthName = monthName,
                remaining = remaining,
                percent = percent,
                backgroundVariants = backgrounds,
                fieldVariants = fields,
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
    backgroundVariants: BitmapVariants,
    fieldVariants: BitmapVariants,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 145.dp || size.height < 145.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "MONTH")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundVariants.forWidgetSize(size),
        contentPadding = if (compact) 7.dp else 10.dp
    ) {
        Column(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
            WidgetHeader(
                title = "$monthName Atlas",
                badge = "$percent%",
                style = style,
                compact = compact
            )
            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 3.dp else 8.dp))
            androidx.glance.Image(
                provider = androidx.glance.ImageProvider(fieldVariants.forWidgetSize(size)),
                contentDescription = "Month atlas field",
                modifier = androidx.glance.GlanceModifier.defaultWeight(),
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 5.dp))
            WidgetFooter(
                leading = if (compact) "$remaining left" else "$remaining days left",
                trailing = if (compact) null else "Calendar relief",
                style = style,
                compact = compact
            )
        }
    }
}

class MonthProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MonthProgressWidget()
}
