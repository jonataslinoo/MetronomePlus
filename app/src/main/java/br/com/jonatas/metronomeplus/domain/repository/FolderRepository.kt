package br.com.jonatas.metronomeplus.domain.repository

import br.com.jonatas.metronomeplus.domain.model.Folder

interface FolderRepository {
    suspend fun save(folder: Folder)
    suspend fun remove(folder: Folder)
    suspend fun getFolders(): List<Folder>
}