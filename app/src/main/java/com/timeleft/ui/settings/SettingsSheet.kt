package com.timeleft.ui.settings

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.domain.models.SymbolType
import com.timeleft.ui.components.ColorPicker
import com.timeleft.ui.components.DotGrid
import com.timeleft.ui.components.SymbolPicker
import com.timeleft.ui.theme.ThemePack
import com.timeleft.ui.theme.appPalette
import com.timeleft.ui.theme.elapsedDotColor
import com.timeleft.ui.theme.themeElapsedColorDefaults
import com.timeleft.ui.theme.themeRemainingColorDefaults
import com.timeleft.util.TimeCalculations
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Modal bottom sheet for app customization.
 *
 * Sections (top -> bottom):
 * - **Preview** -- live 40-dot grid reflecting current symbol + colors.
 * - **Symbol** -- shape picker (dot, star, heart, etc.).
 * - **Colors** -- remaining + elapsed color swatches.
 * - **Profile (Personal)** -- name, birth date, gender, country.
 * - **Life Expectancy** -- lifespan slider (40-120 years).
 * - **Active Hours** -- start/end hour sliders for the Day view.
 * - **Appearance** -- dark-mode toggle.
 * - **Notifications** -- master toggle plus daily / milestone sub-toggles.
 *
 * Opened via long-press on the main [LeftScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    preferences: UserPreferencesData,
    onDismiss: () -> Unit,
    onSymbolChanged: (SymbolType) -> Unit,
    onElapsedColorChanged: (String) -> Unit,
    onRemainingColorChanged: (String) -> Unit,
    onThemePackChanged: (ThemePack) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onDailyNotificationChanged: (Boolean) -> Unit,
    onMilestoneNotificationChanged: (Boolean) -> Unit,
    onBirthDateChanged: (LocalDate) -> Unit,
    onGenderChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onLifespanChanged: (Int) -> Unit,
    onNameChanged: (String) -> Unit,
    onActiveHoursChanged: (Int, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val currentSymbol = SymbolType.fromString(preferences.symbolType)
    val selectedThemePack = ThemePack.fromString(preferences.themePack)
    val palette = appPalette(selectedThemePack, preferences.darkMode)
    val elapsedColor = elapsedDotColor(selectedThemePack, preferences.darkMode)
    val remainingColor = palette.textPrimary

    val birthDate = preferences.birthDate
    val gender = preferences.gender
    val country = preferences.country
    val lifespan = preferences.expectedLifespan

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.46f),
        tonalElevation = 0.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(26.dp)
                )
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Preview ────────────────────────────────────────────────────
            SectionLabel("PREVIEW")
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f),
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp)
            ) {
                DotGrid(
                    totalUnits = 40,
                    elapsedUnits = 18,
                    symbolType = currentSymbol,
                    elapsedColor = elapsedColor,
                    remainingColor = remainingColor,
                    dotSize = 7.dp,
                    spacing = 2.dp,
                    showCurrentIndicator = true,
                    animateOnChange = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Theme Pack ────────────────────────────────────────────────
            SectionLabel("THEME PACK")
            Spacer(modifier = Modifier.height(8.dp))
            ThemePackPicker(
                selected = selectedThemePack,
                onSelected = onThemePackChanged
            )

            Spacer(modifier = Modifier.height(20.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Symbol picker ──────────────────────────────────────────────
            SectionLabel("SYMBOL")
            Spacer(modifier = Modifier.height(8.dp))
            SymbolPicker(
                selectedSymbol = currentSymbol,
                onSymbolSelected = onSymbolChanged
            )

            Spacer(modifier = Modifier.height(20.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Colors ─────────────────────────────────────────────────────
            SectionLabel("COLORS")
            Spacer(modifier = Modifier.height(12.dp))

            ColorPicker(
                label = "Remaining",
                colors = themeRemainingColorDefaults(selectedThemePack),
                selectedColor = remainingColor,
                onColorSelected = { color ->
                    onRemainingColorChanged(colorToHex(color))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ColorPicker(
                label = "Elapsed",
                colors = themeElapsedColorDefaults(selectedThemePack),
                selectedColor = elapsedColor,
                onColorSelected = { color ->
                    onElapsedColorChanged(colorToHex(color))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Profile (Personal) ─────────────────────────────────────────
            SectionLabel("PERSONAL")
            Spacer(modifier = Modifier.height(8.dp))

            GlassSection {
                // Name field with local state for responsive typing
                var localName by remember(preferences.userName) {
                    mutableStateOf(preferences.userName)
                }

                OutlinedTextField(
                    value = localName,
                    onValueChange = { localName = it; onNameChanged(it) },
                    label = { Text("Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        focusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        cursorColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Birth date
                val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
                val birthDateText = birthDate?.format(dateFormatter) ?: "Select date"

                Text(
                    text = "DATE OF BIRTH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                GlassField(
                    text = birthDateText,
                    placeholder = birthDate == null,
                    onClick = {
                        val initial = birthDate ?: LocalDate.of(1990, 1, 1)
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                onBirthDateChanged(LocalDate.of(year, month + 1, day))
                            },
                            initial.year,
                            initial.monthValue - 1,
                            initial.dayOfMonth
                        ).apply {
                            datePicker.maxDate = System.currentTimeMillis()
                        }.show()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gender
                var genderExpanded by remember { mutableStateOf(false) }
                val genderOptions = listOf("Male", "Female", "Other")

                Text(
                    text = "GENDER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    GlassField(
                        text = gender.ifEmpty { "Select gender" },
                        placeholder = gender.isEmpty(),
                        onClick = { genderExpanded = true }
                    )
                    DropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onGenderChanged(option)
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Country
                var countryExpanded by remember { mutableStateOf(false) }

                Text(
                    text = "COUNTRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    GlassField(
                        text = country.ifEmpty { "Select country" },
                        placeholder = country.isEmpty(),
                        onClick = { countryExpanded = true }
                    )
                    DropdownMenu(
                        expanded = countryExpanded,
                        onDismissRequest = { countryExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        TimeCalculations.availableCountries.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    onCountryChanged(c)
                                    countryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Life Expectancy ────────────────────────────────────────────
            SectionLabel("LIFE EXPECTANCY")
            Spacer(modifier = Modifier.height(8.dp))

            GlassSection {
                if (gender.isNotEmpty() && country.isNotEmpty()) {
                    val estimated = TimeCalculations.estimateLifeExpectancy(gender, country)
                    Text(
                        text = "Estimated: $estimated years ($country, $gender)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                var sliderValue by remember(lifespan) {
                    mutableFloatStateOf(lifespan.toFloat())
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expected lifespan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${sliderValue.toInt()} years",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onLifespanChanged(sliderValue.toInt()) },
                    valueRange = 40f..120f,
                    steps = 79,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Active Hours ───────────────────────────────────────────────
            SectionLabel("ACTIVE HOURS")
            Spacer(modifier = Modifier.height(8.dp))

            GlassSection {
                Text(
                    text = "Used for the Day view to show only waking hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                var startHour by remember(preferences.activeHoursStart) {
                    mutableFloatStateOf(preferences.activeHoursStart.toFloat())
                }
                var endHour by remember(preferences.activeHoursEnd) {
                    mutableFloatStateOf(preferences.activeHoursEnd.toFloat())
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Start: ${formatHour(startHour.toInt())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "End: ${formatHour(endHour.toInt())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Slider(
                    value = startHour,
                    onValueChange = { startHour = it },
                    onValueChangeFinished = {
                        onActiveHoursChanged(startHour.toInt(), endHour.toInt())
                    },
                    valueRange = 0f..23f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Slider(
                    value = endHour,
                    onValueChange = { endHour = it },
                    onValueChangeFinished = {
                        onActiveHoursChanged(startHour.toInt(), endHour.toInt())
                    },
                    valueRange = 1f..24f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Appearance ─────────────────────────────────────────────────
            SectionLabel("APPEARANCE")
            Spacer(modifier = Modifier.height(8.dp))
            SettingsToggle(
                label = "Dark Mode",
                checked = preferences.darkMode,
                onCheckedChange = onDarkModeChanged
            )

            Spacer(modifier = Modifier.height(20.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // ── Notifications ──────────────────────────────────────────────
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
                    subtitle = "Daily notification with days left in the year",
                    checked = preferences.dailyNotificationEnabled,
                    onCheckedChange = onDailyNotificationChanged
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggle(
                    label = "Milestones",
                    subtitle = "Notify at 25%, 50%, 75%, and 90%",
                    checked = preferences.milestoneNotificationEnabled,
                    onCheckedChange = onMilestoneNotificationChanged
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** Glass container for form sections. */
@Composable
private fun GlassSection(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

/** Glass-styled field (dropdown/date picker trigger). */
@Composable
private fun GlassField(
    text: String,
    placeholder: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = if (!placeholder)
                    MaterialTheme.colorScheme.onBackground
                else
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
        thickness = 1.dp
    )
}

/** Reusable label + optional subtitle + switch row. */
@Composable
private fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                checkedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.26f),
                checkedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                uncheckedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun ThemePackPicker(
    selected: ThemePack,
    onSelected: (ThemePack) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemePack.entries.forEach { themePack ->
            val isSelected = themePack == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.09f),
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f)
                                )
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f)
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelected(themePack) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = themePack.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    },
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

/** Converts 24-hour int to "3 PM" format for the active-hours sliders. */
private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        hour == 24 -> "12 AM"
        else -> "${hour - 12} PM"
    }
}

/** Converts a Compose [Color] to a "#RRGGBB" hex string for persistence. */
private fun colorToHex(color: Color): String {
    return String.format(
        "#%02X%02X%02X",
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )
}
