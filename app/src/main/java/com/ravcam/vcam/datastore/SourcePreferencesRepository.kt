package com.ravcam.vcam.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ravcam.vcam.domain.models.MediaSourceType
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.domain.models.SourceSlot
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.ravCamSourcesDataStore:
        DataStore<Preferences> by preferencesDataStore(
    name = "ravcam_sources"
)

class SourcePreferencesRepository(
    context: Context
) {
    private val dataStore =
        context.applicationContext.ravCamSourcesDataStore

    val sourcesFlow: Flow<Map<SourceSlot, RavMediaSource>> =
        dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences ->
                buildMap {
                    SourceSlot.entries.forEach { slot ->
                        readSource(
                            preferences = preferences,
                            slot = slot
                        )?.let { source ->
                            put(slot, source)
                        }
                    }
                }
            }

    suspend fun saveSources(
        sources: Map<SourceSlot, RavMediaSource>
    ) {
        dataStore.edit { preferences ->
            SourceSlot.entries.forEach { slot ->
                val source = sources[slot]

                if (source == null) {
                    preferences.removeSlot(slot)
                } else {
                    preferences[idKey(slot)] = source.id
                    preferences[nameKey(slot)] = source.name
                    preferences[typeKey(slot)] = source.type.name
                    preferences[locationKey(slot)] = source.location
                    preferences[activeKey(slot)] = source.isActive
                    preferences[createdAtKey(slot)] =
                        source.createdAtMillis
                }
            }
        }
    }

    private fun readSource(
        preferences: Preferences,
        slot: SourceSlot
    ): RavMediaSource? {
        val id = preferences[idKey(slot)]
            ?: return null

        val name = preferences[nameKey(slot)]
            ?: return null

        val typeName = preferences[typeKey(slot)]
            ?: return null

        val location = preferences[locationKey(slot)]
            ?: return null

        val type = runCatching {
            MediaSourceType.valueOf(typeName)
        }.getOrNull() ?: return null

        return RavMediaSource(
            id = id,
            name = name,
            type = type,
            location = location,
            isActive = preferences[activeKey(slot)] ?: false,
            createdAtMillis =
                preferences[createdAtKey(slot)]
                    ?: System.currentTimeMillis()
        )
    }
}

private fun slotPrefix(
    slot: SourceSlot
): String {
    return "source_${slot.name.lowercase()}"
}

private fun idKey(
    slot: SourceSlot
) = stringPreferencesKey(
    "${slotPrefix(slot)}_id"
)

private fun nameKey(
    slot: SourceSlot
) = stringPreferencesKey(
    "${slotPrefix(slot)}_name"
)

private fun typeKey(
    slot: SourceSlot
) = stringPreferencesKey(
    "${slotPrefix(slot)}_type"
)

private fun locationKey(
    slot: SourceSlot
) = stringPreferencesKey(
    "${slotPrefix(slot)}_location"
)

private fun activeKey(
    slot: SourceSlot
) = booleanPreferencesKey(
    "${slotPrefix(slot)}_active"
)

private fun createdAtKey(
    slot: SourceSlot
) = longPreferencesKey(
    "${slotPrefix(slot)}_created_at"
)

private fun MutablePreferences.removeSlot(
    slot: SourceSlot
) {
    remove(idKey(slot))
    remove(nameKey(slot))
    remove(typeKey(slot))
    remove(locationKey(slot))
    remove(activeKey(slot))
    remove(createdAtKey(slot))
}