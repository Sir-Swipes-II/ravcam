package com.ravcam.vcam.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ravcam.vcam.datastore.OutputProfileRepository
import com.ravcam.vcam.domain.models.OutputFitMode
import com.ravcam.vcam.domain.models.OutputFrameRate
import com.ravcam.vcam.domain.models.OutputResolution
import com.ravcam.vcam.domain.models.OutputRotation
import com.ravcam.vcam.domain.models.RavOutputProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Stable
class RavOutputProfileState(
    private val repository: OutputProfileRepository,
    private val coroutineScope: CoroutineScope
) {
    var profile by mutableStateOf(RavOutputProfile())
        private set

    var isLoaded by mutableStateOf(false)
        private set

    init {
        coroutineScope.launch {
            repository.profileFlow.collect { savedProfile ->
                profile = savedProfile
                isLoaded = true
            }
        }
    }

    fun setResolution(
        resolution: OutputResolution
    ) {
        updateProfile(
            profile.copy(
                resolution = resolution
            )
        )
    }

    fun setFrameRate(
        frameRate: OutputFrameRate
    ) {
        updateProfile(
            profile.copy(
                frameRate = frameRate
            )
        )
    }

    fun setFitMode(
        fitMode: OutputFitMode
    ) {
        updateProfile(
            profile.copy(
                fitMode = fitMode
            )
        )
    }

    fun setRotation(
        rotation: OutputRotation
    ) {
        updateProfile(
            profile.copy(
                rotation = rotation
            )
        )
    }

    fun setMirrorHorizontal(
        enabled: Boolean
    ) {
        updateProfile(
            profile.copy(
                mirrorHorizontal = enabled
            )
        )
    }

    fun setLoopMedia(
        enabled: Boolean
    ) {
        updateProfile(
            profile.copy(
                loopMedia = enabled
            )
        )
    }

    fun setPreviewAudio(
        enabled: Boolean
    ) {
        updateProfile(
            profile.copy(
                previewAudio = enabled
            )
        )
    }

    fun resetProfile() {
        profile = RavOutputProfile()

        coroutineScope.launch {
            repository.resetProfile()
        }
    }

    private fun updateProfile(
        updatedProfile: RavOutputProfile
    ) {
        profile = updatedProfile

        coroutineScope.launch {
            repository.saveProfile(updatedProfile)
        }
    }
}

@Composable
fun rememberRavOutputProfileState():
        RavOutputProfileState {
    val context =
        LocalContext.current.applicationContext

    val coroutineScope =
        rememberCoroutineScope()

    val repository = remember(context) {
        OutputProfileRepository(context)
    }

    return remember(
        repository,
        coroutineScope
    ) {
        RavOutputProfileState(
            repository = repository,
            coroutineScope = coroutineScope
        )
    }
}