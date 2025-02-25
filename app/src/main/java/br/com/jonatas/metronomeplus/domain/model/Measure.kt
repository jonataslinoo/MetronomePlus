package br.com.jonatas.metronomeplus.domain.model

data class Measure(
    val bpm: Int,
    val beats: List<Beat>
)