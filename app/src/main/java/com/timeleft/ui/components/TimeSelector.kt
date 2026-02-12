package com.timeleft.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeleft.domain.models.TimeUnit

@Composable
fun TimeSelector(
    selectedUnit: TimeUnit,
    onUnitSelected: (TimeUnit) -> Unit,
    modifier: Modifier = Modifier,
    showLifeOption: Boolean = true
) {
    val units = if (showLifeOption) TimeUnit.entries else TimeUnit.entries.filter { it != TimeUnit.LIFE }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        units.forEach { unit ->
            val isSelected = unit == selectedUnit
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                } else Color.Transparent,
                animationSpec = tween(200),
                label = "selectorBg"
            )
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onUnitSelected(unit) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
