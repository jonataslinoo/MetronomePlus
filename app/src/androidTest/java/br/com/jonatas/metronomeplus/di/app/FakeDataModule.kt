package br.com.jonatas.metronomeplus.di.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import br.com.jonatas.metronomeplus.data.local.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

private val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(name = "tests_preferences")

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
object FakeDataModule {
    @Provides
    @Singleton
    fun provideFakePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.testDataStore
    }

    @Provides
    @Singleton
    fun provideFakeDataStoreManager(dataStore: DataStore<Preferences>): DataStoreManager {
        return DataStoreManager(dataStore)
    }
}