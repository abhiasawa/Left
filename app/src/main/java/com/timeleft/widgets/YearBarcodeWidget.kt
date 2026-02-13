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
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.timeleft.MainActivity
import com.timeleft.data.preferences.UserPreferencesRepository
import com.timeleft.util.TimeCalculations
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Time Atlas: Year barcode panel as a scanline corridor.
 */
class YearBarcodeWidget : AtlasWidgetBase() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesRepository(context).preferences.first()
        val style = widgetVisualStyle(preferences)
        val card = style.cardColors(hueShift = 236f, saturationMul = 1.08f, valueMul = 0.98f, glowAlphaBoost = 1.3f)

        val totalDays = TimeCalculations.totalDaysInYear()
        val elapsed = TimeCalculations.daysElapsedInYear()
        val remaining = TimeCalculations.daysLeftInYear()
        val percent = ((elapsed.toFloat() / totalDays.coerceAtLeast(1)) * 100f).toInt()

        val now = LocalDate.now()
        val dayOfWeek = now.format(DateTimeFormatter.ofPattern("EEE"))
        val dayNum = now.dayOfMonth.toString()
        val month = now.format(DateTimeFormatter.ofPattern("MMM"))
        val year = now.year.toString()

        val backgrounds = BitmapVariants(
            square = WidgetRenderer.renderAtlasCard(1080, 1080, card.start, card.end, card.glow, card.border),
            wide = WidgetRenderer.renderAtlasCard(1500, 900, card.start, card.end, card.glow, card.border),
            tall = WidgetRenderer.renderAtlasCard(900, 1500, card.start, card.end, card.glow, card.border)
        )
        val fields = BitmapVariants(
            square = WidgetRenderer.renderAtlasBarcodeField(960, 460, totalDays, elapsed, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000),
            wide = WidgetRenderer.renderAtlasBarcodeField(1300, 360, totalDays, elapsed, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000),
            tall = WidgetRenderer.renderAtlasBarcodeField(760, 820, totalDays, elapsed, style.elapsedColor, style.remainingColor, style.currentColor, 0x00000000)
        )

        provideContent {
            BarcodeWidgetContent(
                context = context,
                dayOfWeek = dayOfWeek,
                dayNum = dayNum,
                month = month,
                year = year,
                remaining = remaining,
                percent = percent,
                backgroundVariants = backgrounds,
                fieldVariants = fields,
                style = style
            )
        }
    }
}

@Composable
private fun BarcodeWidgetContent(
    context: Context,
    dayOfWeek: String,
    dayNum: String,
    month: String,
    year: String,
    remaining: Int,
    percent: Int,
    backgroundVariants: BitmapVariants,
    fieldVariants: BitmapVariants,
    style: WidgetVisualStyle
) {
    val size = LocalSize.current
    val compact = size.width < 220.dp || size.height < 120.dp
    val short = size.height < 105.dp

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        putExtra("time_unit", "YEAR")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    WidgetSurface(
        openAppIntent = openAppIntent,
        backgroundBitmap = backgroundVariants.forWidgetSize(size),
        contentPadding = if (compact) 7.dp else 10.dp
    ) {
        Column(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
            WidgetHeader(
                title = "$dayOfWeek $dayNum $month",
                badge = "$percent%",
                style = style,
                compact = compact
            )

            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 5.dp))
            androidx.glance.Image(
                provider = androidx.glance.ImageProvider(fieldVariants.forWidgetSize(size)),
                contentDescription = "Year atlas barcode",
                modifier = androidx.glance.GlanceModifier.defaultWeight(),
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = androidx.glance.GlanceModifier.height(if (compact) 2.dp else 4.dp))
            Row(modifier = androidx.glance.GlanceModifier.fillMaxWidth()) {
                Text(
                    text = if (compact) "$remaining left" else "$remaining days left",
                    style = TextStyle(
                        color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = androidx.glance.GlanceModifier.defaultWeight(),
                    maxLines = 1
                )
                if (!short) {
                    Text(
                        text = year,
                        style = TextStyle(
                            color = ColorProvider(Color(style.textSecondary), Color(style.textSecondary)),
                            fontSize = if (compact) 10.sp else 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

class YearBarcodeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = YearBarcodeWidget()
}
