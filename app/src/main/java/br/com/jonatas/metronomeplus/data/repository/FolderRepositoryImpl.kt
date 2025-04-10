package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.data.mapper.toDto
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource

class FolderRepositoryImpl(
    private val folderDataSource: FolderDataSource
) : FolderRepository {

    override suspend fun save(folder: Folder) {
        folderDataSource.save(folder.toDto())
    }

    override suspend fun remove(folder: Folder) {
        folderDataSource.remove(folder.toDto())
    }

    override suspend fun getFolders(): List<Folder> {
        return folderDataSource.getFolders().toDomainList()
    }
}