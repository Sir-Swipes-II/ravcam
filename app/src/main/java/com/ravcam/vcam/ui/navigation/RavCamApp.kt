package com.ravcam.vcam.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ravcam.vcam.ui.screens.about.AboutScreen
import com.ravcam.vcam.ui.screens.diagnostics.DiagnosticsScreen
import com.ravcam.vcam.ui.screens.home.RavCamDashboard
import com.ravcam.vcam.ui.screens.preview.PreviewScreen
import com.ravcam.vcam.ui.screens.settings.SettingsScreen
import com.ravcam.vcam.ui.screens.sources.SourcesScreen
import com.ravcam.vcam.ui.theme.RavBackgroundDeep
import com.ravcam.vcam.ui.theme.RavCyan
import com.ravcam.vcam.ui.theme.RavSurface
import com.ravcam.vcam.ui.theme.RavTextMuted
import com.ravcam.vcam.ui.state.rememberRavCamSourceState

@Composable
fun RavCamApp() {
    val navController = rememberNavController()
    val sourceState = rememberRavCamSourceState()

    val currentDestination = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RavBackgroundDeep)
    ) {
        NavHost(
            navController = navController,
            startDestination = RavCamDestination.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(RavCamDestination.Home.route) {
                RavCamDashboard(
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(RavCamDestination.Sources.route) {
                SourcesScreen(
                    sourceState = sourceState,
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(RavCamDestination.Preview.route) {
                PreviewScreen(
                    activeSource = sourceState.activeSource,
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(RavCamDestination.Settings.route) {
                SettingsScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(RavCamDestination.Diagnostics.route) {
                DiagnosticsScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(RavCamDestination.About.route) {
                AboutScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        RavBottomNavigationBar(
            currentRoute = currentDestination,
            onDestinationSelected = { destination ->
                navController.navigate(destination.route) {
                    popUpTo(RavCamDestination.Home.route) {
                        saveState = true
                    }

                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}



@Composable
private fun RavBottomNavigationBar(
    currentRoute: androidx.navigation.NavDestination?,
    onDestinationSelected: (RavCamDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(RavSurface.copy(alpha = 0.88f))
            .border(
                width = 1.dp,
                color = RavCyan.copy(alpha = 0.48f),
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        NavigationBar(
            containerColor = RavSurface.copy(alpha = 0.08f),
            tonalElevation = 0.dp
        ) {
            bottomNavDestinations.forEach { destination ->
                val selected = currentRoute
                    ?.hierarchy
                    ?.any { it.route == destination.route } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        onDestinationSelected(destination)
                    },
                    icon = {
                        Text(
                            text = destination.navCode,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    },
                    label = {
                        Text(
                            text = destination.label.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RavBackgroundDeep,
                        selectedTextColor = RavCyan,
                        indicatorColor = RavCyan,
                        unselectedIconColor = RavTextMuted,
                        unselectedTextColor = RavTextMuted
                    )
                )
            }
        }
    }
}