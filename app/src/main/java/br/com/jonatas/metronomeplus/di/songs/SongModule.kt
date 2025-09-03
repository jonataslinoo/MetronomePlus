package br.com.jonatas.metronomeplus.di.songs

import br.com.jonatas.metronomeplus.data.repository.SongRepositoryImpl
import br.com.jonatas.metronomeplus.data.source.SongDataSourceImpl
import br.com.jonatas.metronomeplus.domain.repository.SongRepository
import br.com.jonatas.metronomeplus.domain.source.SongDataSource
import br.com.jonatas.metronomeplus.domain.usecase.folderform.song.GetSongsByFolderUseCase
import br.com.jonatas.metronomeplus.domain.usecase.folderform.song.GetSongsByFolderUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SongModule {

    /* FolderForm */
    @Binds
    abstract fun bindGetSongsByFolderUseCase(getSongsByFolderUseCaseImpl: GetSongsByFolderUseCaseImpl): GetSongsByFolderUseCase

    /* Data */
    @Binds
    @Singleton
    abstract fun bindSongRepository(songRepositoryImpl: SongRepositoryImpl): SongRepository

    @Binds
    @Singleton
    abstract fun bindSongDataSource(songDataSourceImpl: SongDataSourceImpl): SongDataSource
}