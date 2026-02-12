package com.timeleft.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.domain.models.SymbolType
import com.timeleft.ui.components.DotGrid
import com.timeleft.ui.components.calculateGridHeight
import com.timeleft.util.TimeCalculations
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun YouScreen(
    preferences: UserPreferencesData,
    onBirthDateChanged: (LocalDate) -> Unit,
    onGenderChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onLifespanChanged: (Int) -> Unit,
    onNameChanged: (String) -> Unit,
    onActiveHoursChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val birthDate = preferences.birthDate
    val lifespan = preferences.expectedLifespan
    val gender = preferences.gender
    val country = preferences.country
    val userName = preferences.userName

    val elapsedColor = parseColorSafe(preferences.elapsedColor)
    val remainingColor = parseColorSafe(preferences.remainingColor)
    val symbolType = SymbolType.fromString(preferences.symbolType)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Life visualization (if birth date is set)
        if (birthDate != null) {
            val yearsLived = TimeCalculations.lifeYearsElapsed(birthDate)
            val yearsRemaining = TimeCalculations.lifeYearsRemaining(birthDate, lifespan)
            val progress = TimeCalculations.lifeProgress(birthDate, lifespan)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Life Progress",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$yearsRemaining years left",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$yearsLived of $lifespan years (${String.format("%.1f", progress * 100)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val columns = 15
                    val dotSize = 10f
                    val spacing = 3f
                    val gridH = calculateGridHeight(lifespan, columns, dotSize, spacing)

                    DotGrid(
                        totalUnits = lifespan,
                        elapsedUnits = yearsLived,
                        symbolType = symbolType,
                        elapsedColor = elapsedColor,
                        remainingColor = remainingColor,
                        dotSize = dotSize.dp,
                        spacing = spacing.dp,
                        showCurrentIndicator = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridH.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Name field
        SectionHeader("Personal")

        OutlinedTextField(
            value = userName,
            onValueChange = onNameChanged,
            label = { Text("Name (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Birth date
        val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        val birthDateText = birthDate?.format(dateFormatter) ?: "Select date"

        Text(
            text = "Date of Birth",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    val now = LocalDate.now()
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = birthDateText,
                    color = if (birthDate != null)
                        MaterialTheme.colorScheme.onBackground
                    else
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gender
        var genderExpanded by remember { mutableStateOf(false) }
        val genderOptions = listOf("Male", "Female", "Other")

        Text(
            text = "Gender",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { genderExpanded = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gender.ifEmpty { "Select gender" },
                        color = if (gender.isNotEmpty())
                            MaterialTheme.colorScheme.onBackground
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
            DropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Country
        var countryExpanded by remember { mutableStateOf(false) }

        Text(
            text = "Country",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { countryExpanded = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = country.ifEmpty { "Select country" },
                        color = if (country.isNotEmpty())
                            MaterialTheme.colorScheme.onBackground
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
            DropdownMenu(
                expanded = countryExpanded,
                onDismissRequest = { countryExpanded = false }
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

        Spacer(modifier = Modifier.height(24.dp))

        // Life expectancy section
        SectionHeader("Life Expectancy")

        if (gender.isNotEmpty() && country.isNotEmpty()) {
            val estimated = TimeCalculations.estimateLifeExpectancy(gender, country)
            Text(
                text = "Estimated: $estimated years (based on $country, $gender)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        var sliderValue by remember(lifespan) { mutableFloatStateOf(lifespan.toFloat()) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Expected lifespan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${sliderValue.toInt()} years",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
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
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Active hours
        SectionHeader("Active Hours")

        Text(
            text = "Used for the Day view to show only waking hours",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "End: ${formatHour(endHour.toInt())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Slider(
            value = startHour,
            onValueChange = { startHour = it },
            onValueChangeFinished = { onActiveHoursChanged(startHour.toInt(), endHour.toInt()) },
            valueRange = 0f..23f,
            steps = 22,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Slider(
            value = endHour,
            onValueChange = { endHour = it },
            onValueChangeFinished = { onActiveHoursChanged(startHour.toInt(), endHour.toInt()) },
            valueRange = 1f..24f,
            steps = 22,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(100.dp)) // Bottom nav space
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
    focusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
    cursorColor = MaterialTheme.colorScheme.onBackground
)

private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        hour == 24 -> "12 AM"
        else -> "${hour - 12} PM"
    }
}

private fun parseColorSafe(hex: String): androidx.compose.ui.graphics.Color {
    return try {
        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        androidx.compose.ui.graphics.Color.White
    }
}
