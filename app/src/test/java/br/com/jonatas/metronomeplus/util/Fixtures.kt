package br.com.jonatas.metronomeplus.util

import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.BeatStateDto
import br.com.jonatas.metronomeplus.data.model.FolderDto
import br.com.jonatas.metronomeplus.data.model.FolderSongCrossRefDto
import br.com.jonatas.metronomeplus.data.model.SongDto
import br.com.jonatas.metronomeplus.data.model.TimeSignatureDto

object Fixtures {

    val FOLDERS_KEY = stringPreferencesKey("FOLDERS_KEY")
    val SONGS_KEY = stringPreferencesKey("SONGS_KEY")
    val CROSS_REF_KEY = stringPreferencesKey("FOLDER_SONG_CROSS_REF_KEY")

    fun mockAllSongsDto(): List<SongDto> = listOf(
        SongDto(id = "song1", title = "Marcado", artist = "Ronaldo Bezerra", bpm = 120, timeSignature = TimeSignatureDto(4,4), beatPatterns = listOf(BeatDto(BeatStateDto.Accent), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal),)),
        SongDto(id = "song2", title = "Deus do Impossível", artist = "Adhemar de Campos", bpm = 90, timeSignature = TimeSignatureDto(6,4), beatPatterns = listOf(BeatDto(BeatStateDto.Accent), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal),BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal),)),
        SongDto(id = "song3", title = "Atos 2", artist = "Ministerio Zoe", bpm = 320, timeSignature = TimeSignatureDto(8,4), beatPatterns = listOf(BeatDto(BeatStateDto.Accent), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal),BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal),BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal),)),
        SongDto(id = "song4", title = "Aclame ao Senhor / Ele é Exaltado", artist = "Comunidade Evangélica internacial da zona sul", bpm = 75, timeSignature = TimeSignatureDto(4,4), beatPatterns = listOf(BeatDto(BeatStateDto.Accent), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal),)),
        SongDto(id = "song5", title = "Cruz", artist = "Diante do Trono", bpm = 172, timeSignature = TimeSignatureDto(3,4), beatPatterns = listOf(BeatDto(BeatStateDto.Accent), BeatDto(BeatStateDto.Normal), BeatDto(BeatStateDto.Normal),)),
    )

    val mockFoldersDto: List<FolderDto> = listOf(
        FolderDto(id = "folder1", name = "Folder", musics = 5, date = 123L, isDefault = true),
        FolderDto(id = "folder2", name = "Folder 2", musics = 3, date = 123L),
        FolderDto(id = "folder3", name = "Folder 3", musics = 4, date = 123L),
        FolderDto(id = "folder4", name = "Folder 4", musics = 2, date = 123L),
    )

    val mockCrossRefList: List<FolderSongCrossRefDto> = listOf(
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

    val mockBeatListDto: List<BeatDto> = listOf(
        BeatDto(BeatStateDto.Normal),
        BeatDto(BeatStateDto.Silence),
        BeatDto(BeatStateDto.Accent),
        BeatDto(BeatStateDto.Medium)
    )
}