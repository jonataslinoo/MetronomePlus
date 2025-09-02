package br.com.jonatas.metronomeplus.util

import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.jonatas.metronomeplus.data.model.BeatDto
import br.com.jonatas.metronomeplus.data.model.BeatStateDto
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
}