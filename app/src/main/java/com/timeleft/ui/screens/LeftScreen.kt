package com.timeleft.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.domain.models.SymbolType
import com.timeleft.domain.models.TimeUnit
import com.timeleft.ui.components.DotGrid
import com.timeleft.ui.components.MonthCalendar
import com.timeleft.ui.components.TimeSelector
import com.timeleft.ui.theme.ThemePack
import com.timeleft.ui.theme.ambientBrush
import com.timeleft.ui.theme.appPalette
import com.timeleft.ui.theme.elapsedDotColor
import com.timeleft.util.TimeCalculations
import java.time.LocalDate

/**
 * Main screen — visual-first timeline with atmospheric backgrounds and unit morphing.
 */
@Composable
fun LeftScreen(
    preferences: UserPreferencesData,
    selectedUnit: TimeUnit,
    onUnitSelected: (TimeUnit) -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbolType = SymbolType.fromString(preferences.symbolType)
    val themePack = ThemePack.fromString(preferences.themePack)
    val palette = appPalette(themePack, preferences.darkMode)
    val elapsedColor = elapsedDotColor(themePack, preferences.darkMode)
    val remainingColor = palette.textPrimary
    val currentIndicatorColor = remainingColor
    val weekElapsedColor = if (isColorClose(elapsedColor, remainingColor)) palette.textSecondary else elapsedColor
    val weekRemainingColor = if (isColorClose(remainingColor, elapsedColor)) palette.textPrimary else remainingColor

    val hasLifeData = preferences.birthDate != null
    val showLifeOption = hasLifeData

    val availableUnits = remember(showLifeOption) {
        if (showLifeOption) TimeUnit.entries else TimeUnit.entries.filter { it != TimeUnit.LIFE }
    }

    val timeData = remember(selectedUnit, preferences) {
        getTimeData(selectedUnit, preferences)
    }

    val haptic = LocalHapticFeedback.current
    var swipeDelta by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 100f

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "screenMotion")
    val ambientDrift by infiniteTransition.animateFloat(
        initialValue = -34f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientDrift"
    )
    val frameBreath by infiniteTransition.animateFloat(
        initialValue = 0.995f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "frameBreath"
    )

    val swipeParallax by animateFloatAsState(
        targetValue = (swipeDelta / 10f).coerceIn(-22f, 22f),
        animationSpec = tween(180),
        label = "swipeParallax"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .pointerInput(selectedUnit, availableUnits) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val currentIndex = availableUnits.indexOf(selectedUnit)
                        if (swipeDelta < -swipeThreshold && currentIndex < availableUnits.lastIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUnitSelected(availableUnits[currentIndex + 1])
                        } else if (swipeDelta > swipeThreshold && currentIndex > 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUnitSelected(availableUnits[currentIndex - 1])
                        }
                        swipeDelta = 0f
                    },
                    onDragCancel = { swipeDelta = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeDelta += dragAmount
                    }
                )
            }
    ) {
        AtmosphericBackdrop(
            themePack = themePack,
            darkTheme = preferences.darkMode,
            selectedUnit = selectedUnit,
            progressPercent = timeData.progressPercent,
            driftOffset = ambientDrift,
            modifier = Modifier.matchParentSize()
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .widthIn(max = 690.dp)
                    .padding(horizontal = 12.dp)
            ) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(460, delayMillis = 40)) +
                        slideInVertically(tween(520, delayMillis = 40), initialOffsetY = { -it / 2 }),
                    exit = fadeOut(tween(180)) + slideOutVertically(tween(180), targetOffsetY = { -it / 6 })
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = timeData.label.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.textSecondary,
                                letterSpacing = 1.8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = timeData.remainingText,
                                style = MaterialTheme.typography.displaySmall,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.2).sp
                            )
                        }
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            palette.surface.copy(alpha = 0.48f),
                                            palette.surfaceVariant.copy(alpha = 0.26f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = palette.textPrimary.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(520, delayMillis = 110)) +
                        slideInVertically(tween(560, delayMillis = 110), initialOffsetY = { -it / 3 }),
                    exit = fadeOut(tween(180))
                ) {
                    TimeSelector(
                        selectedUnit = selectedUnit,
                        onUnitSelected = onUnitSelected,
                        showLifeOption = showLifeOption
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(620, delayMillis = 160)) +
                        scaleIn(tween(620, delayMillis = 160), initialScale = 0.985f),
                    exit = fadeOut(tween(220)) + scaleOut(tween(220), targetScale = 1.01f),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = frameBreath
                                scaleY = frameBreath
                                translationX = swipeParallax
                            }
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        palette.surface.copy(alpha = 0.46f),
                                        palette.surfaceVariant.copy(alpha = 0.3f),
                                        palette.surface.copy(alpha = 0.24f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = palette.textPrimary.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(30.dp)
                            )
                    ) {
                        FrameChrome(
                            highlight = palette.accent,
                            border = palette.border,
                            modifier = Modifier.matchParentSize()
                        )

                        AnimatedContent(
                            targetState = Triple(timeData.total, timeData.elapsed, selectedUnit),
                            transitionSpec = {
                                val fromIndex = availableUnits.indexOf(initialState.third).coerceAtLeast(0)
                                val toIndex = availableUnits.indexOf(targetState.third).coerceAtLeast(0)
                                val direction = if (toIndex >= fromIndex) 1 else -1
                                (slideInHorizontally(
                                    animationSpec = tween(520),
                                    initialOffsetX = { full -> direction * (full / 5) }
                                ) + fadeIn(tween(460)) + scaleIn(initialScale = 0.975f, animationSpec = tween(460))) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = tween(320),
                                        targetOffsetX = { full -> -direction * (full / 7) }
                                    ) + fadeOut(tween(220)) + scaleOut(targetScale = 1.015f, animationSpec = tween(300)))
                            },
                            label = "visualization",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 14.dp)
                        ) { (total, elapsed, unit) ->
                            when (unit) {
                                TimeUnit.MONTH -> MonthCalendar(
                                    totalDays = total,
                                    elapsedDays = elapsed,
                                    symbolType = symbolType,
                                    elapsedColor = elapsedColor,
                                    remainingColor = remainingColor,
                                    currentIndicatorColor = currentIndicatorColor,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                TimeUnit.WEEK -> DotGrid(
                                    totalUnits = total,
                                    elapsedUnits = elapsed,
                                    symbolType = symbolType,
                                    elapsedColor = weekElapsedColor,
                                    remainingColor = weekRemainingColor,
                                    currentIndicatorColor = currentIndicatorColor,
                                    columns = 7,
                                    fillAvailableSpace = true,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                )
                                TimeUnit.DAY -> DotGrid(
                                    totalUnits = total,
                                    elapsedUnits = elapsed,
                                    symbolType = symbolType,
                                    elapsedColor = elapsedColor,
                                    remainingColor = remainingColor,
                                    currentIndicatorColor = currentIndicatorColor,
                                    columns = when {
                                        total >= 20 -> 8
                                        total >= 14 -> 7
                                        else -> 6
                                    },
                                    fillAvailableSpace = true,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                                TimeUnit.HOUR -> DotGrid(
                                    totalUnits = total,
                                    elapsedUnits = elapsed,
                                    symbolType = symbolType,
                                    elapsedColor = elapsedColor,
                                    remainingColor = remainingColor,
                                    currentIndicatorColor = currentIndicatorColor,
                                    columns = 10,
                                    fillAvailableSpace = true,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                                TimeUnit.LIFE -> DotGrid(
                                    totalUnits = total,
                                    elapsedUnits = elapsed,
                                    symbolType = symbolType,
                                    elapsedColor = elapsedColor,
                                    remainingColor = remainingColor,
                                    currentIndicatorColor = currentIndicatorColor,
                                    columns = 52,
                                    fillAvailableSpace = true,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 2.dp)
                                )
                                else -> DotGrid(
                                    totalUnits = total,
                                    elapsedUnits = elapsed,
                                    symbolType = symbolType,
                                    elapsedColor = elapsedColor,
                                    remainingColor = remainingColor,
                                    currentIndicatorColor = currentIndicatorColor,
                                    fillAvailableSpace = true,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(560, delayMillis = 230)) +
                        slideInVertically(tween(560, delayMillis = 230), initialOffsetY = { it / 4 }),
                    exit = fadeOut(tween(180))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${timeData.progressPercent.toInt()}% elapsed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        )

                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            palette.surface.copy(alpha = 0.46f),
                                            palette.surfaceVariant.copy(alpha = 0.24f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = palette.textPrimary.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun FrameChrome(
    highlight: Color,
    border: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.5.dp.toPx()
        val corner = 30.dp.toPx()

        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = size.height * 0.56f
            ),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
        )

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    highlight.copy(alpha = 0.16f),
                    Color.Transparent,
                    highlight.copy(alpha = 0.08f)
                )
            ),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
        )

        drawRoundRect(
            color = border.copy(alpha = 0.58f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = Stroke(width = strokeWidth)
        )

        val inset = 3.dp.toPx()
        drawRoundRect(
            color = Color.White.copy(alpha = 0.11f),
            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(
                width = size.width - inset * 2,
                height = size.height - inset * 2
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner - inset, corner - inset),
            style = Stroke(width = 0.9.dp.toPx())
        )

        val arcRect = androidx.compose.ui.geometry.Rect(
            left = size.width * 0.08f,
            top = size.height * 0.08f,
            right = size.width * 0.92f,
            bottom = size.height * 0.92f
        )
        drawArc(
            color = highlight.copy(alpha = 0.26f),
            startAngle = -42f,
            sweepAngle = 84f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                start = androidx.compose.ui.geometry.Offset(size.width * 0.55f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height)
            ),
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.52f, -size.height * 0.05f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.height * 0.72f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.2f, size.width * 0.2f)
        )
    }
}

@Composable
private fun AtmosphericBackdrop(
    themePack: ThemePack,
    darkTheme: Boolean,
    selectedUnit: TimeUnit,
    progressPercent: Float,
    driftOffset: Float,
    modifier: Modifier = Modifier
) {
    val progress = (progressPercent / 100f).coerceIn(0f, 1f)
    val palette = appPalette(themePack, darkTheme)
    val glowColor = lerp(palette.accent, palette.textPrimary, 0.22f)

    Box(
        modifier = modifier
            .background(ambientBrush(themePack, darkTheme, selectedUnit))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val maxDim = size.maxDimension
            val centerX = size.width * (0.25f + progress * 0.5f) + driftOffset
            val centerY = size.height * (0.2f + (1f - progress) * 0.5f)

            drawCircle(
                color = glowColor.copy(alpha = if (darkTheme) 0.2f else 0.1f),
                radius = maxDim * 0.42f,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY)
            )
            drawCircle(
                color = palette.ambientEnd.copy(alpha = if (darkTheme) 0.14f else 0.08f),
                radius = maxDim * 0.58f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.8f - (driftOffset * 0.35f), size.height * 0.85f)
            )

            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (darkTheme) 0.08f else 0.14f),
                        Color.Transparent,
                        Color.White.copy(alpha = if (darkTheme) 0.03f else 0.06f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(-size.width * 0.08f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.62f, size.height)
                ),
                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.04f + driftOffset * 0.2f, -size.height * 0.08f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.52f, size.height * 0.92f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.24f, size.width * 0.24f)
            )

            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = if (darkTheme) 0.06f else 0.1f),
                        Color.Transparent
                    ),
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.62f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                ),
                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.66f - driftOffset * 0.1f, -size.height * 0.02f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.28f, size.height * 0.78f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.18f, size.width * 0.18f)
            )
        }
    }
}

/** Presentation model consumed by [LeftScreen] to render the caption and grid. */
data class TimeData(
    val total: Int,
    val elapsed: Int,
    val label: String,
    val remainingText: String,
    val progressPercent: Float
)

/** Maps a [TimeUnit] + user preferences into a [TimeData] snapshot. */
fun getTimeData(unit: TimeUnit, preferences: UserPreferencesData): TimeData {
    return when (unit) {
        TimeUnit.LIFE -> {
            val birthDate = preferences.birthDate ?: LocalDate.of(1990, 1, 1)
            val lifespan = preferences.expectedLifespan
            val totalWeeks = lifespan * 52
            val elapsedWeeks = TimeCalculations.lifeWeeksElapsed(birthDate)
            val remainingYears = TimeCalculations.lifeYearsRemaining(birthDate, lifespan)
            TimeData(
                totalWeeks,
                elapsedWeeks,
                "Life",
                "$remainingYears yrs left",
                if (totalWeeks > 0) (elapsedWeeks.toFloat() / totalWeeks) * 100f else 0f
            )
        }

        TimeUnit.YEAR -> {
            val total = TimeCalculations.totalDaysInYear()
            val elapsed = TimeCalculations.daysElapsedInYear()
            val remaining = TimeCalculations.daysLeftInYear()
            TimeData(total, elapsed, TimeCalculations.yearLabel(), "$remaining left", (elapsed.toFloat() / total) * 100f)
        }

        TimeUnit.MONTH -> {
            val total = TimeCalculations.totalDaysInMonth()
            val elapsed = TimeCalculations.daysElapsedInMonth()
            val remaining = TimeCalculations.daysLeftInMonth()
            TimeData(total, elapsed, TimeCalculations.monthLabel(), "$remaining left", (elapsed.toFloat() / total) * 100f)
        }

        TimeUnit.WEEK -> {
            val total = TimeCalculations.totalDaysInWeek()
            val elapsed = TimeCalculations.daysElapsedInWeek()
            val remaining = TimeCalculations.daysLeftInWeek()
            TimeData(total, elapsed, TimeCalculations.weekLabel(), "$remaining left", (elapsed.toFloat() / total) * 100f)
        }

        TimeUnit.DAY -> {
            val start = preferences.activeHoursStart
            val end = preferences.activeHoursEnd
            val total = TimeCalculations.totalHoursInDay(start, end)
            val elapsed = TimeCalculations.hoursElapsedInDay(start)
            val remaining = TimeCalculations.hoursLeftInDay(start, end)
            TimeData(
                total,
                elapsed,
                TimeCalculations.dayLabel(),
                "$remaining left",
                if (total > 0) (elapsed.toFloat() / total) * 100f else 0f
            )
        }

        TimeUnit.HOUR -> {
            val total = TimeCalculations.totalMinutesInHour()
            val elapsed = TimeCalculations.minutesElapsedInHour()
            val remaining = TimeCalculations.minutesLeftInHour()
            TimeData(total, elapsed, TimeCalculations.hourLabel(), "$remaining left", (elapsed.toFloat() / total) * 100f)
        }
    }
}

private fun isColorClose(a: Color, b: Color, threshold: Float = 0.18f): Boolean {
    val dr = a.red - b.red
    val dg = a.green - b.green
    val db = a.blue - b.blue
    val distance = kotlin.math.sqrt(dr * dr + dg * dg + db * db)
    return distance < threshold
}
