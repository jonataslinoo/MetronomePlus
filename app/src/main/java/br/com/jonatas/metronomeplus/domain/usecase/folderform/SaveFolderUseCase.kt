package br.com.jonatas.metronomeplus.domain.usecase.folderform

import br.com.jonatas.metronomeplus.domain.model.Folder

interface SaveFolderUseCase {
    suspend operator fun invoke(folder: Folder)
}