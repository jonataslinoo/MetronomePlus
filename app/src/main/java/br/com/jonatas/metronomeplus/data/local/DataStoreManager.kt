package br.com.jonatas.metronomeplus.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_preferences")

class DataStoreManager private constructor(private val context: Context) {

    suspend fun setData(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    fun getData(key: Preferences.Key<String>): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[key] ?: ""
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager {
            return INSTANCE ?: synchronized(this) {
                val instance = DataStoreManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}