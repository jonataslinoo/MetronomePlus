package br.com.jonatas.metronomeplus.data.model

data class MeasureDto(
    val bpm: Int,
    val beats: List<BeatDto>
)