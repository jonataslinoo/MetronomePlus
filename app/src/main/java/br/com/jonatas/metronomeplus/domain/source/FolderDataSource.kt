package br.com.jonatas.metronomeplus.domain.source

import br.com.jonatas.metronomeplus.data.model.FolderDto

interface FolderDataSource {
    suspend fun save(folderDto: FolderDto)
    suspend fun remove(folderDto: FolderDto)
    suspend fun getFolders(): List<FolderDto>
}