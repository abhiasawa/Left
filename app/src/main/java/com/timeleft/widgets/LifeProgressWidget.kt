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
 * Life widget rendered as a decade lattice with a strong remaining-years hero metric.
 */
class LifeProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(prefs)
        val card = style.cardColors(hueShift = -22f, saturationMul = 1.12f, valueMul = 0.98f, glowAlphaBoost = 1.2f)

        val birthDate = prefs.birthDate
        val lifespan = prefs.expectedLifespan

        val yearsLived = if (birthDate != null) {
            TimeCalculations.lifeYearsElapsed(birthDate)
        } else 0
        val yearsRemaining = if (birthDate != null) {
            TimeCalculations.lifeYearsRemaining(birthDate, lifespan)
        } else lifespan
        val percent = ((yearsLived.toFloat() / lifespan.coerceAtLeast(1)) * 100f).toInt()

        val gridBitmap = WidgetRenderer.renderDotGrid(
            width = 760,
            height = 440,
            totalUnits = lifespan,
            elapsedUnits = yearsLived,
            elapsedColor = style.elapsedColor,
            remainingColor = style.remainingColor,
            currentColor = style.currentColor,
            backgroundColor = 0x00000000,
            columns = 12
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
            LifeWidgetContent(
                context = context,
                yearsRemaining = yearsRemaining,
                hasBirthDate = birthDate != null,
                percent = percent,
                gridBitmap = gridBitmap,
                backgroundBitmap = backgroundBitmap,
                style = style
            )
        }
    }
}

@Composable
private fun LifeWidgetContent(
    context: Context,
    yearsRemaining: Int,
    hasBirthDate: Boolean,
    percent: Int,
    gridBitmap: Bitmap,
    backgroundBitmap: Bitmap,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 130.dp || size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "LIFE")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundBitmap
    ) {
        Column {
            WidgetHeader(
                title = "Life",
                badge = "$percent%",
                style = style,
                compact = compact
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            WidgetHeroMetric(
                value = if (hasBirthDate) "$yearsRemaining" else "--",
                label = if (hasBirthDate) {
                    if (compact) "years left" else "years remaining"
                } else {
                    "set birth date"
                },
                style = style,
                compact = compact
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Image(
                provider = ImageProvider(gridBitmap),
                contentDescription = "Life progress lattice",
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            WidgetFooter(
                leading = if (hasBirthDate) "Perspective" else "Profile needed",
                trailing = if (compact) null else "Lifespan",
                style = style,
                compact = compact
            )
        }
    }
}

class LifeProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LifeProgressWidget()
}
