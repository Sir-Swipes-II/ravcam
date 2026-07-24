package com.ravcam.vcam.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RavCamDarkColorScheme = darkColorScheme(
    primary = RavCyan,
    secondary = RavMagenta,
    tertiary = RavBlue,
    background = RavBackground,
    surface = RavSurface,
    onPrimary = RavBackgroundDeep,
    onSecondary = RavTextPrimary,
    onTertiary = RavTextPrimary,
    onBackground = RavTextPrimary,
    onSurface = RavTextPrimary,
    error = RavRed
)

@Composable
fun RavCamTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RavCamDarkColorScheme,
        typography = Typography,
        content = content
    )
}