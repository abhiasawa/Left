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
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
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
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Home screen widget that displays a countdown to the nearest upcoming custom date.
 * Shows a dot grid where remaining dots use the event's accent color.
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
        // Gracefully handle invalid or missing color hex values
        val colorInt = try {
            android.graphics.Color.parseColor(entity?.colorHex ?: "#FFFFFF")
        } catch (e: Exception) {
            android.graphics.Color.WHITE
        }

        // Dot grid: elapsed dots are dark, remaining dots use the event's accent color
        val gridBitmap = WidgetRenderer.renderDotGrid(
            width = 600,
            height = 400,
            totalUnits = total,
            elapsedUnits = elapsed,
            elapsedColor = 0xFF333333.toInt(),
            remainingColor = colorInt,
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0x00000000
        )

        provideContent {
            CountdownWidgetContent(
                context = context,
                name = name,
                remaining = remaining,
                gridBitmap = gridBitmap,
                hasCountdown = entity != null
            )
        }
    }
}

/** Glance composable layout for the countdown widget. */
@Composable
private fun CountdownWidgetContent(
    context: Context,
    name: String,
    remaining: Int,
    gridBitmap: Bitmap,
    hasCountdown: Boolean
) {
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(ColorProvider(Color(0xD91C1C1E), Color(0xD91C1C1E)))
            .padding(10.dp)
            .clickable(actionStartActivity(openAppIntent))
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (hasCountdown) {
                Image(
                    provider = ImageProvider(gridBitmap),
                    contentDescription = "Countdown progress",
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    contentScale = ContentScale.Fit
                )
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = ColorProvider(Color.White, Color.White),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "$remaining days left",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                            fontSize = 11.sp
                        )
                    )
                }
            } else {
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "No countdown",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
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
