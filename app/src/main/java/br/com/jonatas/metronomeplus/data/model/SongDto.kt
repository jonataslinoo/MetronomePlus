package br.com.jonatas.metronomeplus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SongDto(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val bpm: Int,
    val timeSignature: TimeSignatureDto,
    val beatPatterns: List<BeatDto>
)