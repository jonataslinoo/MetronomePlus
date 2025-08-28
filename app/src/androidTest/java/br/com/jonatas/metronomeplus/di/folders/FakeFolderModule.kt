package br.com.jonatas.metronomeplus.di.folders

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import br.com.jonatas.metronomeplus.data.local.DataStoreManager
import br.com.jonatas.metronomeplus.data.repository.FolderRepositoryImpl
import br.com.jonatas.metronomeplus.data.source.FolderDataSourceImpl
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCaseImpl
import dagger.Binds
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
    replaces = [FolderModule::class]
)
abstract class FakeFolderModule {

    /* FolderForm */
    @Binds
    abstract fun bindGetFolderUseCase(getFolderUseCaseImpl: GetFolderUseCaseImpl): GetFolderUseCase

    /* Data */
    @Binds
    @Singleton
    abstract fun bindFolderRepository(folderRepositoryImpl: FolderRepositoryImpl): FolderRepository

    @Binds
    @Singleton
    abstract fun bindFolderDataSource(folderDataSourceImpl: FolderDataSourceImpl): FolderDataSource

    companion object {
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
}