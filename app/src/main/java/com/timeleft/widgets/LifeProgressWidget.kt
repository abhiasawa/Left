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
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.timeleft.MainActivity
import com.timeleft.data.preferences.UserPreferencesRepository
import com.timeleft.util.TimeCalculations
import kotlinx.coroutines.flow.first

/**
 * Home screen widget that shows estimated life progress as a dot grid.
 * Each dot represents one year of the user's expected lifespan.
 * Requires a birth date to be set in the app; otherwise displays a prompt.
 */
class LifeProgressWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(prefs)
        val birthDate = prefs.birthDate
        val lifespan = prefs.expectedLifespan

        // Fall back to zero/full when birth date is missing so the grid still renders
        val yearsLived = if (birthDate != null) {
            TimeCalculations.lifeYearsElapsed(birthDate)
        } else 0
        val yearsRemaining = if (birthDate != null) {
            TimeCalculations.lifeYearsRemaining(birthDate, lifespan)
        } else lifespan

        val gridBitmap = WidgetRenderer.renderDotGrid(
            width = 500,
            height = 400,
            totalUnits = lifespan,
            elapsedUnits = yearsLived,
            elapsedColor = style.elapsedColor,
            remainingColor = style.remainingColor,
            currentColor = style.currentColor,
            backgroundColor = 0x00000000,
            columns = 10
        )
        val backgroundBitmap = WidgetRenderer.renderAtmosphericCard(
            width = 900,
            height = 900,
            startColor = style.cardStart,
            endColor = style.cardEnd,
            glowColor = style.cardGlow,
            borderColor = style.cardBorder
        )

        provideContent {
            LifeWidgetContent(
                context = context,
                yearsRemaining = yearsRemaining,
                gridBitmap = gridBitmap,
                hasBirthDate = birthDate != null,
                backgroundBitmap = backgroundBitmap,
                style = style
            )
        }
    }
}

/** Glance composable layout for the life progress widget. */
@Composable
private fun LifeWidgetContent(
    context: Context,
    yearsRemaining: Int,
    gridBitmap: Bitmap,
    hasBirthDate: Boolean,
    backgroundBitmap: Bitmap,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 130.dp || size.height < 130.dp
    val fontSize = if (compact) 10.sp else 11.sp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "LIFE")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(openAppIntent))
    ) {
        Image(
            provider = ImageProvider(backgroundBitmap),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Image(
                provider = ImageProvider(gridBitmap),
                contentDescription = "Life progress",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.Fit
            )
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Life",
                    style = TextStyle(
                        color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = if (hasBirthDate) {
                        if (compact) "$yearsRemaining left" else "$yearsRemaining years left"
                    } else {
                        "Set birth date"
                    },
                    style = TextStyle(
                        color = ColorProvider(Color(style.textSecondary), Color(style.textSecondary)),
                        fontSize = fontSize
                    )
                )
            }
        }
    }
}

/** Broadcast receiver that binds [LifeProgressWidget] to the Android widget framework. */
class LifeProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LifeProgressWidget()
}
