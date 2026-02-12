package com.timeleft.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.timeleft.domain.models.SymbolType

/**
 * Flow-row grid of tappable symbol previews.
 *
 * Each symbol shows its Unicode glyph (or "12" for the number type).
 * The selected chip gets a highlighted background + border with a smooth color animation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymbolPicker(
    selectedSymbol: SymbolType,
    onSymbolSelected: (SymbolType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Symbol",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SymbolType.entries.forEach { symbol ->
                val isSelected = symbol == selectedSymbol
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                    } else Color.Transparent,
                    animationSpec = tween(200),
                    label = "symbolBg"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .then(
                            if (isSelected) Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            ) else Modifier
                        )
                        .clickable { onSymbolSelected(symbol) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (symbol == SymbolType.WORD) "12" else symbol.symbol,
                        fontSize = if (symbol == SymbolType.WORD) 14.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

/**
 * Row of circular color swatches. The selected swatch has a thicker border.
 * Used in the Settings sheet for choosing elapsed / remaining dot colors.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(
    label: String,
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.onBackground,
                                CircleShape
                            ) else Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                CircleShape
                            )
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}
