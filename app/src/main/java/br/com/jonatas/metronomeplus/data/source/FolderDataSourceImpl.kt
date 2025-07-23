package br.com.jonatas.metronomeplus.data.source

import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.jonatas.metronomeplus.data.local.DataStoreManager
import br.com.jonatas.metronomeplus.data.model.FolderDto
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.Date


class FolderDataSourceImpl(private val dataStoreManager: DataStoreManager) : FolderDataSource {

    companion object {
        private val FOLDERS_KEY = stringPreferencesKey("FOLDERS_KEY")
    }

    override suspend fun save(folderDto: FolderDto) {
        val currentFolders = getFolders().first().toMutableList()

        val index = currentFolders.indexOfFirst { it.id == folderDto.id }
        if (index != -1) {
            currentFolders[index] = folderDto
        } else {
            currentFolders.add(folderDto)
        }

        val jsonString = Json.encodeToString(currentFolders)
        dataStoreManager.setData(FOLDERS_KEY, jsonString)
    }

    override suspend fun remove(folderDto: FolderDto) {
    }

    override fun getFolders(): Flow<List<FolderDto>> {
        return dataStoreManager.getData(FOLDERS_KEY).map { jsonStringFound ->
            if (jsonStringFound.isNotEmpty()) {
                Json.decodeFromString<MutableList<FolderDto>>(jsonStringFound)
            } else {
                mockFoldersDto()
            }
        }
    }

    private suspend fun mockFoldersDto(): List<FolderDto> {
        val foldersDto = listOf(
            FolderDto(
                id = "1",
                name = "Folder",
                musics = 1,
                date = Date().time,
                isDefault = true
            ),
            FolderDto(id = "2", name = "Folder 2", musics = 3, date = Date().time),
            FolderDto(id = "3", name = "Folder 3", musics = 5, date = Date().time),
            FolderDto(id = "4", name = "Folder 4", musics = 0, date = Date().time),
        )

        val jsonString = Json.encodeToString(foldersDto)
        dataStoreManager.setData(FOLDERS_KEY, jsonString)

        return foldersDto
    }
}