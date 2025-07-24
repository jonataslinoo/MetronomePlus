package br.com.jonatas.metronomeplus.domain.usecase.library

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import java.util.UUID

class SaveFolderUseCaseImpl(private val repository: FolderRepository) : SaveFolderUseCase {
    override suspend fun invoke(folder: Folder) {
        if (folder.isDefault) return

        val newFolder = if (folder.id.isEmpty()) {
            folder.copy(id = UUID.randomUUID().toString())
        } else {
            folder
        }

        repository.save(newFolder)
    }
}