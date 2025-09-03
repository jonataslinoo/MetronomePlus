package br.com.jonatas.metronomeplus.domain.repository

import br.com.jonatas.metronomeplus.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getSongsByFolderId(folderId: String): Flow<List<Song>>
}