package com.timeleft.navigation

import androidx.compose.runtime.Composable
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.domain.models.TimeUnit
import com.timeleft.ui.screens.LeftScreen

/**
 * Root composable — simply renders the main [LeftScreen].
 *
 * No bottom navigation, no multi-screen routing. The app is a single
 * visualization screen; settings are accessed via the gear icon.
 */
@Composable
fun AppNavigation(
    preferences: UserPreferencesData,
    selectedUnit: TimeUnit,
    onUnitSelected: (TimeUnit) -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    LeftScreen(
        preferences = preferences,
        selectedUnit = selectedUnit,
        onUnitSelected = onUnitSelected,
        onShareClick = onShareClick,
        onSettingsClick = onSettingsClick
    )
}
