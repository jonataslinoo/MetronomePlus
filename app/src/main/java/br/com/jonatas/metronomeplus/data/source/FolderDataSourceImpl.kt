package br.com.jonatas.metronomeplus.data.source

import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.jonatas.metronomeplus.data.local.DataStoreManager
import br.com.jonatas.metronomeplus.data.model.FolderDto
import br.com.jonatas.metronomeplus.data.model.FolderSongCrossRefDto
import br.com.jonatas.metronomeplus.domain.source.FolderDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.Date
import javax.inject.Inject

class FolderDataSourceImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : FolderDataSource {

    companion object {
        private val FOLDERS_KEY = stringPreferencesKey("FOLDERS_KEY")
        private val CROSS_REF_KEY = stringPreferencesKey("FOLDER_SONG_CROSS_REF_KEY")
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

    override suspend fun getFolder(folderId: String): FolderDto? {
        val foldersDto = dataStoreManager.getData(FOLDERS_KEY).map { jsonString ->
            Json.decodeFromString<List<FolderDto>>(jsonString)
        }

        val folderFound = foldersDto.first().find { it.id == folderId }
        return folderFound
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFolders(): Flow<List<FolderDto>> {
        val crossRefFlow = dataStoreManager.getData(CROSS_REF_KEY).map { jsonString ->
            if (jsonString.isNotEmpty()) {
                Json.decodeFromString<MutableList<FolderSongCrossRefDto>>(jsonString)
            } else {
                emptyList()
            }
        }

        val foldersDtoFlow = dataStoreManager.getData(FOLDERS_KEY).map { jsonString ->
            if (jsonString.isNotEmpty()) {
                Json.decodeFromString<MutableList<FolderDto>>(jsonString)
            } else {
                mockFoldersDto()
            }
        }

        return combine(crossRefFlow, foldersDtoFlow) { crossRef, foldersDto ->
            val songsByFolder = crossRef.groupBy { it.folderId }
            val songsById = crossRef.groupBy { it.songId }

            foldersDto.map { folderDto ->
                if (folderDto.isDefault) {
                    folderDto.copy(musics = songsById.size)
                } else {
                    folderDto.copy(musics = songsByFolder[folderDto.id]?.size ?: 0)
                }
            }
        }
    }

    private suspend fun mockFoldersDto(): List<FolderDto> {
        val foldersDto = listOf(
            FolderDto(
                id = "folder1",
                name = "Folder",
                musics = 1,
                date = Date().time,
                isDefault = true
            ),
            FolderDto(id = "folder2", name = "Folder 2", musics = 3, date = Date().time),
            FolderDto(id = "folder3", name = "Folder 3", musics = 5, date = Date().time),
            FolderDto(id = "folder4", name = "Folder 4", musics = 0, date = Date().time),
        )

        val jsonString = Json.encodeToString(foldersDto)
        dataStoreManager.setData(FOLDERS_KEY, jsonString)

        return foldersDto
    }
}