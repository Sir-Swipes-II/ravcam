package com.ravcam.vcam.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ravcam.vcam.datastore.SourcePreferencesRepository
import com.ravcam.vcam.domain.models.MediaSourceType
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.domain.models.SourceSlot
import com.ravcam.vcam.domain.models.toSourceSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Stable
class RavCamSourceState(
    private val repository: SourcePreferencesRepository,
    private val coroutineScope: CoroutineScope
) {
    val sourcesBySlot =
        mutableStateMapOf<SourceSlot, RavMediaSource>()

    var isLoaded by mutableStateOf(false)
        private set

    val activeSource: RavMediaSource?
        get() = sourcesBySlot.values.firstOrNull { source ->
            source.isActive
        }

    init {
        coroutineScope.launch {
            repository.sourcesFlow.collect { savedSources ->
                sourcesBySlot.clear()
                sourcesBySlot.putAll(savedSources)
                isLoaded = true
            }
        }
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

        persistSources()
    }

    fun toggleSource(
        selectedSource: RavMediaSource
    ) {
        val shouldStop = selectedSource.isActive

        SourceSlot.entries.forEach { slot ->
            val source =
                sourcesBySlot[slot] ?: return@forEach

            sourcesBySlot[slot] = source.copy(
                isActive = if (shouldStop) {
                    false
                } else {
                    source.id == selectedSource.id
                }
            )
        }

        persistSources()
    }

    fun deleteSource(
        selectedSource: RavMediaSource
    ) {
        sourcesBySlot.remove(
            selectedSource.type.toSourceSlot()
        )

        persistSources()
    }

    private fun persistSources() {
        val snapshot = sourcesBySlot.toMap()

        coroutineScope.launch {
            repository.saveSources(snapshot)
        }
    }
}

@Composable
fun rememberRavCamSourceState(): RavCamSourceState {
    val context =
        LocalContext.current.applicationContext

    val coroutineScope =
        rememberCoroutineScope()

    val repository = remember(context) {
        SourcePreferencesRepository(context)
    }

    return remember(
        repository,
        coroutineScope
    ) {
        RavCamSourceState(
            repository = repository,
            coroutineScope = coroutineScope
        )
    }
}