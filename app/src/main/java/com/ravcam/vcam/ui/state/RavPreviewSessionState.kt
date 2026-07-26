package com.ravcam.vcam.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ravcam.vcam.domain.models.MediaSourceType
import com.ravcam.vcam.domain.models.RavMediaSource

@Stable
class RavPreviewSessionState {

    var isRunning by mutableStateOf(false)
        private set

    var activeSourceId by mutableStateOf<String?>(null)
        private set

    var startedAtMillis by mutableStateOf<Long?>(null)
        private set

    fun start(
        source: RavMediaSource
    ): Boolean {
        if (!source.type.supportsInAppPreview()) {
            return false
        }

        activeSourceId = source.id
        startedAtMillis = System.currentTimeMillis()
        isRunning = true

        return true
    }

    fun stop() {
        isRunning = false
        activeSourceId = null
        startedAtMillis = null
    }

    fun synchronize(
        activeSource: RavMediaSource?
    ) {
        if (!isRunning) {
            return
        }

        val sourceStillMatches =
            activeSource != null &&
                    activeSource.isActive &&
                    activeSource.id == activeSourceId &&
                    activeSource.type.supportsInAppPreview()

        if (!sourceStillMatches) {
            stop()
        }
    }
}

fun MediaSourceType.supportsInAppPreview(): Boolean {
    return this == MediaSourceType.MP4 ||
            this == MediaSourceType.IMAGE ||
            this == MediaSourceType.GIF ||
            this == MediaSourceType.RTSP ||
            this == MediaSourceType.HTTP
}

@Composable
fun rememberRavPreviewSessionState():
        RavPreviewSessionState {
    return remember {
        RavPreviewSessionState()
    }
}