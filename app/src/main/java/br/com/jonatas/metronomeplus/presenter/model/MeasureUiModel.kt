package br.com.jonatas.metronomeplus.presenter.model

data class MeasureUiModel(
    val isPlaying: Boolean = false,
    val bpm: Int,
    val beats: MutableList<BeatUiModel>
)