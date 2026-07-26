package com.ravcam.vcam.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ravcam.vcam.domain.models.OutputFitMode
import com.ravcam.vcam.domain.models.OutputFrameRate
import com.ravcam.vcam.domain.models.OutputResolution
import com.ravcam.vcam.domain.models.OutputRotation
import com.ravcam.vcam.domain.models.RavOutputProfile
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.ravCamOutputProfileDataStore:
        DataStore<Preferences> by preferencesDataStore(
    name = "ravcam_output_profile"
)

class OutputProfileRepository(
    context: Context
) {
    private val dataStore =
        context.applicationContext.ravCamOutputProfileDataStore

    val profileFlow: Flow<RavOutputProfile> =
        dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences ->
                RavOutputProfile(
                    resolution = preferences.readEnum(
                        key = resolutionKey,
                        defaultValue =
                            OutputResolution.FULL_HD_1080P
                    ),
                    frameRate = preferences.readEnum(
                        key = frameRateKey,
                        defaultValue =
                            OutputFrameRate.FPS_30
                    ),
                    fitMode = preferences.readEnum(
                        key = fitModeKey,
                        defaultValue =
                            OutputFitMode.FIT
                    ),
                    rotation = preferences.readEnum(
                        key = rotationKey,
                        defaultValue =
                            OutputRotation.DEGREE_0
                    ),
                    mirrorHorizontal =
                        preferences[mirrorHorizontalKey]
                            ?: false,
                    loopMedia =
                        preferences[loopMediaKey]
                            ?: true,
                    previewAudio =
                        preferences[previewAudioKey]
                            ?: false
                )
            }

    suspend fun saveProfile(
        profile: RavOutputProfile
    ) {
        dataStore.edit { preferences ->
            preferences[resolutionKey] =
                profile.resolution.name

            preferences[frameRateKey] =
                profile.frameRate.name

            preferences[fitModeKey] =
                profile.fitMode.name

            preferences[rotationKey] =
                profile.rotation.name

            preferences[mirrorHorizontalKey] =
                profile.mirrorHorizontal

            preferences[loopMediaKey] =
                profile.loopMedia

            preferences[previewAudioKey] =
                profile.previewAudio
        }
    }

    suspend fun resetProfile() {
        dataStore.edit { preferences ->
            preferences.remove(resolutionKey)
            preferences.remove(frameRateKey)
            preferences.remove(fitModeKey)
            preferences.remove(rotationKey)
            preferences.remove(mirrorHorizontalKey)
            preferences.remove(loopMediaKey)
            preferences.remove(previewAudioKey)
        }
    }
}

private val resolutionKey =
    stringPreferencesKey("output_resolution")

private val frameRateKey =
    stringPreferencesKey("output_frame_rate")

private val fitModeKey =
    stringPreferencesKey("output_fit_mode")

private val rotationKey =
    stringPreferencesKey("output_rotation")

private val mirrorHorizontalKey =
    booleanPreferencesKey("output_mirror_horizontal")

private val loopMediaKey =
    booleanPreferencesKey("output_loop_media")

private val previewAudioKey =
    booleanPreferencesKey("output_preview_audio")

private inline fun <reified T : Enum<T>> Preferences.readEnum(
    key: Preferences.Key<String>,
    defaultValue: T
): T {
    val savedValue = this[key]
        ?: return defaultValue

    return enumValues<T>()
        .firstOrNull { value ->
            value.name == savedValue
        }
        ?: defaultValue
}