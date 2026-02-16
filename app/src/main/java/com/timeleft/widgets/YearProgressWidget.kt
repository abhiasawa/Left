package com.timeleft.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.timeleft.MainActivity
import com.timeleft.util.TimeCalculations

class YearProgressWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val style = darkDotWidgetStyle()

        val totalDays = TimeCalculations.totalDaysInYear()
        val elapsed = TimeCalculations.daysElapsedInYear()
        val remaining = TimeCalculations.daysLeftInYear()
        val year = TimeCalculations.yearLabel()
        val percent = ((elapsed.toFloat() / totalDays.coerceAtLeast(1)) * 100f).toInt()

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
                width = 920,
                height = 520,
                totalUnits = totalDays,
                elapsedUnits = elapsed,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                emphasizeBand = false,
                drawShadow = false
            ),
            wide = WidgetRenderer.renderAtlasDotField(
                width = 1280,
                height = 430,
                totalUnits = totalDays,
                elapsedUnits = elapsed,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                emphasizeBand = false,
                drawShadow = false
            ),
            tall = WidgetRenderer.renderAtlasDotField(
                width = 760,
                height = 980,
                totalUnits = totalDays,
                elapsedUnits = elapsed,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                emphasizeBand = false,
                drawShadow = false
            )
        )

        provideContent {
            YearDotWidgetContent(
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
private fun YearDotWidgetContent(
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
                contentDescription = "Year progress dots",
                modifier = androidx.glance.GlanceModifier.defaultWeight(),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
            Row(modifier = androidx.glance.GlanceModifier.fillMaxWidth()) {
                Text(
                    text = if (compact) "$remaining left" else "$remaining days left",
                    style = TextStyle(
                        color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = androidx.glance.GlanceModifier.defaultWeight(),
                    maxLines = 1
                )
                if (!short) {
                    Text(
                        text = year,
                        style = TextStyle(
                            color = ColorProvider(Color(style.textSecondary), Color(style.textSecondary)),
                            fontSize = if (compact) 10.sp else 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = YearProgressWidget()
}
