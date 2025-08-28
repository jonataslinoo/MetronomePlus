package br.com.jonatas.metronomeplus.domain.usecase.folderform

import br.com.jonatas.metronomeplus.domain.model.Folder

interface GetFolderUseCase {
    suspend operator fun invoke(folderId: String?): Folder
}