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
 * Year widget with an editorial composition: headline metric + dense day field.
 */
class YearProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val card = style.cardColors(hueShift = 10f, saturationMul = 1.08f, valueMul = 1.02f, glowAlphaBoost = 1.16f)

        val totalDays = TimeCalculations.totalDaysInYear()
        val elapsed = TimeCalculations.daysElapsedInYear()
        val remaining = TimeCalculations.daysLeftInYear()
        val year = TimeCalculations.yearLabel()
        val percent = ((elapsed.toFloat() / totalDays) * 100f).toInt()

        val gridBitmap = WidgetRenderer.renderDotGrid(
            width = 920,
            height = 470,
            totalUnits = totalDays,
            elapsedUnits = elapsed,
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
            YearWidgetContent(
                context = context,
                year = year,
                remaining = remaining,
                percent = percent,
                gridBitmap = gridBitmap,
                backgroundBitmap = backgroundBitmap,
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
    gridBitmap: Bitmap,
    backgroundBitmap: Bitmap,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 130.dp || size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "YEAR")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundBitmap
    ) {
        Column {
            WidgetHeader(
                title = "Year",
                badge = "$percent%",
                style = style,
                compact = compact
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            WidgetHeroMetric(
                value = "$remaining",
                label = if (compact) "days left" else "days remaining",
                style = style,
                compact = compact
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            Image(
                provider = ImageProvider(gridBitmap),
                contentDescription = "Year progress field",
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .padding(horizontal = 2.dp),
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = GlanceModifier.height(3.dp))
            WidgetFooter(
                leading = year,
                trailing = if (compact) "Left" else "Left timeline",
                style = style,
                compact = compact
            )
        }
    }
}

class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearProgressWidget()
}
