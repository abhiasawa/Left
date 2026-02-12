package com.timeleft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import com.timeleft.data.db.AppDatabase
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.data.preferences.UserPreferencesRepository
import com.timeleft.data.repository.TimeRepository
import com.timeleft.domain.models.SymbolType
import com.timeleft.domain.models.TimeUnit
import com.timeleft.navigation.AppNavigation
import com.timeleft.ui.settings.SettingsSheet
import com.timeleft.ui.screens.getTimeData
import com.timeleft.ui.theme.TimeLeftTheme
import com.timeleft.util.NotificationHelper
import com.timeleft.util.ShareHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var prefsRepository: UserPreferencesRepository
    private lateinit var timeRepository: TimeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefsRepository = UserPreferencesRepository(this)
        val db = AppDatabase.getDatabase(this)
        timeRepository = TimeRepository(db.customDateDao())

        NotificationHelper.createNotificationChannels(this)

        setContent {
            val preferences by prefsRepository.preferences.collectAsState(
                initial = UserPreferencesData()
            )
            val customDates by timeRepository.customDates.collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()

            var selectedUnit by remember(preferences.selectedTimeUnit) {
                mutableStateOf(TimeUnit.fromString(preferences.selectedTimeUnit))
            }
            var showSettings by remember { mutableStateOf(false) }

            TimeLeftTheme(darkTheme = preferences.darkMode) {
                AppNavigation(
                    preferences = preferences,
                    customDates = customDates,
                    selectedUnit = selectedUnit,
                    onUnitSelected = { unit ->
                        selectedUnit = unit
                        scope.launch {
                            prefsRepository.updateSelectedTimeUnit(unit.name)
                        }
                    },
                    onShareClick = {
                        val timeData = getTimeData(selectedUnit, preferences)
                        val elapsedColor = parseColorInt(preferences.elapsedColor)
                        val remainingColor = parseColorInt(preferences.remainingColor)
                        val bgColor = if (preferences.darkMode) 0xFF000000.toInt() else 0xFFF5F5F5.toInt()
                        val currentColor = parseColorInt(preferences.currentIndicatorColor)

                        ShareHelper.shareTimeLeft(
                            context = this@MainActivity,
                            totalUnits = timeData.total,
                            elapsedUnits = timeData.elapsed,
                            label = timeData.label,
                            remainingText = timeData.remainingText,
                            symbolType = SymbolType.fromString(preferences.symbolType),
                            elapsedColor = elapsedColor,
                            remainingColor = remainingColor,
                            backgroundColor = bgColor,
                            currentColor = currentColor
                        )
                    },
                    onLongPress = { showSettings = true },
                    onAddDate = { date ->
                        scope.launch { timeRepository.addCustomDate(date) }
                    },
                    onDeleteDate = { id ->
                        scope.launch { timeRepository.deleteCustomDate(id) }
                    },
                    onBirthDateChanged = { date ->
                        scope.launch {
                            prefsRepository.updateBirthDate(date)
                            if (preferences.gender.isNotEmpty() && preferences.country.isNotEmpty()) {
                                val estimated = com.timeleft.util.TimeCalculations.estimateLifeExpectancy(
                                    preferences.gender, preferences.country
                                )
                                prefsRepository.updateExpectedLifespan(estimated)
                            }
                        }
                    },
                    onGenderChanged = { gender ->
                        scope.launch {
                            prefsRepository.updateGender(gender)
                            if (gender.isNotEmpty() && preferences.country.isNotEmpty()) {
                                val estimated = com.timeleft.util.TimeCalculations.estimateLifeExpectancy(
                                    gender, preferences.country
                                )
                                prefsRepository.updateExpectedLifespan(estimated)
                            }
                        }
                    },
                    onCountryChanged = { country ->
                        scope.launch {
                            prefsRepository.updateCountry(country)
                            if (preferences.gender.isNotEmpty() && country.isNotEmpty()) {
                                val estimated = com.timeleft.util.TimeCalculations.estimateLifeExpectancy(
                                    preferences.gender, country
                                )
                                prefsRepository.updateExpectedLifespan(estimated)
                            }
                        }
                    },
                    onLifespanChanged = { years ->
                        scope.launch { prefsRepository.updateExpectedLifespan(years) }
                    },
                    onNameChanged = { name ->
                        scope.launch { prefsRepository.updateUserName(name) }
                    },
                    onActiveHoursChanged = { start, end ->
                        scope.launch { prefsRepository.updateActiveHours(start, end) }
                    }
                )

                // Settings bottom sheet
                if (showSettings) {
                    SettingsSheet(
                        preferences = preferences,
                        onDismiss = { showSettings = false },
                        onSymbolChanged = { symbol ->
                            scope.launch { prefsRepository.updateSymbolType(symbol.name) }
                        },
                        onElapsedColorChanged = { color ->
                            scope.launch { prefsRepository.updateElapsedColor(color) }
                        },
                        onRemainingColorChanged = { color ->
                            scope.launch { prefsRepository.updateRemainingColor(color) }
                        },
                        onDarkModeChanged = { enabled ->
                            scope.launch { prefsRepository.updateDarkMode(enabled) }
                        },
                        onNotificationsChanged = { enabled ->
                            scope.launch {
                                prefsRepository.updateNotificationsEnabled(enabled)
                                if (enabled) {
                                    NotificationHelper.scheduleDailyNotification(this@MainActivity)
                                } else {
                                    NotificationHelper.cancelDailyNotification(this@MainActivity)
                                }
                            }
                        },
                        onDailyNotificationChanged = { enabled ->
                            scope.launch {
                                prefsRepository.updateDailyNotification(enabled)
                                if (enabled) {
                                    NotificationHelper.scheduleDailyNotification(this@MainActivity)
                                } else {
                                    NotificationHelper.cancelDailyNotification(this@MainActivity)
                                }
                            }
                        },
                        onMilestoneNotificationChanged = { enabled ->
                            scope.launch { prefsRepository.updateMilestoneNotification(enabled) }
                        }
                    )
                }
            }
        }
    }

    private fun parseColorInt(hex: String): Int {
        return try {
            android.graphics.Color.parseColor(hex)
        } catch (e: Exception) {
            android.graphics.Color.WHITE
        }
    }
}
