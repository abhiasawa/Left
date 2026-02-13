package com.timeleft.widgets

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

@Composable
fun WidgetSurface(
    openAppIntent: Intent,
    backgroundBitmap: Bitmap,
    content: @Composable () -> Unit
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(22.dp)
            .clickable(actionStartActivity(openAppIntent))
    ) {
        Image(
            provider = ImageProvider(backgroundBitmap),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize()
        )
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            content()
        }
    }
}

@Composable
fun WidgetHeader(
    title: String,
    badge: String?,
    style: WidgetVisualStyle,
    compact: Boolean
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = TextStyle(
                color = ColorProvider(Color(style.textSecondary), Color(style.textSecondary)),
                fontSize = if (compact) 9.sp else 10.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        if (badge != null) {
            Box(
                modifier = GlanceModifier
                    .cornerRadius(8.dp)
                    .background(ColorProvider(Color(style.cardBorder), Color(style.cardBorder)))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    style = TextStyle(
                        color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                        fontSize = if (compact) 9.sp else 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun WidgetFooter(
    leading: String,
    trailing: String?,
    style: WidgetVisualStyle,
    compact: Boolean
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = leading,
            style = TextStyle(
                color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1
        )
        if (trailing != null) {
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = trailing,
                style = TextStyle(
                    color = ColorProvider(Color(style.textSecondary), Color(style.textSecondary)),
                    fontSize = if (compact) 9.sp else 10.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun WidgetHeroMetric(
    value: String,
    label: String?,
    style: WidgetVisualStyle,
    compact: Boolean
) {
    Column {
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(Color(style.textPrimary), Color(style.textPrimary)),
                fontSize = if (compact) 18.sp else 21.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        if (label != null) {
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(Color(style.textSecondary), Color(style.textSecondary)),
                    fontSize = if (compact) 9.sp else 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}
