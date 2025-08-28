package br.com.jonatas.metronomeplus.domain.usecase.folderform

import br.com.jonatas.metronomeplus.domain.model.Folder
import br.com.jonatas.metronomeplus.domain.repository.FolderRepository
import javax.inject.Inject

class GetFolderUseCaseImpl @Inject constructor(
    private val repository: FolderRepository
) : GetFolderUseCase {
    override suspend fun invoke(folderId: String?): Folder {
        return repository.getFolder(folderId ?: "")
            ?: Folder(
                id = "",
                name = "",
                musics = 0,
                date = 0L
            )
    }
}