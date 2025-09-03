package br.com.jonatas.metronomeplus.di.folders

import br.com.jonatas.metronomeplus.data.repository.FolderRepositoryImpl
import br.com.jonatas.metronomeplus.data.source.FolderDataSourceImpl
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCase
import br.com.jonatas.metronomeplus.domain.usecase.folderform.GetFolderUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

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
}