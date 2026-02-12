package com.timeleft.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeleft.domain.models.TimeUnit
import kotlin.math.roundToInt

/**
 * Horizontal pill-bar that lets the user pick a [TimeUnit] (Year, Month, Week, etc.).
 *
 * A translucent pill slides behind the selected label using a bouncy spring
 * animation. Each tap triggers haptic feedback for a tactile feel.
 *
 * The pill's position is tracked via [onGloballyPositioned] on each label,
 * then animated with [animateFloatAsState].
 *
 * @param showLifeOption When false, hides the LIFE option (if no birth date is set).
 */
@Composable
fun TimeSelector(
    selectedUnit: TimeUnit,
    onUnitSelected: (TimeUnit) -> Unit,
    modifier: Modifier = Modifier,
    showLifeOption: Boolean = true
) {
    val units = if (showLifeOption) TimeUnit.entries else TimeUnit.entries.filter { it != TimeUnit.LIFE }
    val selectedIndex = units.indexOf(selectedUnit).coerceAtLeast(0)
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Track the selected item's layout position so the pill can follow it
    var pillX by remember { mutableFloatStateOf(0f) }
    var pillWidth by remember { mutableIntStateOf(0) }
    var pillHeight by remember { mutableIntStateOf(0) }

    val animatedPillX by animateFloatAsState(
        targetValue = pillX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pillX"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Sliding pill indicator
        if (pillWidth > 0) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedPillX.roundToInt(), 0) }
                    .width(with(density) { pillWidth.toDp() })
                    .height(with(density) { pillHeight.toDp() })
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
            )
        }

        // Text items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            units.forEachIndexed { index, unit ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    },
                    animationSpec = tween(200),
                    label = "selectorText"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onUnitSelected(unit)
                        }
                        .onGloballyPositioned { coords ->
                            if (isSelected) {
                                pillX = coords.positionInParent().x
                                pillWidth = coords.size.width
                                pillHeight = coords.size.height
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unit.displayName,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
