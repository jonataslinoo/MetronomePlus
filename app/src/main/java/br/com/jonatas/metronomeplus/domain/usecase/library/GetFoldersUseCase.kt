package br.com.jonatas.metronomeplus.domain.usecase.library

import br.com.jonatas.metronomeplus.domain.model.Folder

interface GetFoldersUseCase {
    suspend operator fun invoke(): List<Folder>
}