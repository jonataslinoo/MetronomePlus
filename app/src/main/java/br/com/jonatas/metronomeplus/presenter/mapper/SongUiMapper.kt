package br.com.jonatas.metronomeplus.presenter.mapper

import br.com.jonatas.metronomeplus.domain.model.Song
import br.com.jonatas.metronomeplus.presenter.model.song.SongUiModel

/** Domain for UI */
fun Song.toUiModel(): SongUiModel = SongUiModel(
    id = id,
    title = title,
    artist = artist,
    bpm = bpm,
    timeSignature = timeSignature.toUiModel(),
    beatPatterns = beatPatterns.map { it.toUiModel() },
    selected = selected
)

fun List<Song>.toUiModelList(): List<SongUiModel> = map { it.toUiModel() }

/** Ui for Domain */
