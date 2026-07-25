package com.ravcam.vcam.ui.screens.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ravcam.vcam.ui.components.RavPlaceholderScreen

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    RavPlaceholderScreen(
        title = "About",
        subtitle = "RavCam VCam is a virtual source control interface for Android.",
        status = "RavCam VCam v0.1",
        modifier = modifier
    )
}