package br.com.jonatas.metronomeplus.domain.usecase.library

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow

class GetFoldersUseCaseImpl(
    private val repository: FolderRepository
) : GetFoldersUseCase {
    override fun invoke(): Flow<List<Folder>> {
        return repository.getFolders()
    }
}