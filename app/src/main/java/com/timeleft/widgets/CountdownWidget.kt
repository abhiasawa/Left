package com.timeleft.widgets

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.timeleft.data.db.AppDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Home screen widget that displays a countdown to the nearest upcoming custom date.
 * Shows a progress ring, the number of days remaining, and the event name.
 * Falls back to a "No countdown" placeholder when no active countdowns exist.
 */
class CountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val dates = db.customDateDao().getAllCustomDates().firstOrNull() ?: emptyList()
        val now = LocalDate.now()

        // Pick the soonest upcoming event so the widget always shows the most relevant countdown
        val entity = dates
            .map { it.toDomain() }
            .filter { !it.isPast }
            .minByOrNull { ChronoUnit.DAYS.between(now, it.endDate) }

        val name = entity?.name ?: "No countdown"
        val remaining = entity?.remainingDays ?: 0
        val total = entity?.totalDays ?: 1
        val elapsed = entity?.elapsedDays ?: 0
        val progress = entity?.progress ?: 0f
        // Gracefully handle invalid or missing color hex values
        val colorInt = try {
            android.graphics.Color.parseColor(entity?.colorHex ?: "#FFFFFF")
        } catch (e: Exception) {
            android.graphics.Color.WHITE
        }

        val ringBitmap = WidgetRenderer.renderProgressRing(
            size = 300,
            progress = progress,
            elapsedColor = 0xFF3A3A3A.toInt(),
            remainingColor = colorInt,
            backgroundColor = android.graphics.Color.TRANSPARENT,
            strokeWidth = 16f
        )

        provideContent {
            CountdownWidgetContent(
                name = name,
                remaining = remaining,
                ringBitmap = ringBitmap,
                hasCountdown = entity != null
            )
        }
    }
}

/** Glance composable layout for the countdown widget. */
@Composable
private fun CountdownWidgetContent(
    name: String,
    remaining: Int,
    ringBitmap: Bitmap,
    hasCountdown: Boolean
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.Black, Color.Black))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCountdown) {
                Image(
                    provider = ImageProvider(ringBitmap),
                    contentDescription = "Countdown progress",
                    modifier = GlanceModifier.size(90.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(
                    text = "$remaining",
                    style = TextStyle(
                        color = ColorProvider(Color.White, Color.White),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "days left",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = name,
                    style = TextStyle(
                        color = ColorProvider(Color.White, Color.White),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            } else {
                Text(
                    text = "No countdown",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Add one in the app",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF555555), Color(0xFF555555)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/** Broadcast receiver that binds [CountdownWidget] to the Android widget framework. */
class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}
