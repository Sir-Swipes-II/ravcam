package com.ravcam.vcam.ui.screens.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ravcam.vcam.ui.components.RavPlaceholderScreen

@Composable
fun PreviewScreen(
    modifier: Modifier = Modifier
) {
    RavPlaceholderScreen(
        title = "Preview",
        subtitle = "Preview the selected virtual camera source before routing output.",
        status = "Preview renderer standing by",
        modifier = modifier
    )
}