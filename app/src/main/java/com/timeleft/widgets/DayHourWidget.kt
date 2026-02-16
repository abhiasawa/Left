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
import com.timeleft.util.TimeCalculations

class DayHourWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val style = darkDotWidgetStyle()

        val totalHours = TimeCalculations.totalHoursInDay()
        val elapsedHours = TimeCalculations.hoursElapsedInDay()
        val remainingHours = TimeCalculations.hoursLeftInDay()
        val dayLabel = TimeCalculations.dayLabel()
        val percent = ((elapsedHours.toFloat() / totalHours.coerceAtLeast(1)) * 100f).toInt()

        val backgrounds = BitmapVariants(
            square = WidgetRenderer.renderAtlasCard(
                width = 1080,
                height = 1080,
                startColor = style.cardStart,
                endColor = style.cardEnd,
                glowColor = style.cardGlow,
                borderColor = style.cardBorder
            ),
            wide = WidgetRenderer.renderAtlasCard(
                width = 1500,
                height = 900,
                startColor = style.cardStart,
                endColor = style.cardEnd,
                glowColor = style.cardGlow,
                borderColor = style.cardBorder
            ),
            tall = WidgetRenderer.renderAtlasCard(
                width = 900,
                height = 1500,
                startColor = style.cardStart,
                endColor = style.cardEnd,
                glowColor = style.cardGlow,
                borderColor = style.cardBorder
            )
        )

        val fields = BitmapVariants(
            square = WidgetRenderer.renderAtlasDotField(
                width = 760,
                height = 520,
                totalUnits = totalHours,
                elapsedUnits = elapsedHours,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                columns = 8,
                emphasizeBand = false,
                drawShadow = false
            ),
            wide = WidgetRenderer.renderAtlasDotField(
                width = 1250,
                height = 360,
                totalUnits = totalHours,
                elapsedUnits = elapsedHours,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                columns = 12,
                emphasizeBand = false,
                drawShadow = false
            ),
            tall = WidgetRenderer.renderAtlasDotField(
                width = 760,
                height = 900,
                totalUnits = totalHours,
                elapsedUnits = elapsedHours,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                columns = 6,
                emphasizeBand = false,
                drawShadow = false
            )
        )

        provideContent {
            DayHourDotWidgetContent(
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
private fun DayHourDotWidgetContent(
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
                contentDescription = "Day progress dots",
                modifier = androidx.glance.GlanceModifier.defaultWeight(),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
            WidgetFooter(
                leading = if (compact) "$remainingHours left" else dayLabel,
                trailing = if (compact) null else "Today",
                style = style,
                compact = compact
            )
        }
    }
}

class DayHourWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DayHourWidget()
}
