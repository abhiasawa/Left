package com.timeleft.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.domain.models.SymbolType
import com.timeleft.ui.components.ColorPicker
import com.timeleft.ui.components.SymbolPicker
import com.timeleft.ui.theme.PresetColors
import com.timeleft.ui.theme.PresetElapsedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    preferences: UserPreferencesData,
    onDismiss: () -> Unit,
    onSymbolChanged: (SymbolType) -> Unit,
    onElapsedColorChanged: (String) -> Unit,
    onRemainingColorChanged: (String) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onDailyNotificationChanged: (Boolean) -> Unit,
    onMilestoneNotificationChanged: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Symbol picker
            SymbolPicker(
                selectedSymbol = SymbolType.fromString(preferences.symbolType),
                onSymbolSelected = onSymbolChanged
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Remaining color
            ColorPicker(
                label = "Remaining Color",
                colors = PresetColors,
                selectedColor = parseColor(preferences.remainingColor),
                onColorSelected = { color ->
                    onRemainingColorChanged(colorToHex(color))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Elapsed color
            ColorPicker(
                label = "Elapsed Color",
                colors = PresetElapsedColors,
                selectedColor = parseColor(preferences.elapsedColor),
                onColorSelected = { color ->
                    onElapsedColorChanged(colorToHex(color))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Dark mode toggle
            SectionLabel("APPEARANCE")
            Spacer(modifier = Modifier.height(8.dp))
            SettingsToggle(
                label = "Dark Mode",
                checked = preferences.darkMode,
                onCheckedChange = onDarkModeChanged
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications
            SectionLabel("NOTIFICATIONS")
            Spacer(modifier = Modifier.height(8.dp))
            SettingsToggle(
                label = "Enable Notifications",
                checked = preferences.notificationsEnabled,
                onCheckedChange = onNotificationsChanged
            )
            if (preferences.notificationsEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggle(
                    label = "Daily Update",
                    subtitle = "Get a daily notification with days left in the year",
                    checked = preferences.dailyNotificationEnabled,
                    onCheckedChange = onDailyNotificationChanged
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggle(
                    label = "Milestones",
                    subtitle = "Notify at 25%, 50%, 75%, and 90% of the year",
                    checked = preferences.milestoneNotificationEnabled,
                    onCheckedChange = onMilestoneNotificationChanged
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.onBackground,
                uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
        )
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.White
    }
}

private fun colorToHex(color: Color): String {
    return String.format(
        "#%02X%02X%02X",
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )
}
