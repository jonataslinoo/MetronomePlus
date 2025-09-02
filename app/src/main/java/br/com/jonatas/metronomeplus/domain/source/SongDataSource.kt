package br.com.jonatas.metronomeplus.domain.source

import br.com.jonatas.metronomeplus.data.model.SongDto
import kotlinx.coroutines.flow.Flow

interface SongDataSource {
    fun getAllSongs(): Flow<List<SongDto>>
    fun getSongsByFolderId(folderId: String): Flow<List<SongDto>>
}