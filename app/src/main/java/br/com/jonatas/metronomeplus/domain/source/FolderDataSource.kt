package br.com.jonatas.metronomeplus.domain.source

import br.com.jonatas.metronomeplus.data.model.FolderDto
import kotlinx.coroutines.flow.Flow

interface FolderDataSource {
    suspend fun save(folderDto: FolderDto)
    suspend fun remove(folderDto: FolderDto)
    suspend fun getFolders(): Flow<List<FolderDto>>
}