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
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CountdownWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val style = darkDotWidgetStyle()

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
        val orbitTotal = 24
        val orbitElapsed = (progress * orbitTotal).toInt().coerceIn(0, orbitTotal)

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
            square = WidgetRenderer.renderAtlasOrbitField(
                width = 620,
                height = 620,
                totalUnits = orbitTotal,
                elapsedUnits = orbitElapsed,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                emphasizeEvery = 6,
                drawShadow = false
            ),
            wide = WidgetRenderer.renderAtlasOrbitField(
                width = 460,
                height = 460,
                totalUnits = orbitTotal,
                elapsedUnits = orbitElapsed,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                emphasizeEvery = 6,
                drawShadow = false
            ),
            tall = WidgetRenderer.renderAtlasOrbitField(
                width = 680,
                height = 680,
                totalUnits = orbitTotal,
                elapsedUnits = orbitElapsed,
                elapsedColor = style.elapsedColor,
                remainingColor = style.remainingColor,
                currentColor = style.currentColor,
                backgroundColor = 0x00000000,
                emphasizeEvery = 6,
                drawShadow = false
            )
        )

        provideContent {
            CountdownDotWidgetContent(
                context = context,
                name = name,
                remaining = remaining,
                hasCountdown = entity != null,
                backgroundVariants = backgrounds,
                orbitVariants = fields,
                style = style
            )
        }
    }
}

@Composable
private fun CountdownDotWidgetContent(
    context: Context,
    name: String,
    remaining: Int,
    hasCountdown: Boolean,
    backgroundVariants: BitmapVariants,
    orbitVariants: BitmapVariants,
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
                title = "Countdown",
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
                        provider = androidx.glance.ImageProvider(orbitVariants.forWidgetSize(size)),
                        contentDescription = "Countdown dots",
                        modifier = androidx.glance.GlanceModifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
                WidgetFooter(
                    leading = if (compact) "$remaining left" else "$remaining days left",
                    trailing = if (compact || short) null else "Event",
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
