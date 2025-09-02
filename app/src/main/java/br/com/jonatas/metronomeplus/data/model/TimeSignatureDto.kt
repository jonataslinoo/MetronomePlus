package br.com.jonatas.metronomeplus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TimeSignatureDto(
    val numerator: Int,
    val denominator: Int
)