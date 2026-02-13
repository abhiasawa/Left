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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.timeleft.MainActivity
import com.timeleft.data.db.AppDatabase
import com.timeleft.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Time Atlas: Countdown panel as a singularity ring with event beacon.
 */
class CountdownWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val card = style.cardColors(hueShift = 96f, saturationMul = 1.1f, valueMul = 1.05f, glowAlphaBoost = 1.3f)

        val db = AppDatabase.getDatabase(context)
        val dates = db.customDateDao().getAllCustomDates().firstOrNull() ?: emptyList()
        val now = LocalDate.now()

        val entity = dates
            .map { it.toDomain() }
            .filter { !it.isPast }
            .minByOrNull { ChronoUnit.DAYS.between(now, it.endDate) }

        val name = entity?.name ?: "No countdown"
        val remaining = entity?.remainingDays ?: 0
        val total = entity?.totalDays ?: 1
        val elapsed = entity?.elapsedDays ?: 0
        val progress = (elapsed.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)

        val eventColor = try {
            android.graphics.Color.parseColor(entity?.colorHex ?: "#FFFFFF")
        } catch (_: Exception) {
            style.currentColor
        }

        val backgrounds = BitmapVariants(
            square = WidgetRenderer.renderAtlasCard(1080, 1080, card.start, card.end, eventColor, card.border),
            wide = WidgetRenderer.renderAtlasCard(1500, 900, card.start, card.end, eventColor, card.border),
            tall = WidgetRenderer.renderAtlasCard(900, 1500, card.start, card.end, eventColor, card.border)
        )
        val rings = BitmapVariants(
            square = WidgetRenderer.renderAtlasRingField(560, progress, style.elapsedColor, eventColor, style.currentColor, 0x00000000, strokeWidth = 22f),
            wide = WidgetRenderer.renderAtlasRingField(460, progress, style.elapsedColor, eventColor, style.currentColor, 0x00000000, strokeWidth = 18f),
            tall = WidgetRenderer.renderAtlasRingField(620, progress, style.elapsedColor, eventColor, style.currentColor, 0x00000000, strokeWidth = 24f)
        )

        provideContent {
            CountdownWidgetContent(
                context = context,
                name = name,
                remaining = remaining,
                hasCountdown = entity != null,
                backgroundVariants = backgrounds,
                ringVariants = rings,
                style = style
            )
        }
    }
}

@Composable
private fun CountdownWidgetContent(
    context: Context,
    name: String,
    remaining: Int,
    hasCountdown: Boolean,
    backgroundVariants: BitmapVariants,
    ringVariants: BitmapVariants,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 145.dp || size.height < 145.dp
    val short = size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundVariants.forWidgetSize(size),
        contentPadding = if (compact) 7.dp else 10.dp
    ) {
        Column(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
            WidgetHeader(
                title = "Countdown Atlas",
                badge = if (hasCountdown) "LIVE" else "SETUP",
                style = style,
                compact = compact
            )
            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))

            if (hasCountdown) {
                Text(
                    text = name,
                    style = TextStyle(
                        color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 11.sp else 12.sp
                    ),
                    maxLines = 1
                )
                Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 5.dp))
                Box(
                    modifier = androidx.glance.GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.glance.Image(
                        provider = androidx.glance.ImageProvider(ringVariants.forWidgetSize(size)),
                        contentDescription = "Countdown atlas ring",
                        modifier = androidx.glance.GlanceModifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
                WidgetFooter(
                    leading = if (compact) "$remaining left" else "$remaining days left",
                    trailing = if (compact || short) null else "Event beacon",
                    style = style,
                    compact = compact
                )
            } else {
                Spacer(modifier = androidx.glance.GlanceModifier.defaultWeight())
                WidgetHeroMetric(
                    value = "No event",
                    label = if (compact) "Add in app" else "Add countdown in app",
                    style = style,
                    compact = compact
                )
            }
        }
    }
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CountdownWidget()
}
