package br.com.jonatas.metronomeplus.data.source

import br.com.jonatas.metronomeplus.data.model.FolderDto
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import java.util.Date

class FolderDataSourceImpl : FolderDataSource {

    private val folders: MutableList<FolderDto> = mutableListOf()

    override suspend fun save(folderDto: FolderDto) {
        folders.add(folderDto)
    }

    override suspend fun remove(folderDto: FolderDto) {
        folders.remove(folderDto)
    }

    override suspend fun getFolders(): List<FolderDto> {
        folders.addAll(
            listOf(
                FolderDto(id= "1",name = "TESTESTESTESTESTES", musics = 1, date = Date().time),
                FolderDto(id= "2",name = "TESTESTESTESTESTES", musics = 3, date = Date().time),
                FolderDto(id= "3",name = "TESTESTESTESTESTES", musics = 5, date = Date().time),
                FolderDto(id= "4",name = "TESTESTESTESTESTES", musics = 0, date = Date().time),
            )
        )
        return folders
    }
}