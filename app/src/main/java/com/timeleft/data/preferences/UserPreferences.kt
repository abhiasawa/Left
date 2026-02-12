package com.timeleft.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Singleton DataStore instance scoped to the application [Context]. */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/** Typed keys for every persisted preference field. */
object PreferenceKeys {
    val BIRTH_DATE = longPreferencesKey("birth_date")
    val EXPECTED_LIFESPAN = intPreferencesKey("expected_lifespan")
    val SYMBOL_TYPE = stringPreferencesKey("symbol_type")
    val ELAPSED_COLOR = stringPreferencesKey("elapsed_color")
    val REMAINING_COLOR = stringPreferencesKey("remaining_color")
    val CURRENT_INDICATOR_COLOR = stringPreferencesKey("current_indicator_color")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val ACTIVE_HOURS_START = intPreferencesKey("active_hours_start")
    val ACTIVE_HOURS_END = intPreferencesKey("active_hours_end")
    val GENDER = stringPreferencesKey("gender")
    val COUNTRY = stringPreferencesKey("country")
    val USER_NAME = stringPreferencesKey("user_name")
    val SELECTED_TIME_UNIT = stringPreferencesKey("selected_time_unit")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val DAILY_NOTIFICATION_ENABLED = booleanPreferencesKey("daily_notification_enabled")
    val MILESTONE_NOTIFICATION_ENABLED = booleanPreferencesKey("milestone_notification_enabled")
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
}

/**
 * Snapshot of all user settings.
 *
 * Emitted from [UserPreferencesRepository.preferences] as a [Flow].
 * Default values match the first-launch experience (dark mode, dot symbols, year view).
 */
data class UserPreferencesData(
    val birthDate: LocalDate? = null,
    val expectedLifespan: Int = 80,
    val symbolType: String = "DOT",
    val elapsedColor: String = "#3A3A3A",
    val remainingColor: String = "#FFFFFF",
    val currentIndicatorColor: String = "#FF3B30",
    val darkMode: Boolean = true,
    val activeHoursStart: Int = 0,
    val activeHoursEnd: Int = 24,
    val gender: String = "",
    val country: String = "United States",
    val userName: String = "",
    val selectedTimeUnit: String = "YEAR",
    val notificationsEnabled: Boolean = false,
    val dailyNotificationEnabled: Boolean = false,
    val milestoneNotificationEnabled: Boolean = false,
    val hasCompletedOnboarding: Boolean = false
)

/**
 * Single source of truth for user preferences, backed by Jetpack DataStore.
 *
 * Exposes a reactive [preferences] flow and individual `suspend` update methods.
 * Each update is an atomic read-modify-write thanks to DataStore's [edit] API.
 */
class UserPreferencesRepository(private val context: Context) {

    /** Reactive stream that emits a fresh [UserPreferencesData] on every change. */
    val preferences: Flow<UserPreferencesData> = context.dataStore.data.map { prefs ->
        UserPreferencesData(
            birthDate = prefs[PreferenceKeys.BIRTH_DATE]?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            },
            expectedLifespan = prefs[PreferenceKeys.EXPECTED_LIFESPAN] ?: 80,
            symbolType = prefs[PreferenceKeys.SYMBOL_TYPE] ?: "DOT",
            elapsedColor = prefs[PreferenceKeys.ELAPSED_COLOR] ?: "#3A3A3A",
            remainingColor = prefs[PreferenceKeys.REMAINING_COLOR] ?: "#FFFFFF",
            currentIndicatorColor = prefs[PreferenceKeys.CURRENT_INDICATOR_COLOR] ?: "#FF3B30",
            darkMode = prefs[PreferenceKeys.DARK_MODE] ?: true,
            activeHoursStart = prefs[PreferenceKeys.ACTIVE_HOURS_START] ?: 0,
            activeHoursEnd = prefs[PreferenceKeys.ACTIVE_HOURS_END] ?: 24,
            gender = prefs[PreferenceKeys.GENDER] ?: "",
            country = prefs[PreferenceKeys.COUNTRY] ?: "United States",
            userName = prefs[PreferenceKeys.USER_NAME] ?: "",
            selectedTimeUnit = prefs[PreferenceKeys.SELECTED_TIME_UNIT] ?: "YEAR",
            notificationsEnabled = prefs[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: false,
            dailyNotificationEnabled = prefs[PreferenceKeys.DAILY_NOTIFICATION_ENABLED] ?: false,
            milestoneNotificationEnabled = prefs[PreferenceKeys.MILESTONE_NOTIFICATION_ENABLED] ?: false,
            hasCompletedOnboarding = prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING] ?: false
        )
    }

    suspend fun updateBirthDate(date: LocalDate) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.BIRTH_DATE] = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }

    suspend fun updateExpectedLifespan(years: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.EXPECTED_LIFESPAN] = years
        }
    }

    suspend fun updateSymbolType(symbol: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SYMBOL_TYPE] = symbol
        }
    }

    suspend fun updateElapsedColor(colorHex: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.ELAPSED_COLOR] = colorHex
        }
    }

    suspend fun updateRemainingColor(colorHex: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.REMAINING_COLOR] = colorHex
        }
    }

    suspend fun updateCurrentIndicatorColor(colorHex: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.CURRENT_INDICATOR_COLOR] = colorHex
        }
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DARK_MODE] = enabled
        }
    }

    suspend fun updateActiveHours(start: Int, end: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.ACTIVE_HOURS_START] = start
            prefs[PreferenceKeys.ACTIVE_HOURS_END] = end
        }
    }

    suspend fun updateGender(gender: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.GENDER] = gender
        }
    }

    suspend fun updateCountry(country: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.COUNTRY] = country
        }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.USER_NAME] = name
        }
    }

    suspend fun updateSelectedTimeUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SELECTED_TIME_UNIT] = unit
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateDailyNotification(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DAILY_NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun updateMilestoneNotification(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.MILESTONE_NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }
}
