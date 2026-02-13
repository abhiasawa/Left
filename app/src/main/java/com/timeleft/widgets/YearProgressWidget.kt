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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import com.timeleft.MainActivity
import com.timeleft.data.preferences.UserPreferencesRepository
import com.timeleft.util.TimeCalculations
import kotlinx.coroutines.flow.first

/**
 * Time Atlas: Year panel as a dense survey map of days.
 */
class YearProgressWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val card = style.cardColors(hueShift = 14f, saturationMul = 1.1f, valueMul = 1.04f, glowAlphaBoost = 1.22f)

        val totalDays = TimeCalculations.totalDaysInYear()
        val elapsed = TimeCalculations.daysElapsedInYear()
        val remaining = TimeCalculations.daysLeftInYear()
        val year = TimeCalculations.yearLabel()
        val percent = ((elapsed.toFloat() / totalDays) * 100f).toInt()

        val backgrounds = BitmapVariants(
            square = WidgetRenderer.renderAtlasCard(1080, 1080, card.start, card.end, card.glow, card.border),
            wide = WidgetRenderer.renderAtlasCard(1500, 900, card.start, card.end, card.glow, card.border),
            tall = WidgetRenderer.renderAtlasCard(900, 1500, card.start, card.end, card.glow, card.border)
        )
        val fields = BitmapVariants(
            square = WidgetRenderer.renderAtlasDotField(920, 520, totalDays, elapsed, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000),
            wide = WidgetRenderer.renderAtlasDotField(1280, 430, totalDays, elapsed, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000),
            tall = WidgetRenderer.renderAtlasDotField(760, 980, totalDays, elapsed, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000)
        )

        provideContent {
            YearWidgetContent(
                context = context,
                year = year,
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
private fun YearWidgetContent(
    context: Context,
    year: String,
    remaining: Int,
    percent: Int,
    backgroundVariants: BitmapVariants,
    fieldVariants: BitmapVariants,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 145.dp || size.height < 145.dp
    val short = size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "YEAR")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundVariants.forWidgetSize(size),
        contentPadding = if (compact) 7.dp else 10.dp
    ) {
        Column(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
            WidgetHeader(
                title = "Year Atlas",
                badge = "$percent%",
                style = style,
                compact = compact
            )

            if (!compact && !short) {
                Spacer(modifier = androidx.glance.GlanceModifier.height(4.dp))
                WidgetHeroMetric(
                    value = "$remaining",
                    label = "days remaining",
                    style = style,
                    compact = compact
                )
            }

            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 6.dp))
            androidx.glance.Image(
                provider = androidx.glance.ImageProvider(fieldVariants.forWidgetSize(size)),
                contentDescription = "Year atlas field",
                modifier = androidx.glance.GlanceModifier.defaultWeight(),
                contentScale = androidx.glance.layout.ContentScale.FillBounds
            )
            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
            WidgetFooter(
                leading = if (compact) "$remaining left" else year,
                trailing = if (compact) null else "Chronomap",
                style = style,
                compact = compact
            )
        }
    }
}

class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = YearProgressWidget()
}
