package br.com.jonatas.metronomeplus.domain.usecase.library

import br.com.jonatas.metronomeplus.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface GetFoldersUseCase {
    suspend operator fun invoke(): Flow<List<Folder>>
}