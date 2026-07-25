package com.ravcam.vcam.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ravcam.vcam.ui.components.RavPlaceholderScreen

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    RavPlaceholderScreen(
        title = "Settings",
        subtitle = "Configure profiles, output behavior, source handling, and future account options.",
        status = "Configuration core ready",
        modifier = modifier
    )
}