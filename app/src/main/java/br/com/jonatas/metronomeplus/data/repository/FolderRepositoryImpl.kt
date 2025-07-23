package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.data.mapper.toDto
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FolderRepositoryImpl(
    private val folderDataSource: FolderDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FolderRepository {

    override suspend fun save(folder: Folder) = withContext(dispatcher) {
        folderDataSource.save(folder.toDto())
    }

    override suspend fun remove(folder: Folder) = withContext(dispatcher) {
        folderDataSource.remove(folder.toDto())
    }

    override fun getFolders(): Flow<List<Folder>> {
        return folderDataSource.getFolders().map {
            it.toDomainList()
        }.flowOn(dispatcher)
    }
}