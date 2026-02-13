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
 * Time Atlas: Life panel as a longitudinal age lattice.
 */
class LifeProgressWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(prefs)
        val card = style.cardColors(hueShift = -28f, saturationMul = 1.12f, valueMul = 1.0f, glowAlphaBoost = 1.2f)

        val birthDate = prefs.birthDate
        val lifespan = prefs.expectedLifespan

        val yearsLived = if (birthDate != null) TimeCalculations.lifeYearsElapsed(birthDate) else 0
        val yearsRemaining = if (birthDate != null) TimeCalculations.lifeYearsRemaining(birthDate, lifespan) else lifespan
        val percent = ((yearsLived.toFloat() / lifespan.coerceAtLeast(1)) * 100f).toInt()

        val backgrounds = BitmapVariants(
            square = WidgetRenderer.renderAtlasCard(1080, 1080, card.start, card.end, card.glow, card.border),
            wide = WidgetRenderer.renderAtlasCard(1500, 900, card.start, card.end, card.glow, card.border),
            tall = WidgetRenderer.renderAtlasCard(900, 1500, card.start, card.end, card.glow, card.border)
        )
        val fields = BitmapVariants(
            square = WidgetRenderer.renderAtlasDotField(880, 540, lifespan, yearsLived, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000, columns = 12),
            wide = WidgetRenderer.renderAtlasDotField(1250, 420, lifespan, yearsLived, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000, columns = 16),
            tall = WidgetRenderer.renderAtlasDotField(720, 980, lifespan, yearsLived, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000, columns = 10)
        )

        provideContent {
            LifeWidgetContent(
                context = context,
                yearsRemaining = yearsRemaining,
                hasBirthDate = birthDate != null,
                percent = percent,
                backgroundVariants = backgrounds,
                fieldVariants = fields,
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
    backgroundVariants: BitmapVariants,
    fieldVariants: BitmapVariants,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 145.dp || size.height < 145.dp
    val short = size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "LIFE")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundVariants.forWidgetSize(size),
        contentPadding = if (compact) 7.dp else 10.dp
    ) {
        Column(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
            WidgetHeader(
                title = "Life Atlas",
                badge = "$percent%",
                style = style,
                compact = compact
            )

            if (!compact && !short) {
                Spacer(modifier = androidx.glance.GlanceModifier.height(4.dp))
                WidgetHeroMetric(
                    value = if (hasBirthDate) "$yearsRemaining" else "--",
                    label = if (hasBirthDate) "years remaining" else "set birth date",
                    style = style,
                    compact = compact
                )
            }

            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
            androidx.glance.Image(
                provider = androidx.glance.ImageProvider(fieldVariants.forWidgetSize(size)),
                contentDescription = "Life atlas field",
                modifier = androidx.glance.GlanceModifier.defaultWeight(),
                contentScale = ContentScale.FillBounds
            )
            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
            WidgetFooter(
                leading = if (compact && hasBirthDate) "$yearsRemaining left" else if (hasBirthDate) "Perspective" else "Profile needed",
                trailing = if (compact) null else "Lifespan",
                style = style,
                compact = compact
            )
        }
    }
}

class LifeProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LifeProgressWidget()
}
