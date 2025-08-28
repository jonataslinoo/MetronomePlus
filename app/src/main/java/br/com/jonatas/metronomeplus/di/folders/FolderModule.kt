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
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_preferences")

@Module
@InstallIn(SingletonComponent::class)
abstract class FolderModule {

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
        fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.dataStore
        }

        @Provides
        @Singleton
        fun provideDataStoreManager(dataStore: DataStore<Preferences>): DataStoreManager {
            return DataStoreManager(dataStore)
        }
    }
}