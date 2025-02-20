package br.com.jonatas.metronomeplus.data.model

data class MeasureDto(
    val isPlaying: Boolean,
    val bpm: Int,
    val beats: MutableList<BeatDto>
)