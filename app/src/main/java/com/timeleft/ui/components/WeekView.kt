package com.timeleft.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Week visualization: full day names stacked vertically.
 *
 * The text IS the visualization. Past days are dimmed,
 * the current day is highlighted in red, future days are bright.
 * No dots, no bars — pure typography communicating time through color.
 */
@Composable
fun WeekView(
    elapsedDays: Int,
    elapsedColor: Color,
    remainingColor: Color,
    currentIndicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val dayNames = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

    val animationProgress = remember(elapsedDays) { Animatable(0f) }
    LaunchedEffect(elapsedDays) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Start
    ) {
        dayNames.forEachIndexed { index, name ->
            val isElapsed = index < elapsedDays
            val isCurrent = index == elapsedDays
            val color = when {
                isCurrent -> currentIndicatorColor
                isElapsed -> elapsedColor
                else -> remainingColor
            }

            val itemAlpha = ((animationProgress.value - index * 0.08f) / 0.5f).coerceIn(0f, 1f)

            Text(
                text = name,
                color = color,
                fontSize = if (isCurrent) 28.sp else 22.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 4.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(itemAlpha)
                    .padding(vertical = 2.dp)
            )
        }
    }
}
