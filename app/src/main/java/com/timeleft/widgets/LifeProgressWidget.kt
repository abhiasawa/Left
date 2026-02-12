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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
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
            width = 300,
            height = 220,
            totalUnits = lifespan,
            elapsedUnits = yearsLived,
            elapsedColor = 0xFF3A3A3A.toInt(),
            remainingColor = 0xFFFFFFFF.toInt(),
            currentColor = 0xFFFF3B30.toInt(),
            backgroundColor = 0xFF000000.toInt(),
            columns = 10,
            dotRadiusPx = 8f,
            spacingPx = 4f
        )

        provideContent {
            LifeWidgetContent(
                yearsRemaining = yearsRemaining,
                gridBitmap = gridBitmap,
                hasBirthDate = birthDate != null
            )
        }
    }
}

/** Glance composable layout for the life progress widget. */
@Composable
private fun LifeWidgetContent(
    yearsRemaining: Int,
    gridBitmap: Bitmap,
    hasBirthDate: Boolean
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.Black, Color.Black))
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Life",
                style = TextStyle(
                    color = ColorProvider(Color.White, Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = if (hasBirthDate) "$yearsRemaining years left" else "Set birth date in app",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF8E8E93), Color(0xFF8E8E93)),
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Image(
                provider = ImageProvider(gridBitmap),
                contentDescription = "Life progress",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/** Broadcast receiver that binds [LifeProgressWidget] to the Android widget framework. */
class LifeProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LifeProgressWidget()
}
