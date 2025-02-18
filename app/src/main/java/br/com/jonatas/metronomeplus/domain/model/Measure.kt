package br.com.jonatas.metronomeplus.domain.model

data class Measure(
    val isPlaying: Boolean,
    val bpm: Int,
    val beats: MutableList<Beat>
)