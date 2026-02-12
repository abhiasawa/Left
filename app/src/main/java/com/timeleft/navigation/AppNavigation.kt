package com.timeleft.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timeleft.data.preferences.UserPreferencesData
import com.timeleft.domain.models.CustomDate
import com.timeleft.domain.models.SymbolType
import com.timeleft.domain.models.TimeUnit
import com.timeleft.ui.screens.AheadScreen
import com.timeleft.ui.screens.LeftScreen
import com.timeleft.ui.screens.YouScreen
import java.time.LocalDate

sealed class Screen(val route: String, val label: String, val iconRes: Int) {
    data object Left : Screen("left", "Left", android.R.drawable.ic_menu_today)
    data object Ahead : Screen("ahead", "Ahead", android.R.drawable.ic_menu_recent_history)
    data object You : Screen("you", "You", android.R.drawable.ic_menu_myplaces)
}

val bottomNavItems = listOf(Screen.Left, Screen.Ahead, Screen.You)

@Composable
fun AppNavigation(
    preferences: UserPreferencesData,
    customDates: List<CustomDate>,
    selectedUnit: TimeUnit,
    onUnitSelected: (TimeUnit) -> Unit,
    onShareClick: () -> Unit,
    onLongPress: () -> Unit,
    onAddDate: (CustomDate) -> Unit,
    onDeleteDate: (Int) -> Unit,
    onBirthDateChanged: (LocalDate) -> Unit,
    onGenderChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onLifespanChanged: (Int) -> Unit,
    onNameChanged: (String) -> Unit,
    onActiveHoursChanged: (Int, Int) -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.iconRes),
                                contentDescription = screen.label
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onBackground,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Left.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Left.route) {
                LeftScreen(
                    preferences = preferences,
                    selectedUnit = selectedUnit,
                    onUnitSelected = onUnitSelected,
                    onShareClick = onShareClick,
                    onLongPress = onLongPress
                )
            }
            composable(Screen.Ahead.route) {
                AheadScreen(
                    customDates = customDates,
                    onAddDate = onAddDate,
                    onDeleteDate = onDeleteDate
                )
            }
            composable(Screen.You.route) {
                YouScreen(
                    preferences = preferences,
                    onBirthDateChanged = onBirthDateChanged,
                    onGenderChanged = onGenderChanged,
                    onCountryChanged = onCountryChanged,
                    onLifespanChanged = onLifespanChanged,
                    onNameChanged = onNameChanged,
                    onActiveHoursChanged = onActiveHoursChanged
                )
            }
        }
    }
}
