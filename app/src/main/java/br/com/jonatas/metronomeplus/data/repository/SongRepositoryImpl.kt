package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.di.app.IoDispatcher
import br.com.jonatas.metronomeplus.domain.model.Song
import br.com.jonatas.metronomeplus.domain.repository.SongRepository
import br.com.jonatas.metronomeplus.domain.source.SongDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val songDataSource: SongDataSource,
    @IoDispatcher
    private val dispatcher: CoroutineDispatcher
) : SongRepository {

    override fun getAllSongs(): Flow<List<Song>> {
        return songDataSource.getAllSongs().map {
            it.toDomainList()
        }.flowOn(dispatcher)
    }

    override fun getSongsByFolderId(folderId: String): Flow<List<Song>> {
        return songDataSource.getSongsByFolderId(folderId = folderId).map {
            it.toDomainList()
        }.flowOn(dispatcher)
    }
}