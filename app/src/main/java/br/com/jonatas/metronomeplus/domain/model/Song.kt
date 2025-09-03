package br.com.jonatas.metronomeplus.domain.model

data class Song(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val bpm: Int,
    val timeSignature: TimeSignature,
    val beatPatterns: List<Beat>,
)