package com.ravcam.vcam.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.ravcam.vcam.domain.models.MediaSourceType
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.domain.models.SourceSlot
import com.ravcam.vcam.domain.models.toSourceSlot

@Stable
class RavCamSourceState {

    val sourcesBySlot = mutableStateMapOf<SourceSlot, RavMediaSource>()

    val activeSource: RavMediaSource?
        get() = sourcesBySlot.values.firstOrNull { source ->
            source.isActive
        }

    init {
        sourcesBySlot[SourceSlot.VIDEO] = RavMediaSource(
            id = "source_video",
            name = "Demo Local MP4",
            type = MediaSourceType.MP4,
            location = "/storage/emulated/0/Movies/demo.mp4"
        )

        sourcesBySlot[SourceSlot.IMAGE] = RavMediaSource(
            id = "source_image",
            name = "Fallback Image",
            type = MediaSourceType.IMAGE,
            location = "/storage/emulated/0/Pictures/ravcam.jpg"
        )

        sourcesBySlot[SourceSlot.STREAM] = RavMediaSource(
            id = "source_stream",
            name = "OBS Stream",
            type = MediaSourceType.RTMP,
            location = "rtmp://192.168.1.10/live/ravcam"
        )
    }

    fun saveSource(
        name: String,
        type: MediaSourceType,
        location: String
    ) {
        val slot = type.toSourceSlot()

        sourcesBySlot[slot] = RavMediaSource(
            id = "source_${slot.name.lowercase()}",
            name = name.trim(),
            type = type,
            location = location.trim(),
            isActive = false
        )
    }

    fun toggleSource(selectedSource: RavMediaSource) {
        val shouldStop = selectedSource.isActive

        SourceSlot.entries.forEach { slot ->
            val source = sourcesBySlot[slot] ?: return@forEach

            sourcesBySlot[slot] = source.copy(
                isActive = if (shouldStop) {
                    false
                } else {
                    source.id == selectedSource.id
                }
            )
        }
    }

    fun deleteSource(selectedSource: RavMediaSource) {
        sourcesBySlot.remove(
            selectedSource.type.toSourceSlot()
        )
    }
}

@Composable
fun rememberRavCamSourceState(): RavCamSourceState {
    return remember {
        RavCamSourceState()
    }
}