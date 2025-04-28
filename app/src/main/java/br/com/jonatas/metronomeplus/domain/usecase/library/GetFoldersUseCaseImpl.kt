package br.com.jonatas.metronomeplus.domain.usecase.library

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository

class GetFoldersUseCaseImpl(
    private val repository: FolderRepository
) : GetFoldersUseCase {
    override suspend fun invoke(): List<Folder> {
        return repository.getFolders()
    }
}