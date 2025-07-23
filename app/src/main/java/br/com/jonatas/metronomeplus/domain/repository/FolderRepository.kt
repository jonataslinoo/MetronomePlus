package br.com.jonatas.metronomeplus.domain.repository

import br.com.jonatas.metronomeplus.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    suspend fun save(folder: Folder)
    suspend fun remove(folder: Folder)
    fun getFolders(): Flow<List<Folder>>
}