package br.com.jonatas.metronomeplus.domain.usecase.library

import br.com.jonatas.metronomeplus.domain.model.Folder

interface SaveFolderUseCase {
    suspend operator fun invoke(folder: Folder)
}