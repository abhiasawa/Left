package com.timeleft.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.timeleft.MainActivity
import com.timeleft.data.db.AppDatabase
import com.timeleft.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Countdown widget redesigned as a hero ring with event-first typography.
 */
class CountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val baseCard = style.cardColors(hueShift = 88f, saturationMul = 1.08f, valueMul = 1.04f, glowAlphaBoost = 1.25f)

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

        val backgroundBitmap = WidgetRenderer.renderAtmosphericCard(
            width = 1080,
            height = 1080,
            startColor = baseCard.start,
            endColor = baseCard.end,
            glowColor = eventColor,
            borderColor = baseCard.border
        )

        val ringBitmap = WidgetRenderer.renderProgressRing(
            size = 560,
            progress = progress,
            elapsedColor = style.elapsedColor,
            remainingColor = eventColor,
            backgroundColor = 0x00000000,
            strokeWidth = 22f
        )

        provideContent {
            CountdownWidgetContent(
                context = context,
                name = name,
                remaining = remaining,
                hasCountdown = entity != null,
                ringBitmap = ringBitmap,
                backgroundBitmap = backgroundBitmap,
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
    ringBitmap: Bitmap,
    backgroundBitmap: Bitmap,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 130.dp || size.height < 130.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundBitmap
    ) {
        Column {
            WidgetHeader(
                title = "Countdown",
                badge = if (hasCountdown) "LIVE" else "SETUP",
                style = style,
                compact = compact
            )
            Spacer(modifier = GlanceModifier.height(4.dp))

            if (hasCountdown) {
                Text(
                    text = name,
                    style = TextStyle(
                        color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 12.sp else 13.sp
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(ringBitmap),
                        contentDescription = "Countdown ring",
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                WidgetFooter(
                    leading = if (compact) "$remaining left" else "$remaining days left",
                    trailing = "Target",
                    style = style,
                    compact = compact
                )
            } else {
                Spacer(modifier = GlanceModifier.defaultWeight())
                WidgetHeroMetric(
                    value = "No event",
                    label = "Add countdown in app",
                    style = style,
                    compact = compact
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
        }
    }
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}
