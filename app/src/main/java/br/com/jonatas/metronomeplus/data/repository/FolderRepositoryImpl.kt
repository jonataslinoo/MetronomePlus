package br.com.jonatas.metronomeplus.data.repository

import br.com.jonatas.metronomeplus.data.mapper.toDomain
import br.com.jonatas.metronomeplus.data.mapper.toDomainList
import br.com.jonatas.metronomeplus.data.mapper.toDto
import br.com.jonatas.metronomeplus.di.app.IoDispatcher
import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val folderDataSource: FolderDataSource,
    @IoDispatcher
    private val dispatcher: CoroutineDispatcher
) : FolderRepository {

    override suspend fun save(folder: Folder) = withContext(dispatcher) {
        folderDataSource.save(folder.toDto())
    }

    override suspend fun remove(folder: Folder) = withContext(dispatcher) {
        folderDataSource.remove(folder.toDto())
    }

    override suspend fun getFolder(folderId: String): Folder? = withContext(dispatcher) {
        folderDataSource.getFolder(folderId)?.toDomain()
    }

    override fun getFolders(): Flow<List<Folder>> {
        return folderDataSource.getFolders().map {
            it.toDomainList()
        }.flowOn(dispatcher)
    }
}