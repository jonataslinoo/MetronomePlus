package br.com.jonatas.metronomeplus.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun setData(key: Preferences.Key<String>, value: String) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    fun getData(key: Preferences.Key<String>): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[key] ?: ""
        }
    }
}