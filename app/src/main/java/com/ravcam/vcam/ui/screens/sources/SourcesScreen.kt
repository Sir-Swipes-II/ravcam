package com.ravcam.vcam.ui.screens.sources

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ravcam.vcam.ui.components.RavPlaceholderScreen

@Composable
fun SourcesScreen(
    modifier: Modifier = Modifier
) {
    RavPlaceholderScreen(
        title = "Sources",
        subtitle = "Manage MP4, image, GIF, RTMP, RTSP, and HTTP media sources.",
        status = "Source library coming online",
        modifier = modifier
    )
}