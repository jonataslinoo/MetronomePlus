package br.com.jonatas.metronomeplus.presenter.model.song

import br.com.jonatas.metronomeplus.presenter.model.BeatUiModel
import br.com.jonatas.metronomeplus.presenter.model.TimeSignatureUiModel

data class SongUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val bpm: Int,
    val timeSignature: TimeSignatureUiModel,
    val beatPatterns: List<BeatUiModel>
)