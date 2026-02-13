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

/**
 * Time Atlas: Day panel as an orbital route map.
 */
class DayHourWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val card = style.cardColors(hueShift = 170f, saturationMul = 1.04f, valueMul = 1.02f, glowAlphaBoost = 1.2f)

        val totalHours = TimeCalculations.totalHoursInDay()
        val elapsedHours = TimeCalculations.hoursElapsedInDay()
        val remainingHours = TimeCalculations.hoursLeftInDay()
        val dayLabel = TimeCalculations.dayLabel()
        val percent = ((elapsedHours.toFloat() / totalHours.coerceAtLeast(1)) * 100f).toInt()

        val backgrounds = BitmapVariants(
            square = WidgetRenderer.renderAtlasCard(1080, 1080, card.start, card.end, card.glow, card.border),
            wide = WidgetRenderer.renderAtlasCard(1500, 900, card.start, card.end, card.glow, card.border),
            tall = WidgetRenderer.renderAtlasCard(900, 1500, card.start, card.end, card.glow, card.border)
        )
        val fields = BitmapVariants(
            square = WidgetRenderer.renderAtlasOrbitField(760, 560, totalHours, elapsedHours, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000, emphasizeEvery = 6),
            wide = WidgetRenderer.renderAtlasOrbitField(1250, 420, totalHours, elapsedHours, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000, emphasizeEvery = 6),
            tall = WidgetRenderer.renderAtlasOrbitField(760, 980, totalHours, elapsedHours, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000, emphasizeEvery = 6)
        )

        provideContent {
            DayHourWidgetContent(
                context = context,
                dayLabel = dayLabel,
                remainingHours = remainingHours,
                percent = percent,
                backgroundVariants = backgrounds,
                fieldVariants = fields,
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
    backgroundVariants: BitmapVariants,
    fieldVariants: BitmapVariants,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 145.dp || size.height < 145.dp
    val short = size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "DAY")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundVariants.forWidgetSize(size),
        contentPadding = if (compact) 7.dp else 10.dp
    ) {
        Column(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
            WidgetHeader(
                title = "Day Atlas",
                badge = "$percent%",
                style = style,
                compact = compact
            )

            if (!compact && !short) {
                Spacer(modifier = androidx.glance.GlanceModifier.height(4.dp))
                WidgetHeroMetric(
                    value = "$remainingHours",
                    label = "$dayLabel hours left",
                    style = style,
                    compact = compact
                )
            }

            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
            androidx.glance.Image(
                provider = androidx.glance.ImageProvider(fieldVariants.forWidgetSize(size)),
                contentDescription = "Day atlas orbit",
                modifier = androidx.glance.GlanceModifier.defaultWeight(),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
            WidgetFooter(
                leading = if (compact) "$remainingHours left" else dayLabel,
                trailing = if (compact) null else "Orbit",
                style = style,
                compact = compact
            )
        }
    }
}

class DayHourWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DayHourWidget()
}
