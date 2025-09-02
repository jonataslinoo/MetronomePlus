package br.com.jonatas.metronomeplus.data.source

import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.jonatas.metronomeplus.data.local.DataStoreManager
import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.data.model.FolderSongCrossRefDto
import br.com.jonatas.metronomeplus.data.model.SongDto
import br.com.jonatas.metronomeplus.data.model.TimeSignatureDto
import br.com.jonatas.metronomeplus.domain.source.SongDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SongDataSourceImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : SongDataSource {

    companion object {
        private val SONGS_KEY = stringPreferencesKey("SONGS_KEY")
        private val CROSS_REF_KEY = stringPreferencesKey("FOLDER_SONG_CROSS_REF_KEY")
    }

    override fun getAllSongs(): Flow<List<SongDto>> {
        return dataStoreManager.getData(SONGS_KEY).map { jsonStringFound ->
            if (jsonStringFound.isNotEmpty()) {
                Json.decodeFromString<MutableList<SongDto>>(jsonStringFound)
            } else {
                mockSongsDto()
            }
        }
    }

    override fun getSongsByFolderId(folderId: String): Flow<List<SongDto>> {
        val crossRefFlow = dataStoreManager.getData(CROSS_REF_KEY).map { jsonString ->
            if (jsonString.isNotEmpty()) {
                Json.decodeFromString<MutableList<FolderSongCrossRefDto>>(jsonString)
                    .filter { it.folderId == folderId }
            } else {
                mockCrossResList().filter { it.folderId == folderId }
            }
        }

        val songsDtoFlow = dataStoreManager.getData(SONGS_KEY).map { jsonString ->
            if (jsonString.isNotEmpty()) {
                Json.decodeFromString<MutableList<SongDto>>(jsonString)
            } else {
                mockSongsDto()
            }
        }

        return combine(crossRefFlow, songsDtoFlow) { crossRef, songsDto ->
            songsDto.filter { song -> crossRef.any { it.songId == song.id } }
        }
    }

    private suspend fun mockSongsDto(): List<SongDto> {
        val songsDto = mockAllSongs()
        val crossRefList = mockCrossResList()

        val songsJsonString = Json.encodeToString(songsDto)
        dataStoreManager.setData(SONGS_KEY, songsJsonString)

        val crossRefJsonString = Json.encodeToString(crossRefList)
        dataStoreManager.setData(CROSS_REF_KEY, crossRefJsonString)

        return songsDto
    }

    private fun mockAllSongs(): List<SongDto> {
        val songsDto = listOf(
            SongDto(
                id = "song1",
                title = "Marcado",
                artist = "Ronaldo Bezerra",
                bpm = 120,
                timeSignature = TimeSignatureDto(14, 4),
                beatPatterns = listOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            ),
            SongDto(
                id = "song2",
                title = "Deus do Impossível",
                artist = "Adhemar de Campos",
                bpm = 90,
                timeSignature = TimeSignatureDto(6, 4),
                beatPatterns = listOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            ),
            SongDto(
                id = "song3",
                title = "Atos 2",
                artist = "Ministerio Zoe",
                bpm = 320,
                timeSignature = TimeSignatureDto(8, 4),
                beatPatterns = listOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            ),
            SongDto(
                id = "song4",
                title = "Aclame ao Senhor / Ele é Exaltado",
                artist = "Comunidade Evangélica internacial da zona sul",
                bpm = 75,
                timeSignature = TimeSignatureDto(4, 4),
                beatPatterns = listOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            ),
            SongDto(
                id = "song5",
                title = "Cruz",
                artist = "Diante do Trono",
                bpm = 172,
                timeSignature = TimeSignatureDto(3, 4),
                beatPatterns = listOf(
                    BeatDto(BeatStateDto.Accent),
                    BeatDto(BeatStateDto.Normal),
                    BeatDto(BeatStateDto.Normal),
                )
            ),
        )
        return songsDto
    }

    private fun mockCrossResList(): List<FolderSongCrossRefDto> {
        val crossRefList = listOf(
            FolderSongCrossRefDto(folderId = "folder2", songId = "song1"),
            FolderSongCrossRefDto(folderId = "folder2", songId = "song2"),
            FolderSongCrossRefDto(folderId = "folder2", songId = "song3"),
            FolderSongCrossRefDto(folderId = "folder3", songId = "song3"),
            FolderSongCrossRefDto(folderId = "folder3", songId = "song4"),
            FolderSongCrossRefDto(folderId = "folder3", songId = "song5"),
            FolderSongCrossRefDto(folderId = "folder3", songId = "song1"),
            FolderSongCrossRefDto(folderId = "folder4", songId = "song5"),
            FolderSongCrossRefDto(folderId = "folder4", songId = "song3"),
        )
        return crossRefList
    }
}